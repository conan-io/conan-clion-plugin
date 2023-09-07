package com.jfrog.conan.plugin.conan.datamodels

import kotlinx.serialization.Serializable

@Serializable
data class Recipe(val name: String, val versions: List<String> = listOf())
