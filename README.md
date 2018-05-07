# Conan CLion Plugin [![Build Status](https://travis-ci.org/conan-io/conan-clion-plugin.svg?branch=master)](https://travis-ci.org/conan-io/conan-clion-plugin)
Conan CLion plugin adds Conan support to Jetbrains CLion.

# Prerequisites
* CLion version 2018.1 and above.
* Conan executable in path environment variable.

# Building and Testing the Sources
To build the plugin sources, please follow these steps:
1. Clone the code from git.
2. If you'd just like to run the tests, run the following command:
    ```
    ./gradlew clean test
    ```
3. Build and create the Conan CLion Plugin zip file by running the following gradle command.
    ```
    ./gradlew clean build
    ```
After the build finishes, you'll find the zip file in the *build/distributions* directory.
The zip file can be loaded into CLion.

# Installation  
1. Download the [latest release](https://github.com/conan-io/conan-clion-plugin/releases/latest) or build it from sources.
2. Under Settings (Preferences) | Plugins, click `Install plugin from disk...`
3. Select the zip file and click OK.

# Developing the Plugin Code
If you'd like to help us develop and enhance the plugin, this section is for you.
To build and run the sandbox following your code changes, follow these steps:

1. From the *Gradle Projects* window, expand *clion-conan-plugin --> Tasks --> IntelliJ*
2. Run the *buildPlugin* task.
3. Run the *runIde* task.

# Code Contributions
We welcome community contribution through pull requests.
