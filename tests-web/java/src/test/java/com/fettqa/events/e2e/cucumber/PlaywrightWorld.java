package com.fettqa.events.e2e.cucumber;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

/**
 * PicoContainer-scoped state for one Cucumber scenario (one browser page).
 */
public class PlaywrightWorld implements AutoCloseable {

  private static final Object LOCK = new Object();
  private static Playwright playwright;
  private static Browser browser;

  private final String baseUrl = System.getProperty("baseUrl", "http://localhost:8080");
  private Page page;

  private String eventName;
  private String userEmail;
  private String userFullName;

  public String baseUrl() {
    return baseUrl;
  }

  public Page page() {
    if (page == null) {
      ensureBrowser();
      page = browser.newPage();
    }
    return page;
  }

  /** Existing page or null (does not open a new tab). */
  public Page currentPageOrNull() {
    return page;
  }

  public void closePage() {
    if (page != null) {
      page.close();
      page = null;
    }
  }

  public String eventName() {
    return eventName;
  }

  public void setEventName(String eventName) {
    this.eventName = eventName;
  }

  public String userEmail() {
    return userEmail;
  }

  public void setUserEmail(String userEmail) {
    this.userEmail = userEmail;
  }

  public String userFullName() {
    return userFullName;
  }

  public void setUserFullName(String userFullName) {
    this.userFullName = userFullName;
  }

  private static void ensureBrowser() {
    synchronized (LOCK) {
      if (playwright == null) {
        playwright = Playwright.create();
        browser =
            playwright
                .chromium()
                .launch(new BrowserType.LaunchOptions().setHeadless(true));
      }
    }
  }

  /** Called from JVM shutdown if needed; pages are closed per scenario in Hooks. */
  @Override
  public void close() {
    closePage();
  }
}
