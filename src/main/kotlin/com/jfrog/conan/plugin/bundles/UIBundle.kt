package com.jfrog.conan.plugin.bundles

import com.intellij.DynamicBundle
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

@NonNls
private const val UI_BUNDLE = "messages.ui"

object UIBundle : DynamicBundle(UI_BUNDLE) {

    @Suppress("SpreadOperator")
    @JvmStatic
    fun message(@PropertyKey(resourceBundle = UI_BUNDLE) key: String, vararg params: Any) =
        getMessage(key, *params)

    @Suppress("SpreadOperator", "unused")
    @JvmStatic
    fun messagePointer(@PropertyKey(resourceBundle = UI_BUNDLE) key: String, vararg params: Any) =
        getLazyMessage(key, *params)
}
