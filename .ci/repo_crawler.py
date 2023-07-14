import os
import subprocess

import yaml


def get_recipe_last_modify(recipe_path):
    command = f'git --no-pager rev-list -1 HEAD -- {recipe_path} | xargs -I {{}} git --no-pager show -s --pretty=format:%cd --date=unix {{}}'
    #command = f'git --no-pager log -1 --pretty=format:%cd --date=unix -- {recipe_path}'
    result = subprocess.run(command, shell=True, capture_output=True, text=True)
    out = result.stdout
    return int(out.strip())


def get_all_recipes(root_dir):
    ret = []
    recipes = []
    for dirpath, dirnames, filenames in os.walk(root_dir):
        if 'config.yml' in filenames:
            recipe_name = os.path.basename(dirpath)

            with open(os.path.join(dirpath, 'config.yml'), 'r') as f:
                data = yaml.safe_load(f)

            all_versions = list(data['versions'].keys())
            # the rule in c3i is that these versions are ordered in the yml
            # we could double check doing a conan search
            latest_version = all_versions[0]
            recipe_folder = data['versions'][latest_version]['folder']

            recipe_path = os.path.join(dirpath, recipe_folder, "conanfile.py")

            recipes.append((recipe_name, recipe_path, all_versions))

    old_path = os.getcwd()
    os.chdir(root_dir)
    print("getting timestamps from repository")
    total = len(recipes)
    for i, (recipe_name, recipe_path, all_versions) in enumerate(recipes):
        relative_path = os.path.relpath(recipe_path, root_dir)
        timestamp = get_recipe_last_modify(relative_path)
        ret.append((recipe_name, recipe_path, all_versions, timestamp))
        print(f"Processed {recipe_path} ({i + 1}/{total})")
    os.chdir(old_path)

    return ret
