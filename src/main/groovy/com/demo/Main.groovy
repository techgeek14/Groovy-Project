package com.demo

import com.codeborne.selenide.Configuration
import com.codeborne.selenide.WebDriverRunner
import org.openqa.selenium.PageLoadStrategy

import java.time.Duration

import static com.codeborne.selenide.Selenide.executeJavaScript
import static com.codeborne.selenide.Selenide.open
import static com.codeborne.selenide.Selenide.sleep


Configuration.holdBrowserOpen = true
Configuration.pageLoadStrategy = PageLoadStrategy.EAGER
//Configuration.timeout = 10000
open("https://www.atlassian.com/")
//WebDriverRunner.getWebDriver().manage().timeouts().pageLoadTimeout(Duration.ofMinutes(10));
def k = executeJavaScript("return document.getElementById('section-hero').style.cssText")
println "Inline Style: $k"
