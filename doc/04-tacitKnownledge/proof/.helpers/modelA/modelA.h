#include <Arduino.h>
#include <Servo.h>
static Servo motor;

const uint8_t       SERVO_PIN       = 5;
const uint8_t       LED_PIN         = 13;
const uint8_t       BUZZER_PIN      = 3;
const uint8_t       DISPLAY_LATCH   = 4;
const uint8_t       DISPLAY_CLOCK   = 7;
const uint8_t       DISPLAY_DATA    = 8;
const int           STOP_US         = 1500;  
const int           SPEED_US        = 200;   
const unsigned long SPIN_TIME_MS    = 1000;  
const unsigned long SETTLE_DELAY    = 200;

enum Direction : int8_t { CCW = -1, CW = 1 }; 

static Direction nextDirection = CW;          

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

void stopMotor() {
 motor.writeMicroseconds(STOP_US);
 delay(SETTLE_DELAY);
 motor.detach();
}

void spin(Direction dir, unsigned long durationMs) {
 if (!motor.attached()) motor.attach(SERVO_PIN);
 motor.writeMicroseconds(STOP_US + dir * SPEED_US);
 delay(durationMs);
 stopMotor();
}

void setupArm() {
 clearDisplay();
 pinMode(LED_PIN, OUTPUT);
 pinMode(BUZZER_PIN, OUTPUT);
 setFeedback(false);
}

void togglePower() {
 setFeedback(true);
 spin(nextDirection, SPIN_TIME_MS);
 nextDirection = (nextDirection == CW) ? CCW : CW; 
 setFeedback(false);
}