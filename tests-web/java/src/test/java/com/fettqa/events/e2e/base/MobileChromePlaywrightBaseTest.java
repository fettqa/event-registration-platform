package com.fettqa.events.e2e.base;

import com.fettqa.events.e2e.preconditions.Precondition;
import com.microsoft.playwright.Browser;


public abstract class MobileChromePlaywrightBaseTest extends PlaywrightBaseTest<Precondition> {

  private static final String PIXEL_7_USER_AGENT =
      "Mozilla/5.0 (Linux; Android 14; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) "
          + "Chrome/120.0.0.0 Mobile Safari/537.36";

  @Override
  protected Browser.NewContextOptions newContextOptions() {
    return new Browser.NewContextOptions()
        .setUserAgent(PIXEL_7_USER_AGENT)
        .setViewportSize(412, 915)
        .setDeviceScaleFactor(2.625)
        .setIsMobile(true)
        .setHasTouch(true);
  }
}
