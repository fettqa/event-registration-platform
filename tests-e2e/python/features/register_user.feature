Feature: Register user
  As a visitor
  I want to create an account
  So that the header shows my identity

  Scenario: New user sees their info in the header
    When I register a new user via the UI
    Then the header shows me as a USER
