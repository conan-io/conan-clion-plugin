package com.jfrog.conan.clionplugin.bundles

import com.intellij.DynamicBundle
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

@NonNls
private const val DIALOGS_BUNDLE = "messages.dialogs"

object DialogsBundle : DynamicBundle(DIALOGS_BUNDLE) {

    @Suppress("SpreadOperator")
    @JvmStatic
    fun message(@PropertyKey(resourceBundle = DIALOGS_BUNDLE) key: String, vararg params: Any) =
        getMessage(key, *params)

    @Suppress("SpreadOperator", "unused")
    @JvmStatic
    fun messagePointer(@PropertyKey(resourceBundle = DIALOGS_BUNDLE) key: String, vararg params: Any) =
        getLazyMessage(key, *params)
}
