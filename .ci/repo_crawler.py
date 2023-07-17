import os
import subprocess
import datetime

import yaml


def get_modified_recipes(root_dir, last_update):
    old_path = os.getcwd()
    os.chdir(root_dir)
    date = datetime.datetime.fromtimestamp(last_update)
    formatted_date = date.strftime('%Y-%m-%d %H:%M:%S')
    c3i_default_branch = "master"
    command = f'git --no-pager log {c3i_default_branch} --before="{formatted_date}" -n 1 --pretty="%h"'
    base = subprocess.run(command, shell=True, capture_output=True, text=True).stdout.strip()
    command = f'git rev-parse --short {c3i_default_branch}'
    head = subprocess.run(command, shell=True, capture_output=True, text=True).stdout.strip()
    command = f'git diff {base}..{head} --name-only'
    modified = subprocess.run(command, shell=True, capture_output=True, text=True).stdout.splitlines()
    os.chdir(old_path)
    modified_recipes = {path.split('/')[1] for path in modified if path.startswith("recipes/")}
    return modified_recipes


def get_recipes(root_dir, last_update):
    modified_since_last_update = get_modified_recipes(root_dir, last_update)
    ret = []
    for dirpath, dirnames, filenames in os.walk(root_dir):
        if 'config.yml' in filenames:
            recipe_name = os.path.basename(dirpath)
            outdated = recipe_name in modified_since_last_update

            with open(os.path.join(dirpath, 'config.yml'), 'r') as f:
                data = yaml.safe_load(f)

            all_versions = list(data['versions'].keys())
            # the rule in c3i is that these versions are ordered in the yml
            # we could double check doing a conan search
            latest_version = all_versions[0]
            recipe_folder = data['versions'][latest_version]['folder']

            recipe_path = os.path.join(dirpath, recipe_folder, "conanfile.py")

            ret.append((recipe_name, recipe_path, all_versions, outdated))

    return ret
