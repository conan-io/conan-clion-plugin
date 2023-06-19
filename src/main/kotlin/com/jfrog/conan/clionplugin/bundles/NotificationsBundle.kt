package com.jfrog.conan.clionplugin.bundles

import com.intellij.DynamicBundle
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

@NonNls
private const val NOTIFICATIONS_BUNDLE = "messages.notifications"

object NotificationsBundle : DynamicBundle(NOTIFICATIONS_BUNDLE) {

    @Suppress("SpreadOperator")
    @JvmStatic
    fun message(@PropertyKey(resourceBundle = NOTIFICATIONS_BUNDLE) key: String, vararg params: Any) =
        getMessage(key, *params)

    @Suppress("SpreadOperator", "unused")
    @JvmStatic
    fun messagePointer(@PropertyKey(resourceBundle = NOTIFICATIONS_BUNDLE) key: String, vararg params: Any) =
        getLazyMessage(key, *params)
}