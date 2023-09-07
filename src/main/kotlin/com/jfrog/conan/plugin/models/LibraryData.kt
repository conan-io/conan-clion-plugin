package com.jfrog.conan.plugin.models

import kotlinx.serialization.Serializable

@Serializable
data class LibraryData(
        val libraries: HashMap<String, Library>
)
