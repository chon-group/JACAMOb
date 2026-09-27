const byte Led = 13;

String msg = "";

void setup(){
    pinMode(Led, OUTPUT);
    Serial.begin(9600);
}

void loop(){
    readSerialPort();
}

void readSerialPort(){
    while (Serial.available()){
        char c = Serial.read();
        if (c == '\n') {
            processMsg(msg);
            msg = "";
        }
        else if (c != '\r') {
            msg += c;
        }
    }
}

void processMsg(String cmd){
    cmd.trim();
    if (cmd == "getPercepts") getPercepts();
}

void getPercepts(){
    digitalWrite(Led, !digitalRead(Led));

    Serial.print("locomotion(");
    Serial.print(digitalRead(Led));
    Serial.println(");");
}