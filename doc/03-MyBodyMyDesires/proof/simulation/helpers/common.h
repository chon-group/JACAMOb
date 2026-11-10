#ifndef COMMON_H
#define COMMON_H
#include <LiquidCrystal.h>

// ===== Pinagem LCD =====
#define PinLCDCtrl01  5
#define PinLCDCtrl02  4
#define PinLCDCtrl03  3
#define PinLCDCtrl04  2
#define PinLCDRS     11
#define PinLCDEN     10
#define PinLCDLight  12

// ===== LEDs =====
#define LedRed         7
#define LedYellow      8
#define LedGreen       9

// ===== Bateria =====
#define BATTERY_INITIAL  100.0f
#define BATTERY_LOW       15.0f
#define BATTERY_CRITICAL   5.0f

#define DISCHARGE_MS      1000
#define DISCHARGE_AMOUNT 0.164f

float batteryLevel = BATTERY_INITIAL;
unsigned long lastDischarge = 0;

LiquidCrystal lcd(PinLCDRS, PinLCDEN, PinLCDCtrl01,
    PinLCDCtrl02, PinLCDCtrl03, PinLCDCtrl04);

// ===== Protótipos =====
void configBody();
void updateBattery();
void updateDisplay();
float getBatteryLevel();

// ===== Inicialização =====
void configBody() {
    pinMode(LedRed, OUTPUT); pinMode(LedYellow, OUTPUT);
    pinMode(LedGreen, OUTPUT); pinMode(PinLCDLight, OUTPUT);
    digitalWrite(PinLCDLight, HIGH);
    lcd.begin(16, 2);
    lastDischarge = millis();
    updateDisplay();
}

void updateBattery() {
    unsigned long now = millis();
    if (now - lastDischarge >= DISCHARGE_MS) {
        unsigned long cycles = (now - lastDischarge) / DISCHARGE_MS;
        lastDischarge += cycles * DISCHARGE_MS;
        batteryLevel -= cycles * DISCHARGE_AMOUNT;
        if (batteryLevel < 0.0f){batteryLevel = 0.0f;}
        updateDisplay();
    }
}

float getBatteryLevel() {return batteryLevel;}

void updateDisplay() {
    bool critical = batteryLevel <= BATTERY_CRITICAL;
    bool low = batteryLevel <= BATTERY_LOW;

    digitalWrite(LedRed, critical ? HIGH : LOW);
    digitalWrite(LedYellow, (low && !critical) ? HIGH : LOW);
    digitalWrite(LedGreen, !low ? HIGH : LOW);

    lcd.setCursor(0, 0);
    lcd.print("Battery: ");
    lcd.print((int)batteryLevel);
    lcd.print("%    ");

    lcd.setCursor(0, 1);
    if (critical){lcd.print("Status: CRITICAL");}
    else if (low){lcd.print("Status: LOW     ");}
    else{lcd.print("Status: NORMAL  ");}
}

#endif