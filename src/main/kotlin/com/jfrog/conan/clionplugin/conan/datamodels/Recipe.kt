package com.jfrog.conan.clionplugin.conan.datamodels

import kotlinx.serialization.Serializable

@Serializable
data class Recipe(val revisions: List<RecipeRevision> = listOf())
