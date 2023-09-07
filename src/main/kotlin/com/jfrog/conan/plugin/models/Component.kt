package com.jfrog.conan.plugin.models
import kotlinx.serialization.Serializable

@Serializable
data class Component(
        val cmake_target_name: String? = null
)
