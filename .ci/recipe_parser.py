import ast

import astunparse


def parse_recipe_info(conanfile):
    tree = ast.parse(conanfile)
    desc = ""
    lic = ""
    for node in ast.walk(tree):
        if isinstance(node, ast.ClassDef):
            for body_node in node.body:
                if isinstance(body_node, ast.Assign) and len(body_node.targets) == 1:
                    target = body_node.targets[0]
                    if isinstance(target, ast.Name) and target.id == "description":
                        value_node = body_node.value
                        if isinstance(value_node, ast.Str):
                            desc = value_node.s
                    if isinstance(target, ast.Name) and target.id == "license":
                        value_node = body_node.value
                        if isinstance(value_node, ast.Str):
                            lic = value_node.s

    return desc, lic


def get_basic_info_from_recipe(recipe_name, recipe_path):
    basic_info = {}

    with open(recipe_path, 'r') as f:
        recipe_content = f.read()

    description, license = parse_recipe_info(recipe_content)
    if not description:
        raise Exception(f"could not get basic information for: {recipe_name}")
    basic_info["description"] = description.replace("\n", "").replace("  ", "")
    basic_info["license"] = [license] if type(license) != list else license
    # this is just a speculation, that inspect was successful does not mean is v2 compatible
    basic_info["v2"] = False
    return basic_info


def get_package_info_from_recipe(recipe_path):

    with open(recipe_path, 'r') as f:
        recipe_content = f.read()

    root = ast.parse(recipe_content)

    package_info = {}

    for node in ast.walk(root):
        if isinstance(node, ast.ClassDef):
            base_classes = [base.id for base in node.bases if isinstance(base, ast.Name)]
            if 'ConanFile' in base_classes:
                for sub_node in node.body:
                    if isinstance(sub_node, ast.FunctionDef) and sub_node.name == "package_info":
                        for stmt in sub_node.body:
                            statement = astunparse.unparse(stmt)
                            if isinstance(stmt, ast.Expr) and isinstance(stmt.value, ast.Call):
                                func = stmt.value.func
                                line = astunparse.unparse(stmt)
                                if isinstance(func,
                                              ast.Attribute) and func.attr == 'set_property' and 'cpp_info.components' not in line:
                                    # Check if the function is called on a component
                                    if isinstance(func.value, ast.Attribute) \
                                            and func.value.attr == 'cpp_info' \
                                            and isinstance(func.value.value, ast.Name) \
                                            and func.value.value.id == 'self':

                                        args = stmt.value.args
                                        if len(args) == 2:
                                            if args[0].s == "cmake_file_name" or args[0].s == "cmake_target_name":
                                                if isinstance(args[1], ast.Str):
                                                    package_info[args[0].s] = str(args[1].s)
                                                else:
                                                    # the target name of file name is defined by a python variable
                                                    # we will have to use conan to get the info
                                                    raise Exception(
                                                        "Target info can't be recovered by parsing the ConanFile")
                                elif 'cpp_info.components' in line and 'set_property' in line:
                                    args = stmt.value.args
                                    if len(args) == 2 and isinstance(args[0], ast.Str) and isinstance(args[1], ast.Str):
                                        component_name = str(astunparse.unparse(func.value.slice)).replace("'",
                                                                                                           "").replace(
                                            '"', '').strip()
                                        if not component_name.startswith("_"):
                                            package_info["components"] = package_info.get("components", {})
                                            package_info["components"][component_name] = package_info["components"].get(
                                                component_name,
                                                {})
                                            if args[0].s == "cmake_target_name":
                                                package_info["components"][component_name]["cmake_target_name"] = args[
                                                    1].s

    return package_info
