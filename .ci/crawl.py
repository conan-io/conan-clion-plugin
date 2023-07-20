import json
import sys
import argparse
import datetime
import time

from conan.api.conan_api import ConanAPI
from conans.errors import ConanException

from conan_helper import get_package_info_with_install, get_basic_info_with_inspect
from recipe_parser import get_package_info_from_recipe, get_basic_info_from_recipe
from repo_crawler import get_recipes, get_modified_recipes


def main(recipes_dir, input_json_path, output_json_path):

    with open(input_json_path, 'r') as f:
        current_data = json.load(f)
    packages_info_current = current_data["libraries"]
    last_update = current_data["date"]
    now = datetime.datetime.now()
    current_date = int(time.mktime(now.timetuple()))

    conan_api = ConanAPI()

    # define which packages will get the information via a Conan install
    # these should be packages that have complex package_info() methods
    # that are hard to read with a parser
    force_install_packages = ["boost", "opencv"]

    failed_references = []

    recipes = get_recipes(recipes_dir, last_update)

    packages_info = {}

    skipped = 0

    # get basic recipe information, like name, description, topics...
    for recipe_name, recipe_path, all_versions, outdated in recipes:

        current_recipe_info = packages_info_current.get(recipe_name)

        # if the cloned recipe is not outdated we stay with our data, but we update always the versions
        if not outdated:
            packages_info[recipe_name] = current_recipe_info
            # we always update the versions, maybe the config.yml was updated but not the recipe
            packages_info[recipe_name]["versions"] = all_versions
            skipped = skipped + 1
            print(f"skip ({skipped}): {recipe_name}")
            continue

        print(f"processing: {recipe_name}")
        packages_info[recipe_name] = {"versions": all_versions}

        # we only fill info for latest version
        latest_version = all_versions[0]
        try:
            basic_info = get_basic_info_with_inspect(conan_api, recipe_path)
        except ConanException:
            basic_info = get_basic_info_from_recipe(recipe_name, recipe_path)

        packages_info[recipe_name].update(basic_info)

        # try to get properties and components information from
        # the repo
        use_conan_install = False
        if recipe_name not in force_install_packages:
            try:
                package_info = get_package_info_from_recipe(recipe_path)
                packages_info[recipe_name].update(package_info)
            except Exception as exc:
                print(f"fail parsing: {recipe_name} {str(exc)}, will try to get info from conan install",
                      file=sys.stderr)
                use_conan_install = True
        else:
            print(f"forcing: {recipe_name}")
            use_conan_install = True

        if use_conan_install:
            package_info, err = get_package_info_with_install(conan_api, recipe_name, latest_version)
            if not err:
                packages_info[recipe_name].update(package_info)
            else:
                failed_references.append(recipe_name)

    json_data = {"libraries": packages_info, "date": current_date}

    with open(output_json_path, 'w') as f:
        json.dump(json_data, f, indent=4, sort_keys=True)

    print("####################")
    print("Total failures:", len(failed_references), failed_references)
    print("####################")

    return


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Get Conan Center packages information.')
    parser.add_argument('recipes_dir', help='Directory where conan center index recipes folder is located.')
    parser.add_argument('input_json_path', help='Path to the json input with packages information.')
    parser.add_argument('output_json_path', help='Path to the json output to store packages information.')
    args = parser.parse_args()
    main(args.recipes_dir, args.input_json_path, args.output_json_path)