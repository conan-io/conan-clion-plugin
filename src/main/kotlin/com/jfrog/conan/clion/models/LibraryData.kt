package com.jfrog.conan.clion.models

import kotlinx.serialization.Serializable

@Serializable
data class LibraryData(
        val libraries: HashMap<String, Library>
)
