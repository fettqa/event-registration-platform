package com.fettqa.events.e2e.base;

import com.fettqa.events.e2e.preconditions.Precondition;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

public abstract class PlaywrightBaseTest<T extends Precondition> {

  protected static Playwright playwright;
  protected static Browser browser;
  protected static APIRequestContext api;

  protected Page page;
  protected T precondition;

  protected static final String baseUrl =
      System.getProperty("baseUrl", "http://localhost:8080");

  @BeforeAll
  static void launchBrowser() {
    playwright = Playwright.create();
    browser = playwright.chromium().launch(
        new BrowserType.LaunchOptions().setHeadless(true));
    api = playwright.request().newContext(
        new APIRequest.NewContextOptions().setBaseURL(baseUrl));
  }

  @AfterAll
  static void closeBrowser() {
    if (api != null) {
      api.dispose();
    }
    if (browser != null) {
      browser.close();
    }
    if (playwright != null) {
      playwright.close();
    }
  }

  @BeforeEach
  void openPage() {
    page = browser.newPage();
    precondition = createPreconditionInstance();
    precondition.setApi(api);
  }

  @AfterEach
  void closePage() {
    if (page != null) {
      page.close();
    }
  }

  @SuppressWarnings("unchecked")
  private T createPreconditionInstance() {
    Class<?> clazz = getClass();
    while (clazz != Object.class) {
      Type genericSuperclass = clazz.getGenericSuperclass();
      if (genericSuperclass instanceof ParameterizedType parameterized) {
        Type typeArg = parameterized.getActualTypeArguments()[0];
        if (typeArg instanceof Class<?> preconditionClass) {
          try {
            return ((Class<T>) preconditionClass).getDeclaredConstructor().newInstance();
          } catch (ReflectiveOperationException e) {
            throw new RuntimeException(
                "Failed to create precondition (needs public no-arg ctor): "
                    + preconditionClass.getName(),
                e);
          }
        }
      }
      clazz = clazz.getSuperclass();
    }
    throw new IllegalStateException(
        "No precondition type parameter found on " + getClass().getName());
  }
}
