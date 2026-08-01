package com.fettqa.events.mobile

import io.qameta.allure.Epic
import io.qameta.allure.Feature
import org.junit.jupiter.api.Test

@Epic("Mobile Appium")
@Feature("Login")
class LoginTest : AppiumBaseTest() {

    @Test
    fun login_admin_shows_admin_controls() {
        loginAsAdmin()
        assertIdVisible("headerAdminButton")
        assertIdVisible("createEventFab")
    }

    @Test
    fun login_bad_password_shows_error() {
        tapId("headerLoginButton")
        typeId("emailInput", "admin@example.com")
        typeId("passwordInput", "wrong-password")
        tapId("loginButton")
        assertIdVisible("errorText")
        assertTextVisible("bad credentials")
    }
}
