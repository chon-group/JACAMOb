
#include <Javino.h>
#include <DHT.h>

// Multi-purpose Shield V1
#define pinDHT 4

// RGB LED
#define RedLED   9
#define BlueLED  10
#define GreenLED 11

Javino javino;
DHT dht(pinDHT, DHT11);

String direction = "stop";

float temperature = 0.0;
bool dhtValid = false;

unsigned long lastDHTRead = 0;
const unsigned long DHT_INTERVAL = 2000;

void serialEvent() {
    javino.readSerial();
}

void setRGB(bool red, bool green, bool blue) {
    digitalWrite(RedLED, red ? HIGH : LOW);
    digitalWrite(GreenLED, green ? HIGH : LOW);
    digitalWrite(BlueLED, blue ? HIGH : LOW);
}

void left() {
    direction = "left";
    setRGB(false, false, true);
}

void right() {
    direction = "right";
    setRGB(false, true, false);
}

void stop() {
    direction = "stop";
    setRGB(false, false, false);
}

void readDHT() {
    unsigned long now = millis();

    if (now - lastDHTRead < DHT_INTERVAL) return;

    lastDHTRead = now;

    float temp = dht.readTemperature();
    float humi = dht.readHumidity();

    if (isnan(temp) || isnan(humi)) return;

    temperature = temp;
    dhtValid = true;
}

void getExogenousPerceptions() {
    readDHT();

    javino.addPercept(
        "direction(" + direction + ")"
    );

    if (dhtValid) {
        javino.addPercept(
            "temperature(" + String(temperature, 2) + ")"
        );
    }
}

void setup() {
    pinMode(RedLED, OUTPUT);
    pinMode(GreenLED, OUTPUT);
    pinMode(BlueLED, OUTPUT);

    setRGB(false, false, false);

    dht.begin();

    // Javino Acts and Percepts
    javino.act["left"] = left;
    javino.act["right"] = right;
    javino.act["stop"] = stop;

    javino.perceive(getExogenousPerceptions);

    javino.start(9600);
}

void loop() {
    javino.run();
    readDHT();
}
