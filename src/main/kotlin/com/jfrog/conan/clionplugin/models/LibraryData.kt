package com.jfrog.conan.clionplugin.models

import kotlinx.serialization.Serializable

@Serializable
data class LibraryData(
        val libraries: HashMap<String, Library>
)
