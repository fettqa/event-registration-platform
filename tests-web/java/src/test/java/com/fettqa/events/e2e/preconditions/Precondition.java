package com.fettqa.events.e2e.preconditions;

import com.microsoft.playwright.APIRequestContext;

public class Precondition implements IPrecondition {

  protected APIRequestContext api;

  public Precondition() {
  }

  public void setApi(APIRequestContext api) {
    this.api = api;
  }
}
