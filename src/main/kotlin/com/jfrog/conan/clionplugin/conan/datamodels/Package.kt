package com.jfrog.conan.clionplugin.conan.datamodels

import kotlinx.serialization.Serializable


@Serializable
data class Package(val revisions: List<PackageRevision> = listOf())