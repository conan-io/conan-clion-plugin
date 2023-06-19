package com.jfrog.conan.clionplugin.models

class PersistentStorageKeys {
    companion object {
        const val AUTOMATIC_ADD_CONAN = "com.jfrog.conanplugin.addconansupport"
        // CONAN_EXECUTABLE can have 3 states:
        // - value="/path/configured/by/user" the user entered a path
        // - value="conan" the user selected "Use conan from system"
        // - value="" not yet configured
        const val CONAN_EXECUTABLE = "com.jfrog.conanplugin.conanexecutable"
    }
}