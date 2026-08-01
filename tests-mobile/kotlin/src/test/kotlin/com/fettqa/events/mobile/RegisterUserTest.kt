package com.fettqa.events.mobile

import io.appium.java_client.AppiumBy
import io.qameta.allure.Epic
import io.qameta.allure.Feature
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.openqa.selenium.TimeoutException
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration
import java.util.UUID

@Epic("Mobile Appium")
@Feature("Register")
class RegisterUserTest : AppiumBaseTest() {

    @Test
    fun register_user_reaches_events_without_create_fab() {
        val suffix = UUID.randomUUID().toString().substring(0, 8)
        tapId("headerRegisterButton")
        typeId("fullNameInput", "Mobile User $suffix")
        typeId("emailInput", "mobile_$suffix@example.com")
        typeId("passwordInput", "secret12")
        tapId("registerButton")

        assertTextVisible("Events")
        assertTextVisible("Role: USER")

        val shortWait = WebDriverWait(driver, Duration.ofSeconds(3))
        assertThrows(TimeoutException::class.java) {
            shortWait.until(
                ExpectedConditions.visibilityOfElementLocated(
                    AppiumBy.id("$APP_PACKAGE:id/createEventFab"),
                ),
            )
        }
    }
}
