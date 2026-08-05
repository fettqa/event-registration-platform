package com.fettqa.events.android.ui

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.fettqa.events.android.R
import com.fettqa.events.android.data.AppServices
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class EventListActivityTest {
    @Before
    fun clearSession() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        AppServices.resetForTests()
        AppServices.session(context).clear()
    }

    @Test
    fun guest_sees_events_title_and_login() {
        ActivityScenario.launch(EventListActivity::class.java).use {
            onView(withText(R.string.events_title)).check(matches(isDisplayed()))
            onView(withId(R.id.headerLoginButton)).check(matches(isDisplayed()))
            onView(withId(R.id.eventsRecycler)).check(matches(isDisplayed()))
        }
    }
}
