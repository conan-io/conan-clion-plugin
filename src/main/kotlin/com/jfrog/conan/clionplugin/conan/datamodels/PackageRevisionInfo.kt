package com.jfrog.conan.clionplugin.conan.datamodels

import kotlinx.serialization.Serializable

@Serializable
data class PackageRevisionInfo(val settings: HashMap<String, String>, val options: HashMap<String, String>)