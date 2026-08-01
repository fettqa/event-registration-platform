package com.fettqa.events.mobile

import io.qameta.allure.Epic
import io.qameta.allure.Feature
import org.junit.jupiter.api.Test

@Epic("Mobile Appium")
@Feature("Guest")
class GuestEventsTest : AppiumBaseTest() {

    @Test
    fun guest_sees_events_list() {
        assertTextVisible("Events")
        assertIdVisible("headerLoginButton")
        assertIdVisible("headerRegisterButton")
        assertIdVisible("eventsSearchInput")
        assertIdVisible("eventsRecycler")
    }
}
