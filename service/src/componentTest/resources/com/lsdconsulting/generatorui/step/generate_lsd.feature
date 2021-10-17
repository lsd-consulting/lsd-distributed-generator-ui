Feature: Lsd generator

  Scenario: Generate an diagram
    Given existing captured interactions in the database
    When an lsd request is received
    Then the correct diagram is generated
