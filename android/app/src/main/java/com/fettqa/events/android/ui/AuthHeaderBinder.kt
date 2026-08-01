package com.fettqa.events.android.ui

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.fettqa.events.android.R
import com.fettqa.events.android.data.AppServices
import com.google.android.material.button.MaterialButton

object AuthHeaderBinder {
    fun bind(activity: Activity, root: View, onLoggedOut: (() -> Unit)? = null) {
        val session = AppServices.session(activity)
        val guest = root.findViewById<LinearLayout>(R.id.guestActions)
        val userBlock = root.findViewById<LinearLayout>(R.id.userBlock)
        val fullName = root.findViewById<TextView>(R.id.headerFullName)
        val email = root.findViewById<TextView>(R.id.headerEmail)
        val role = root.findViewById<TextView>(R.id.headerRole)
        val loginBtn = root.findViewById<MaterialButton>(R.id.headerLoginButton)
        val registerBtn = root.findViewById<MaterialButton>(R.id.headerRegisterButton)
        val adminBtn = root.findViewById<MaterialButton>(R.id.headerAdminButton)
        val logoutBtn = root.findViewById<MaterialButton>(R.id.headerLogoutButton)

        session.hydrateFromToken()
        if (session.isLoggedIn()) {
            guest.visibility = View.GONE
            userBlock.visibility = View.VISIBLE
            fullName.text = session.fullName.orEmpty().ifBlank { "—" }
            email.text = session.effectiveEmail().orEmpty()
            role.text = activity.getString(
                R.string.role_label,
                session.effectiveRole().orEmpty().ifBlank { "—" },
            )
            adminBtn.visibility = if (session.isAdmin()) View.VISIBLE else View.GONE
            adminBtn.setOnClickListener {
                activity.startActivity(Intent(activity, AdminPanelActivity::class.java))
            }
            logoutBtn.setOnClickListener {
                session.clear()
                onLoggedOut?.invoke() ?: bind(activity, root, onLoggedOut)
            }
        } else {
            userBlock.visibility = View.GONE
            guest.visibility = View.VISIBLE
            loginBtn.setOnClickListener {
                activity.startActivity(Intent(activity, LoginActivity::class.java))
            }
            registerBtn.setOnClickListener {
                activity.startActivity(Intent(activity, RegisterActivity::class.java))
            }
        }
    }
}
