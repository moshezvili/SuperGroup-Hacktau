    /*
 == MASTER CODE ==
*/
#include <SoftwareSerial.h>

SoftwareSerial BTSerial(10, 11); // RX | TX
#define ledPin 9
int state = 0;

// Pulse Monitor Test Script
int sensorPin = 0;
double alpha = 0.75;
int period = 100;
double change = 0.0;
double minval = 0.0;
unsigned long time;


void setup() {
  Serial.begin(9600);
 pinMode(ledPin, OUTPUT);
 digitalWrite(ledPin, LOW);
 BTSerial.begin(38400); // HC-05 default speed in AT command more
}
void loop() {
if(BTSerial.available() > 0){ // Checks whether data is comming from the serial port
 state = BTSerial.read(); // Reads the data from the serial port
}
static double oldValue = 0;
static double oldChange = 0;

int rawValue = analogRead (sensorPin);
double value = alpha * oldValue + (1 - alpha) * rawValue;
time = millis();
Serial.print (time);
Serial.print (",");
Serial.println (value);
BTSerial.write(time+','+value);
}
