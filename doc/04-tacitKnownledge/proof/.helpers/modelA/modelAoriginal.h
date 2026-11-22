#include <Arduino.h>
#include <Servo.h>
static Servo motor;

const uint8_t       SERVO_PIN       = 5;
const uint8_t       LED_PIN         = 13;
const uint8_t       BUZZER_PIN      = 3;
const int           REST_POSITION   = 0;
const int           END_POSITION    = 180;
const unsigned long STEP_DELAY      = 5;
const unsigned long SETTLE_DELAY    = 200;
const uint8_t       DISPLAY_LATCH   = 4;
const uint8_t       DISPLAY_CLOCK   = 7;
const uint8_t       DISPLAY_DATA    = 8;

void clearDisplay() {
 pinMode(DISPLAY_LATCH, OUTPUT);
 pinMode(DISPLAY_CLOCK, OUTPUT);
 pinMode(DISPLAY_DATA, OUTPUT);

 digitalWrite(DISPLAY_LATCH, LOW);
 shiftOut(DISPLAY_DATA, DISPLAY_CLOCK, MSBFIRST, 0xFF);
 shiftOut(DISPLAY_DATA, DISPLAY_CLOCK, MSBFIRST, 0x00);
 digitalWrite(DISPLAY_LATCH, HIGH);
}

void setFeedback(bool enabled) {
 digitalWrite(LED_PIN, enabled ? LOW : HIGH);
 digitalWrite(BUZZER_PIN, enabled ? LOW : HIGH);
}

void setupArm() {
 clearDisplay();
 pinMode(LED_PIN, OUTPUT);   pinMode(BUZZER_PIN, OUTPUT);
 motor.write(REST_POSITION); motor.attach(SERVO_PIN);
}

void togglePower() {
 setFeedback(true);   
 for (int position = REST_POSITION + 1; position <= END_POSITION; position++) {
    motor.write(position);
    delay(STEP_DELAY);
 }
 for (int position = END_POSITION - 1; position >= REST_POSITION; position--) {
    motor.write(position);
    delay(STEP_DELAY);
 }
 delay(SETTLE_DELAY);
 setFeedback(false);
}