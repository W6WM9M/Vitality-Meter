/*
  Optical SP02 Detection (SPK Algorithm) using the MAX30105 Breakout
  By: Nathan Seidle @ SparkFun Electronics
  Date: October 19th, 2016
  https://github.com/sparkfun/MAX30105_Breakout

  This demo shows heart rate and SPO2 levels.

  It is best to attach the sensor to your finger using a rubber band or other tightening 
  device. Humans are generally bad at applying constant pressure to a thing. When you 
  press your finger against the sensor it varies enough to cause the blood in your 
  finger to flow differently which causes the sensor readings to go wonky.

  This example is based on MAXREFDES117 and RD117_LILYPAD.ino from Maxim. Their example
  was modified to work with the SparkFun MAX30105 library and to compile under Arduino 1.6.11
  Please see license file for more info.

  Hardware Connections (Breakoutboard to Arduino):
  -5V = 5V (3.3V is allowed)
  -GND = GND
  -SDA = A4 (or SDA)
  -SCL = A5 (or SCL)
  -INT = Not connected
 
  The MAX30105 Breakout can handle 5V or 3.3V I2C logic. We recommend powering the board with 5V
  but it will also run at 3.3V.
*/

#include <Wire.h>
#include "MAX30105.h"
#include "spo2_algorithm.h"

MAX30105 particleSensor;


#define MAX_BRIGHTNESS 255

//To solve this problem, 16-bit MSB of the sampled data will be truncated. Samples become 16-bit data.

int32_t spo2; //SPO2 value
int8_t validSPO2; //indicator to show if the SPO2 calculation is valid
int32_t heartRate; //heart rate value
int8_t validHeartRate; //indicator to show if the heart rate calculation is valid

byte pulseLED = 11; //Must be on PWM pin
//byte readLED = 13; //Blinks with each data read
String command;
uint16_t irBuffer[100]; //infrared LED sensor data
uint16_t redBuffer[100];  //red LED sensor data

#define GLUCOCONTROL 2
#define MODE 3
#define RESET 4
#define GLUCO5V 5

bool oximeter_on = false;
void setup()
{

  pinMode(GLUCOCONTROL, OUTPUT);
  pinMode(MODE, OUTPUT);
  pinMode(RESET, OUTPUT);
  pinMode(GLUCO5V, OUTPUT);
  digitalWrite(GLUCOCONTROL, LOW);
  digitalWrite(MODE, LOW);
  digitalWrite(RESET, LOW);
  digitalWrite(GLUCO5V, LOW);
  
  pinMode(pulseLED, OUTPUT);
  particleSensor.begin(Wire, I2C_SPEED_FAST);

  byte ledBrightness = 60; //Options: 0=Off to 255=50mA
  byte sampleAverage = 4; //Options: 1, 2, 4, 8, 16, 32
  byte ledMode = 2; //Options: 1 = Red only, 2 = Red + IR, 3 = Red + IR + Green
  byte sampleRate = 100; //Options: 50, 100, 200, 400, 800, 1000, 1600, 3200
  int pulseWidth = 411; //Options: 69, 118, 215, 411
  int adcRange = 4096; //Options: 2048, 4096, 8192, 16384
  particleSensor.setup(ledBrightness, sampleAverage, ledMode, sampleRate, pulseWidth, adcRange); //Configure sensor with these settings
  
  particleSensor.shutDown();
  Serial.begin(115200); // initialize serial communication at 115200 bits per second:

  
}

void loop()
{   
    if (Serial.available()){
      command = Serial.readString();
    }

    if (command == "O_on"){
      particleSensor.wakeUp();
      delay(1000);
      oximeter_on = true;
      command == "";
    }
    else if (command == "O_off") {
      particleSensor.shutDown();
      oximeter_on = false;
    }
    else if (command == "file?") {
      Serial.print(F("file2"));
      command = "";
    }

    if (oximeter_on) {
      oximeter();
    }
}

void oximeter() {
  //calculate heart rate and SpO2 after first 100 samples (first 4 seconds of samples)
  
  maxim_heart_rate_and_oxygen_saturation(irBuffer, 100, redBuffer, &spo2, &validSPO2, &heartRate, &validHeartRate);

  //Continuously taking samples from MAX30102.  Heart rate and SpO2 are calculated every 1 second
  while (1)
  { 
    if (Serial.available()){
      command = Serial.readString();
      if (command == "O_off") {
        return;
      }
    }

    //------Check CMD here-------//
    //dumping the first 25 sets of samples in the memory and shift the last 75 sets of samples to the top
    for (byte i = 25; i < 100; i++)
    {
      redBuffer[i - 25] = redBuffer[i];
      irBuffer[i - 25] = irBuffer[i];
    }

    
    //take 25 sets of samples before calculating the heart rate.
    for (byte i = 75; i < 100; i++)
    {
      while (particleSensor.available() == false) //do we have new data?
        particleSensor.check(); //Check the sensor for new data
      
      redBuffer[i] = particleSensor.getRed();
      irBuffer[i] = particleSensor.getIR();
      particleSensor.nextSample(); //We're finished with this sample so move to next sample
     
    }
        Serial.print(spo2, DEC);

    //After gathering 25 new samples recalculate HR and SP02
    maxim_heart_rate_and_oxygen_saturation(irBuffer, 100, redBuffer, &spo2, &validSPO2, &heartRate, &validHeartRate);
    return;
  }
}
