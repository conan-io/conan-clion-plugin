import json
import sys

from conan.api.conan_api import ConanAPI
from conans.errors import ConanException

from conan_helper import get_package_info_with_install, get_basic_info_with_inspect
from recipe_parser import get_package_info_from_recipe, get_basic_info_from_recipe
from repo_crawler import get_all_recipes


def main():
    conan_api = ConanAPI()

    root_dir = '../tmp/conan-center-index/recipes'

    # define which packages will get the information via a Conan install
    # these should be packages that have complex package_info() methods
    # that are hard to read with a parser
    force_install_packages = ["boost"]

    failed_references = []

    recipes = get_all_recipes(root_dir)

    packages_info = {}

    # get basic recipe information, like name, description, topics...
    for recipe_name, recipe_path, all_versions in recipes:
        packages_info[recipe_name] = {}
        packages_info[recipe_name]["versions"] = all_versions

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
            print(f"forcing: {recipe_name}", file=sys.stderr)
            use_conan_install = True

        if use_conan_install:
            package_info, err = get_package_info_with_install(conan_api, recipe_name, latest_version)
            if not err:
                packages_info[recipe_name].update(package_info)
            else:
                failed_references.append(recipe_name)

    json_data = json.dumps({"libraries": packages_info}, indent=4)

    print(json_data, file=sys.stdout)

    print("####################", file=sys.stderr)
    print("Total failures:", len(failed_references), failed_references, file=sys.stderr)
    print("####################", file=sys.stderr)

    return


if __name__ == '__main__':
    main()
