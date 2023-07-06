package com.jfrog.conan.clionplugin.models

import kotlinx.serialization.Serializable

@Serializable
data class Library(
    val description: String,
    val license: List<String>,
    val v2: Boolean,
    val cmake_file_name: String? = null,
    val cmake_target_name: String? = null,
    val components: HashMap<String, Component>? = null
)
