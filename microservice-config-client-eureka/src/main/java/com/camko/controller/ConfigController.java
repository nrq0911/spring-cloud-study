package com.camko.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by nie on 17/5/24.
 */
@RestController
public class ConfigController {

    @Value("${test}")
    private String test;

    @GetMapping("/test")
    public String test(){
        return this.test;
    }

}
