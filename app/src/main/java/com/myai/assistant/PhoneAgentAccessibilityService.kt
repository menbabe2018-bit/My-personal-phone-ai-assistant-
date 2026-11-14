package com.myai.assistant

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * User-enabled, foreground accessibility bridge.
 * Only exposes narrow actions requested by the user and confirmed in the app.
 */
class PhoneAgentAccessibilityService : AccessibilityService() {
    companion object {
        @Volatile var instance: PhoneAgentAccessibilityService? = null
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) { }

    override fun onInterrupt() { }

    override fun onDestroy() {
        if (instance === this) instance = null
        super.onDestroy()
    }

    fun readVisibleText(): String {
        val root = rootInActiveWindow ?: return "I can't see the current screen. Open an app and try again."
        val lines = mutableListOf<String>()
        collectText(root, lines)
        return if (lines.isEmpty()) "I couldn't find readable text on this screen." else lines.distinct().take(60).joinToString("\n")
    }

    private fun collectText(node: AccessibilityNodeInfo, output: MutableList<String>) {
        val value = node.text?.toString()?.trim().orEmpty()
        if (value.isNotEmpty()) output.add(value)
        val description = node.contentDescription?.toString()?.trim().orEmpty()
        if (description.isNotEmpty() && description != value) output.add(description)
        for (i in 0 until node.childCount) node.getChild(i)?.let { child ->
            collectText(child, output)
            child.recycle()
        }
    }

    fun tapVisibleText(target: String): Boolean {
        val root = rootInActiveWindow ?: return false
        return try { findAndClick(root, target.trim(), 0) } finally { root.recycle() }
    }

    private fun findAndClick(node: AccessibilityNodeInfo, target: String, depth: Int): Boolean {
        if (depth > 30) return false
        val text = node.text?.toString().orEmpty()
        val desc = node.contentDescription?.toString().orEmpty()
        if ((text.equals(target, true) || desc.equals(target, true)) && node.isVisibleToUser) {
            var candidate: AccessibilityNodeInfo? = AccessibilityNodeInfo.obtain(node)
            while (candidate != null && !candidate.isClickable) {
                val parent = candidate.parent
                candidate.recycle()
                candidate = parent
            }
            val clicked = candidate?.performAction(AccessibilityNodeInfo.ACTION_CLICK) == true
            candidate?.recycle()
            if (clicked) return true
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findAndClick(child, target, depth + 1)
            child.recycle()
            if (found) return true
        }
        return false
    }

    fun scrollScreen(down: Boolean): Boolean {
        val root = rootInActiveWindow ?: return false
        return try { findScrollable(root, down, 0) } finally { root.recycle() }
    }

    private fun findScrollable(node: AccessibilityNodeInfo, down: Boolean, depth: Int): Boolean {
        if (depth > 30) return false
        if (node.isScrollable && node.isVisibleToUser) {
            val action = if (down) AccessibilityNodeInfo.ACTION_SCROLL_FORWARD else AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
            if (node.performAction(action)) return true
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val done = findScrollable(child, down, depth + 1)
            child.recycle()
            if (done) return true
        }
        return false
    }

    fun typeIntoFocusedField(value: String): Boolean {
        val root = rootInActiveWindow ?: return false
        return try {
            val focused = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT) ?: return false
            try {
                val args = Bundle().apply { putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, value) }
                focused.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
            } finally { focused.recycle() }
        } finally { root.recycle() }
    }
}
