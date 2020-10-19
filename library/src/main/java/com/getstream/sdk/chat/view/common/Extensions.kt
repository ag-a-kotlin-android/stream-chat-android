package com.getstream.sdk.chat.view.common

import android.content.ContextWrapper
import android.view.View
import androidx.appcompat.app.AppCompatActivity

internal fun View.visible(isVisible: Boolean) {
    visibility = if (isVisible) {
        View.VISIBLE
    } else {
        View.GONE
    }
}

/**
 * Ensures the context being accessed in a View can be cast to Activity
 */
internal val View.activity: AppCompatActivity?
    get() {
        var context = context
        while (context is ContextWrapper) {
            if (context is AppCompatActivity) {
                return context
            }
            context = context.baseContext
        }
        return null
    }
