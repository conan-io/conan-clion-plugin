# Conan CLion Plugin [![Build Status](https://travis-ci.org/conan-io/conan-clion-plugin.svg?branch=master)](https://travis-ci.org/conan-io/conan-clion-plugin)
Conan CLion plugin adds Conan support to Jetbrains CLion.

# Prerequisites
* CLion version 2018.3 and above.
* Conan executable in path environment variable.

# Using the Plugin
#### Matching Profiles
Before running the build for the first time, please match a Conan profile to each CMake profile.
To do this click on the *Match profiles* ![Match profiles](src/main/resources/icons/properties.png?raw=true "Match profiles") icon.

![Matching profiles form](src/main/resources/screenshots/matching-profiles.png?raw=true "Matching profiles form")

#### Downloading Conan Dependencies
* The Conan dependencies are automatically downloaded before each CMake build.
However, you can download dependencies manually by clicking on the *install* ![Install](src/main/resources/icons/conan.png?raw=true "Install") or the *Update and install* ![Update and install](src/main/resources/icons/conan-update-install.png?raw=true "Update and install") button.
* ![Install](src/main/resources/icons/conan.png?raw=true "Install") - Installs conan dependencies.
* ![Update and install](src/main/resources/icons/conan-update-install.png?raw=true "Update and install") - Checks updates existence from upstream remotes and installs conan dependencies.

![Install](src/main/resources/screenshots/install.png?raw=true "Install")

#### Config Install
Conan lets you download and use a pre-configured environment. In order to do so, click on *Open configuration* ![Open configuration](src/main/resources/icons/settings.png?raw=true "Open configuration") button, supply your config folder URL, and click download.

![Install](src/main/resources/screenshots/config-install.png?raw=true "Config install")

#### Set Install Arguments
In order to set Conan install arguments, click on *Open configuration* button and provide your install arguments.
These arguments are passed 'as is' to the Conan install command.

##### Install args examples:

```
--build missing
```
```
-o zlib:shared=True -o bzip2:option=132
```

#### Set Conan Executable Path
If the Conan executable is part of the PATH environment variable, it is used by default.
To set a different Conan executable, click on *Open configuration* button and provide your conan executable path.

# Building and Testing the Plugin's Sources
To build and run the plugin sources, please follow these steps:
1. Clone the code from git.
2. To run the tests, use the following command:
    ```
    ./gradlew clean test
    ```
3. To Build and create the Conan CLion Plugin zip file, run the following command.
    ```
    ./gradlew clean build
    ```
After the build process is completed, you'll find the zip file in the *build/distributions* directory.
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
