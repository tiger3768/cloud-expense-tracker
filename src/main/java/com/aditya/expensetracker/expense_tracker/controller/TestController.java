package com.aditya.expensetracker.expense_tracker.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController

public class TestController {

    @GetMapping("/api/test")
    public String test() {

        return "Protected API Working";
    }
}