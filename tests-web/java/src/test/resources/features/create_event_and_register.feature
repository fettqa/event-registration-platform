Feature: Create event and register
  As an admin
  I want to create an event and register for it
  So that I appear in the registrations list

  @allure.label.layer:e2e
  @allure.label.framework:cucumber
  Scenario: Admin creates an event and registers for it
    Given I am logged in as admin
    When I open the events list
    And I create an event with 25 seats
    And I register for the event
    Then I see registration success for admin

