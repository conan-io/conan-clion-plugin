import os

import yaml


def get_all_recipes(root_dir):
    ret = []
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

            ret.append((recipe_name, recipe_path, all_versions))
    return ret
