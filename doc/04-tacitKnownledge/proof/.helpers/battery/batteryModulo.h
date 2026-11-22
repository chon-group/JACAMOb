#ifndef POWER_MODULE_H
#define POWER_MODULE_H

#include <Arduino.h>
#include <LiquidCrystal.h>

namespace PowerModule {

#ifdef POWER_MODULE_SIMULIDE
    constexpr uint8_t LCD_D4 = 5, LCD_D5 = 4;
    constexpr uint8_t LCD_D6 = 3, LCD_D7 = 2;
    constexpr uint8_t LCD_RS = 11, LCD_EN = 10;
    constexpr uint8_t LCD_LIGHT = 12;
    constexpr uint8_t LED_RED = 7, LED_YELLOW = 8, LED_GREEN = 9;
#else
    constexpr uint8_t LCD_D4 = 4, LCD_D5 = 5;
    constexpr uint8_t LCD_D6 = 6, LCD_D7 = 7;
    constexpr uint8_t LCD_RS = 8, LCD_EN = 9;
    constexpr uint8_t LCD_LIGHT = 10;
    constexpr uint8_t LED_RED = 11, LED_YELLOW = 12, LED_GREEN = 13;
#endif

    constexpr int BATTERY_LOW = 15;
    constexpr int BATTERY_CRITICAL = 5;
    constexpr unsigned long DEBOUNCE_MS = 250;

    static LiquidCrystal lcd(LCD_RS, LCD_EN,
                            LCD_D4, LCD_D5, LCD_D6, LCD_D7);
    static int batteryLevel = 100;
    static unsigned long lastClick = 0;
    static String lastButton = "none";

    inline void updateLeds() {
        digitalWrite(LED_GREEN, batteryLevel > BATTERY_LOW ? HIGH : LOW);
        digitalWrite(LED_YELLOW,
            batteryLevel > BATTERY_CRITICAL && batteryLevel <= BATTERY_LOW
                ? HIGH : LOW);
        digitalWrite(LED_RED, batteryLevel <= BATTERY_CRITICAL ? HIGH : LOW);
    }

    inline void updateLCD() {
        lcd.clear();
        lcd.setCursor(0, 0);
        lcd.print("Rosie Battery");
        lcd.setCursor(0, 1);
        lcd.print(batteryLevel);
        lcd.print("% ");

        if (batteryLevel <= BATTERY_CRITICAL)
            lcd.print("CRITICAL");
        else if (batteryLevel <= BATTERY_LOW)
            lcd.print("LOW");
        else
            lcd.print("NORMAL");

        updateLeds();
    }

    inline String getBtnPressed() {
        int input = analogRead(A0);
        if (input < 50)  return "right";
        if (input < 150) return "up";
        if (input < 350) return "down";
        if (input < 550) return "left";
        if (input < 750) return "select";
        return "none";
    }

    inline void updateBattery() {
        String button = getBtnPressed();
        unsigned long now = millis();

        if (button != "none" && lastButton == "none"
                && now - lastClick >= DEBOUNCE_MS) {
            lastClick = now;

            if (button == "right")       batteryLevel += 1;
            else if (button == "left")   batteryLevel -= 1;
            else if (button == "up")     batteryLevel += 5;
            else if (button == "down")   batteryLevel -= 5;
            else if (button == "select") batteryLevel = 100;

            batteryLevel = constrain(batteryLevel, 0, 100);
            updateLCD();
        }
        lastButton = button;
    }

    inline int getBatteryLevel() {
        return batteryLevel;
    }

    inline void setupBattery() {
        pinMode(LED_RED, OUTPUT);
        pinMode(LED_YELLOW, OUTPUT);
        pinMode(LED_GREEN, OUTPUT);
        pinMode(LCD_LIGHT, OUTPUT);
        digitalWrite(LCD_LIGHT, HIGH);
        lcd.begin(16, 2);
        updateLCD();
    }
}

#endif
