
#include <Javino.h>
#include <DHT.h>

// Multi-purpose Shield V1
#define pinDHT 4
#define BlueLED 13
#define RedLED 12

Javino javino;
DHT dht(pinDHT, DHT11);

bool cleaning = false;
float temperature = 0.0;
bool dhtValid = false;

unsigned long lastDHTRead = 0;
const unsigned long DHT_INTERVAL = 2000;

void serialEvent() {
    javino.readSerial();
}

void togglePower() {
    cleaning = !cleaning;
    digitalWrite(BlueLED, cleaning ? HIGH : LOW);
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

void manageLeds() {
    digitalWrite(RedLED,
        dhtValid && temperature > 100.0 ? HIGH : LOW);
}

void getExogenousPerceptions() {
    readDHT();

    if (cleaning) {
        javino.addPercept("powerStatus(on)");
    } else {
        javino.addPercept("powerStatus(off)");
    }

    if (dhtValid) {
        javino.addPercept(
            "temperature(" + String(temperature, 2) + ")"
        );
    }
}

void setup() {
    pinMode(BlueLED, OUTPUT);
    pinMode(RedLED, OUTPUT);

    digitalWrite(BlueLED, LOW);
    digitalWrite(RedLED, LOW);

    dht.begin();

    // Javino Acts and Percepts
    javino.act["togglePower"] = togglePower;
    javino.perceive(getExogenousPerceptions);

    javino.start(9600);
}

void loop() {
    javino.run();
    readDHT();
    manageLeds();
}
