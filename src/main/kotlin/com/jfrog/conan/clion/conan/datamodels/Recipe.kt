package com.jfrog.conan.clion.conan.datamodels

import kotlinx.serialization.Serializable

@Serializable
data class Recipe(val name: String, val versions: List<String> = listOf())
