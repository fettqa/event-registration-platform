package com.fettqa.events.mobile

import io.qameta.allure.Epic
import io.qameta.allure.Feature
import io.qameta.allure.Step
import org.junit.jupiter.api.Test
import java.util.UUID

@Epic("Mobile Appium")
@Feature("Register for event")
class RegisterForEventTest : AppiumBaseTest() {

    @Step("Create event via UI")
    private fun createEvent(name: String, seats: String) {
        tapId("createEventFab")
        typeId("nameInput", name)
        typeId("seatsInput", seats)
        tapId("submitButton")
        assertIdVisible("registerButton")
    }

    @Test
    fun register_for_event_success() {
        val suffix = UUID.randomUUID().toString().substring(0, 8)
        loginAsAdmin()
        createEvent("Appium Event $suffix", "20")
        tapId("registerButton")
        assertTextVisible("You are registered")
        assertIdVisible("registrationsRecycler")
    }

    @Test
    fun register_duplicate_shows_error() {
        val suffix = UUID.randomUUID().toString().substring(0, 8)
        loginAsAdmin()
        createEvent("Appium Dup $suffix", "5")
        tapId("registerButton")
        assertTextVisible("You are registered")
        tapId("registerButton")
        assertIdVisible("errorText")
    }
}
