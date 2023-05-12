package com.jfrog.conan.clionplugin.conan.datamodels

import kotlinx.serialization.Serializable

@Serializable
data class RecipeRevision(val packages: List<Package> = listOf())