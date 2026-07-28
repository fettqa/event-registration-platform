package com.fettqa.events.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminPageController {

  @GetMapping("/adminPanel")
  public String adminPanel() {
    return "admin/panel";
  }
}
