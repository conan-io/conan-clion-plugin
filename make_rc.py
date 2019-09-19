#!/usr/bin/env python

import sys
import os
import re
import textwrap
from lxml import etree

from functools import partial
from datetime import date

try:
    from github import Github, RateLimitExceededException
except ImportError:
    sys.stderr.write("Install 'pip install PyGithub'")
    sys.exit(1)


me = os.path.dirname(__file__)

version_pattern = re.compile(r'^pluginVersion\s=\s(?P<v>[\d\.]+)$', re.MULTILINE)
gradle_properties = os.path.join(me, "gradle.properties")

# Get the github client
def get_github():
    github_token = os.environ.get("GITHUB_TOKEN")
    if not github_token:
        sys.stderr.write("Please, provide a read-only token to access Github using environment variable 'GITHUB_TOKEN'\n")

    # Find matching milestone
    g = Github(github_token)
    return g

# Get the github repository
def get_github_repository():
    g = get_github()
    return g.get_repo('conan-io/conan-clion-plugin')

repo = get_github_repository()


def get_current_version():
    v_gradle_properties = None

    # Get version from 'gradle.properties'
    for line in open(gradle_properties, "r").readlines():
        m = version_pattern.match(line)
        if m:
            v_gradle_properties = m.group("v")
    
    return v_gradle_properties


def set_current_version(version):
    v_gradle_properties = None

    def replace_closure(subgroup, replacement, m):
        if m.group(subgroup) not in [None, '']:
            start = m.start(subgroup)
            end = m.end(subgroup)
            return m.group(0)[:start] + replacement + m.group(0)[end:]

    # Substitute version in 'gradle.properties'
    lines = []
    for line in open(gradle_properties, "r").readlines():
        line_sub = version_pattern.sub(partial(replace_closure, "v", version), line)
        lines.append(line_sub)
    with open(gradle_properties, "w") as f:
        f.write("".join(lines))


def write_changelog(version, prs):
    changelog = os.path.join(me, "CHANGELOG.md")
    plugin_xml = os.path.join(me, "src", "main", "resources", "META-INF", "plugin.xml")

    prs = [pr for pr in prs if "[skip changelog]" not in pr.title]
    version_content = ["- {} ([#{}]({}))\n".format(pr.title, pr.number, pr.html_url) for pr in prs]
    sys.stdout.write("*"*20)
    sys.stdout.write("\n{}".format(''.join(version_content)))
    sys.stdout.write("*"*20)
    sys.stdout.write("\n\n")
    if not query_yes_no("This is the list of items that will be added to the CHANGELOG"):
        sys.stdout.write("Exit!")
        sys.exit(1)

    version_date = date.today().strftime('%Y-%m-%d')

    # Write to CHANGELOG.md
    new_content = []
    changelog_found = False
    changelog_added = False
    version_pattern = re.compile("## [\d\.]+")
    for line in open(changelog, "r").readlines():
        if changelog_added:
            pass
        elif not changelog_found:
            changelog_found = bool(line.strip() == "# Changelog")
        else:
            if version_pattern.match(line):
                # Add before new content
                new_content.append("## {}\n\n".format(version))
                new_content.append("**{}**\n\n".format(version_date))
                new_content += version_content
                new_content.append("\n\n")
                changelog_added = True
        new_content.append(line)

    with open(changelog, "w") as f:
        f.write("".join(new_content))

    # Update plugin.xml
    parser = etree.XMLParser(strip_cdata=False)
    tree = etree.parse(plugin_xml, parser)
    changelog_node = tree.find('change-notes')
    clog_cdata = ['<li>{} (<a href="{}">#{}</a>)</li>'.format(pr.title, pr.html_url, pr.number) for pr in prs]
    changelog_node.text = etree.CDATA("""
    <a href="https://github.com/conan-io/conan-clion-plugin/releases/tag/{version}">
        <b>v{versioon}</b>
    </a> ({date}})
    <br>
    <ul>
    {items}
    </ul>
    <br>
    <a href="https://github.com/conan-io/conan-clion-plugin/releases">
        <b>Full Changelog</b>
    </a>
    """.format(items='\n'.join(clog_cdata), version=version, date=version_date))
    tree.write(plugin_xml)


def get_git_current_branch():
    return os.popen('git rev-parse --abbrev-ref HEAD').read().strip()

def get_git_is_clean():
    return len(os.popen('git status --untracked-files=no --porcelain').read().strip()) == 0

def query_yes_no(question, default="yes"):
    valid = {"yes": True, "y": True, "ye": True,
             "no": False, "n": False}
    if default is None:
        prompt = " [y/n] "
    elif default == "yes":
        prompt = " [Y/n] "
    elif default == "no":
        prompt = " [y/N] "
    else:
        raise ValueError("invalid default answer: '%s'" % default)

    while True:
        sys.stdout.write(question + prompt)
        choice = input().lower()
        if default is not None and choice == '':
            return valid[default]
        elif choice in valid:
            return valid[choice]
        else:
            sys.stdout.write("Please respond with 'yes' or 'no' (or 'y' or 'n').\n")

def work_on_release(next_release):
    open_milestones = repo.get_milestones(state='open')
    for milestone in open_milestones:
        if str(milestone.title) == next_release:
            # Gather pull requests
            prs = [it for it in repo.get_pulls(state="all") if it.milestone == milestone]
            sys.stdout.write("Found {} pull request for this milestone:\n".format(len(prs)))
            for p in prs:
                status = "[!]" if p.state != "closed" else ""
                sys.stdout.write("\t {}\t#{} {}\n".format(status, p.number, p.title))
            
            # Gather issues
            issues = [it for it in repo.get_issues(milestone=milestone, state="all")]
            sys.stdout.write("Found {} issues for this milestone:\n".format(len(issues)))
            for issue in issues:
                status = "[!]" if issue.state != "closed" else ""
                sys.stdout.write("\t {}\t#{} {}\n".format(status, issue.number, issue.title))
            
            # Any open PR or issue?
            if any([p.state != "closed" for p in prs]) or any([issue.state != "closed" for issue in issues]):
                sys.stderr.write("Close all PRs and issues belonging to the milestone before making the release")
                return
            
            # Checkout the release branch and commit the changes
            os.system('git checkout -b release/{}'.format(next_release))

            # Modify the working directory
            set_current_version(next_release)
            prs = [pr for pr in prs if pr.merged]
            write_changelog(next_release, prs)

            if query_yes_no("Commit and push to 'conan' repository"):
                os.system("git add CHANGELOG.md")
                os.system("git add gradle.properties")
                os.system("git add src/main/resources/META-INF/plugin.xml")

                os.system('git commit -m "Preparing release {}"'.format(next_release))
                os.system('git push --set-upstream conan release/{}'.format(next_release))

                sys.stdout.write("Now create PR to 'master' and PR back to 'dev'")
                pr = repo.create_pull(title="Release {}".format(next_release),
                                      head="release/{}".format(next_release),
                                      base="master",
                                      body=textwrap.dedent("""
                                      Release {}

                                      Manual checking:
                                      - [ ] General usability

                                      After merging, don't forget to create the tag and merge back 'master' into 'dev'.
                                      """.format(next_release)))

                # TOO DANGEROUS: a simple click on 'update with dev' will make a commit to 'master'
                #repo.create_pull(title="Merge back release branch {}".format(next_release),
                #                head="master",  # So we get also the merge commit from 'master'
                #                base="dev",
                #                body="Merging back changes from release branch {}. Don't merge before #{}".format(next_release, pr.number))
            else:
                sys.stdout.write("You will need to commit and push yourself, and to create the PRs")
            break
    else:
        sys.stderr.write("No milestone matching version {!r}. Open milestones found were '{}'\n".format(next_release, "', '".join([it.title for it in open_milestones])))

def guess_next_release(current_release, head_branch):
    major, minor, patch = map(int, current_release.split("."))

    open_milestones = repo.get_milestones(state='open')

    # Look into open milestones with PRs already merged to 'head_branch' branch
    ml_to_consider = []
    closed_prs = repo.get_pulls(state="closed")
    for milestone in open_milestones:
        prs = [it for it in closed_prs if it.milestone == milestone]
        merged_prs = [it for it in prs if it.merged and it.base.ref==head_branch]
        if merged_prs:
            ml_to_consider.append((milestone, merged_prs))

    # If no PR
    if not len(ml_to_consider):
        sys.stderr.write("Cannot find any milestone suitable for the operation\n")
        sys.exit(1)

    # If we have more than one, we should warn the user
    if len(ml_to_consider) > 1:
        sys.stderr.write("There are several open milestones with merged PRs into '{}' branch."
                         " Cannot decide which one to use for next release. Please,"
                         " reorganize milestones.\n".format(head_branch))
        for ml, prs in ml_to_consider:
            sys.stdout.write("Milestone: {}\n".format(milestone.title))
            for it in prs:
                sys.stdout.write("\t#{} {}\n".format(it.number, it.title))
        sys.exit(1)

    next_milestone, _ = ml_to_consider[0]

    # Check it is a valid release and it is greater than the current one
    next_major, next_minor, next_patch = map(int, next_milestone.title.split('.'))
    assert (next_major >= major) or \
           (next_major == major and next_minor >= minor) or \
           (next_major == major and next_minor == minor and next_patch >= patch), "{} < {}!!".format(next_milestone.title, current_release)
    return next_milestone.title


def main():
    current_branch = get_git_current_branch()
    if current_branch != "dev":
        sys.stderr.write("Move to the 'dev' branch to work with this tool. You are in '{}'\n".format(current_branch))
        sys.exit(1)
    
    if not get_git_is_clean():
        sys.stderr.write("Current branch is not clean\n")
        sys.exit(1)

    v = get_current_version()
    sys.stdout.write("Current version is {!r}\n".format(v))
    next_release = guess_next_release(v, current_branch)
    if query_yes_no("Next version will be {!r}".format(next_release)):
        work_on_release(next_release)
    else:
        sys.stdout.write("Sorry, I cannot help you then...")


if __name__ == "__main__":
    try:
        main()
    except RateLimitExceededException:
        sys.stderr.write("Rate limit!")
        g = get_github()
        r = g.get_rate_limit()
        sys.stdout.write(" limit: {}".format(r.core.limit))
        sys.stdout.write(" remaining: {}".format(r.core.remaining))
