#include <Arduino.h>
#include <Servo.h>

const uint8_t       SERVO_PIN           = 7;
const uint8_t       RED_LED_PIN         = 12;
const uint8_t       BLUE_LED_PIN        = 13;
const uint8_t       BUZZER_PIN          = 5;
const int           REST_POSITION       = 90;
const int           LEFT_POSITION       = 0;
const int           RIGHT_POSITION      = 180;
const unsigned long STEP_DELAY          = 5;
const unsigned long SETTLE_DELAY        = 100;
const unsigned long BOOT_DELAY          = 500;  
const unsigned int  BUZZER_FREQUENCY    = 1000;

static Servo motor;

void setupArm() {
    const uint8_t pins[] = {
        RED_LED_PIN, BLUE_LED_PIN,
        BUZZER_PIN, 9, 10, 11
    };

    for (uint8_t i = 0; i < sizeof(pins); ++i) {
        pinMode(pins[i], OUTPUT);
        digitalWrite(pins[i], LOW);
    }

    motor.attach(SERVO_PIN);
    motor.write(REST_POSITION);
    delay(BOOT_DELAY);
}

void move(int target, uint8_t led) {
    digitalWrite(RED_LED_PIN, LOW);
    digitalWrite(BLUE_LED_PIN, LOW);
    digitalWrite(led, HIGH);

    tone(BUZZER_PIN, BUZZER_FREQUENCY);

    const int direction = target < REST_POSITION ? -1 : 1;

    for (int p = REST_POSITION; p != target;) {
        p += direction;
        motor.write(p);
        delay(STEP_DELAY);
    }

    for (int p = target; p != REST_POSITION;) {
        p -= direction;
        motor.write(p);
        delay(STEP_DELAY);
    }

    delay(SETTLE_DELAY);

    noTone(BUZZER_PIN);
    digitalWrite(BUZZER_PIN, LOW);
    digitalWrite(led, LOW);
}

void moveLeft()  { move(LEFT_POSITION,  RED_LED_PIN);  }
void moveRight() { move(RIGHT_POSITION, BLUE_LED_PIN); }