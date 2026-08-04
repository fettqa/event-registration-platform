package com.fettqa.events.android.ui

import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.fettqa.events.android.R


abstract class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
    }

    override fun setContentView(view: View?) {
        super.setContentView(view)
        val root = view ?: return
        val toolbar = root.findViewById<View>(R.id.toolbar)
        if (toolbar != null) {
            applyStatusBarPaddingToToolbar(toolbar)
        } else {
            applySystemBarPaddingToRoot(root)
        }
    }

    private fun applyStatusBarPaddingToToolbar(toolbar: View) {
        val actionBarSize = actionBarSizePx()
        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { v, insets ->
            val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.updateLayoutParams<ViewGroup.LayoutParams> {
                height = actionBarSize + top
            }
            v.updatePadding(top = top)
            insets
        }
        ViewCompat.requestApplyInsets(toolbar)
    }

    private fun applySystemBarPaddingToRoot(root: View) {
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                left = bars.left,
                top = bars.top,
                right = bars.right,
                bottom = bars.bottom,
            )
            insets
        }
        ViewCompat.requestApplyInsets(root)
    }

    private fun actionBarSizePx(): Int {
        val tv = TypedValue()
        theme.resolveAttribute(androidx.appcompat.R.attr.actionBarSize, tv, true)
        return TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
    }
}
