package com.jfrog.conan.plugin.models

class PersistentStorageKeys {
    companion object {
        const val HAS_BEEN_SETUP = "com.jfrog.conanplugin.hasbeensetup"
        const val AUTOMATIC_ADD_CONAN = "com.jfrog.conanplugin.addconansupport"
        // CONAN_EXECUTABLE can have 3 states:
        // - value="/path/configured/by/user" the user entered a path
        // - value="conan" the user selected "Use conan from system"
        // - value="" not yet configured
        const val CONAN_EXECUTABLE = "com.jfrog.conanplugin.conanexecutable"
        const val AUTOMANAGE_CMAKE_ADVANCED_SETTINGS = "com.jfrog.conanplugin.automanage.cmake.advanced.settings"
    }
}
