package com.jfrog.conan.clionplugin.bundles

import com.intellij.DynamicBundle
import com.jfrog.conan.clionplugin.bundles.WINDOW_BUNDLE
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

@NonNls
private const val WINDOW_BUNDLE = "messages.window"

object WindowBundle : DynamicBundle(WINDOW_BUNDLE) {

    @Suppress("SpreadOperator")
    @JvmStatic
    fun message(@PropertyKey(resourceBundle = WINDOW_BUNDLE) key: String, vararg params: Any) =
        getMessage(key, *params)

    @Suppress("SpreadOperator", "unused")
    @JvmStatic
    fun messagePointer(@PropertyKey(resourceBundle = WINDOW_BUNDLE) key: String, vararg params: Any) =
        getLazyMessage(key, *params)
}