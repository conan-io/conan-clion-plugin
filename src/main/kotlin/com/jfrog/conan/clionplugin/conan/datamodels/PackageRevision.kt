package com.jfrog.conan.clionplugin.conan.datamodels

import kotlinx.serialization.Serializable

@Serializable
data class PackageRevision(val info: PackageRevisionInfo)