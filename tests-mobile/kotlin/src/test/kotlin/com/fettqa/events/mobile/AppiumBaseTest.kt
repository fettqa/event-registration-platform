package com.fettqa.events.mobile

import io.appium.java_client.AppiumBy
import io.appium.java_client.android.AndroidDriver
import io.appium.java_client.android.options.UiAutomator2Options
import io.qameta.allure.Step
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration

abstract class AppiumBaseTest {

    protected lateinit var driver: AndroidDriver
    protected lateinit var wait: WebDriverWait

    @BeforeEach
    fun setUpDriver() {
        val options = UiAutomator2Options()
            .setPlatformName("Android")
            .setAutomationName("UiAutomator2")
            .setAppPackage(APP_PACKAGE)
            .setAppActivity(APP_ACTIVITY)
            .setNoReset(false)
            .setNewCommandTimeout(Duration.ofSeconds(120))

        var apk = System.getProperty("apk")
        if (apk.isNullOrBlank()) {
            val defaultApk = Path.of("..", "..", "android", "app", "build", "outputs", "apk", "debug", "app-debug.apk")
                .toAbsolutePath()
                .normalize()
            if (Files.isRegularFile(defaultApk)) {
                apk = defaultApk.toString()
            }
        }
        if (!apk.isNullOrBlank() && Files.isRegularFile(Path.of(apk))) {
            options.setApp(apk)
        }

        val appiumUrl = System.getProperty("appiumUrl", "http://127.0.0.1:4723")
        driver = AndroidDriver(URI.create(appiumUrl).toURL(), options)
        wait = WebDriverWait(driver, Duration.ofSeconds(15))
    }

    @AfterEach
    fun tearDownDriver() {
        if (::driver.isInitialized) {
            driver.quit()
        }
    }

    protected fun id(resource: String): By =
        AppiumBy.id("$APP_PACKAGE:id/$resource")

    @Step("Tap id={resource}")
    protected fun tapId(resource: String) {
        wait.until(ExpectedConditions.elementToBeClickable(id(resource))).click()
    }

    @Step("Type into id={resource}")
    protected fun typeId(resource: String, text: String) {
        val el = wait.until(ExpectedConditions.visibilityOfElementLocated(id(resource)))
        el.clear()
        el.sendKeys(text)
    }

    @Step("Assert id visible: {resource}")
    protected fun assertIdVisible(resource: String) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(id(resource)))
    }

    @Step("Assert text visible: {text}")
    protected fun assertTextVisible(text: String) {
        wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                AppiumBy.androidUIAutomator("""new UiSelector().textContains("$text")"""),
            ),
        )
    }

    @Step("Login as admin")
    protected fun loginAsAdmin() {
        tapId("headerLoginButton")
        typeId("emailInput", "admin@example.com")
        typeId("passwordInput", "admin123")
        tapId("loginButton")
        assertTextVisible("admin@example.com")
        assertTextVisible("Role: ADMIN")
    }

    companion object {
        const val APP_PACKAGE = "com.fettqa.events.android"
        const val APP_ACTIVITY = ".ui.EventListActivity"
    }
}
