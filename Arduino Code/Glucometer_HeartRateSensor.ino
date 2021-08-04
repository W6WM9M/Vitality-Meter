  #include <Wire.h>
#include "MAX30105.h"           //MAX3010x library
#include "heartRate.h"          //Heart rate calculating algorithm
#include <Adafruit_ADS1X15.h>   //ADC

MAX30105 particleSensor;
Adafruit_ADS1115 ads;  /* Use this for the 16-bit version */

//VARIABLES AND CONSTANTS FOR HEARTRATE SENSOR
#define RATE_SIZE 4 //Increase this for more averaging. 4 is good.
byte rates[RATE_SIZE]; //Array of heart rates
byte rateSpot = 0;
long lastBeat = 0; //Time at which the last beat occurred
float beatsPerMinute;
int beatAvg;


//VARIABLES AND CONSTANTS FOR GLUCOMETER
#define DURATION 5000 //Time to read the max volts2_value before sending
#define DIPPED_IN 100 //FS4_value when it is dipped in the solution
#define GLUCOCONTROL 2
#define MODE 3
#define RESET 4
#define GLUCO5V 5
#define FILL_SUFF A0
long start_time = 0;


//Set up for Bluetooth
#define INACTIVE 0
//Oximeter and Heart Rate Sensor
#define H_ON 1
//Glucometer
#define G_ON 2
#define START 3
#define RESET 4

boolean sufficient = 0;
//0: Blood, 1: Sweat
boolean isSweat = 0;
//Default State
byte state = INACTIVE;
String command;

//Variables and Constants for glucometer() function
#define AVG_SIZE 50
float start_voltages[AVG_SIZE];
int glucometer_i = 0;
float sum = 0;
float average = 0;
bool hasSentStartVoltage = false;
bool hasStartedTimer = false;

void setup() {
  pinMode(GLUCOCONTROL, OUTPUT);
  pinMode(MODE, OUTPUT);
  pinMode(RESET, OUTPUT);
  pinMode(GLUCO5V, OUTPUT);
  pinMode (FILL_SUFF, INPUT);
  ads.setGain(GAIN_ONE);
  ads.begin();  
  
  Serial.begin(115200);
  delay(1000);
  // Initialize sensor
  particleSensor.begin(Wire, I2C_SPEED_FAST); //Use default I2C port, 400kHz speed
  particleSensor.setup(); //Configure sensor with default settings
  particleSensor.setPulseAmplitudeRed(0x0A); //Turn Red LED to low to indicate sensor is running
  particleSensor.shutDown();
}
//
void loop(){
  if (Serial.available()){
    command = Serial.readString();
  }
  
  if (command == "H_off" || command == "O_off" || command == "G_off") {
    state = INACTIVE;
    command = "";
  }
  //When Heart Rate Sensor is On
  else if (command == "H_on"){
    particleSensor.wakeUp();     //Should only wake up once
    delay(1000);  //Delay to ensure that it is completely on
    state = H_ON;
    command = "";
  }

  //When Glucometer is On
  else if (command == "G_on") {
    state = G_ON;
    command = "";
  }
  //When mode is blood
  else if (command == "Blood") {
    isSweat = 0;
    command = "";
  }
  //When mode is sweat
  else if (command == "Sweat") {
    isSweat = 1;
    command = "";
  }
  //When user start their glucose reading
  else if (command == "start") {
    state = START;
    command = "";
  }
//  else if (command == "O_on") {
//    Serial.print("Error1");
//    command = "";
//  }
  else if (command == "file?") {
    Serial.print("file1");
    command = "";
  }
  else {
    command = "";
  }

  
  //INACTIVE: Turn everything down
  if (state == INACTIVE) {
    particleSensor.shutDown();
    digitalWrite(MODE, LOW);
    digitalWrite(GLUCO5V, LOW);
    digitalWrite(RESET, LOW);
    digitalWrite(GLUCOCONTROL, LOW);   
  }
  //Start Reading Heart Rate
  else if (state == H_ON) {
    getHeartRate();
  }
  //Click on CardView and send G_ON and set default on switch
  else if (state == G_ON) {
    digitalWrite(GLUCO5V, HIGH); //Turn ON Glucometer by supplying 5V
    digitalWrite(RESET, LOW); 
    digitalWrite(GLUCOCONTROL, HIGH);
    
    if (!isSweat){
      digitalWrite(MODE, HIGH);  //if mode is blood
    }
    else {
      digitalWrite(MODE, LOW);  //if mode is sweat
    }
  }
  else if (state == START) {

    if (detected_strip()){
      Serial.print(F("strip"));
      delay(100);
      if (glucometer()){
        delay(200);
        Serial.print(F("show"));
        state = G_ON;
        digitalWrite(reset,HIGH); //reset peak detector
        delay(1000);        //delay for capacitor discharge
      }
    }
  }
}


void getHeartRate() {
 long irValue = particleSensor.getIR();    //Reading the IR value it will permit us to know if there's a finger on the sensor or not
  //Serial.println(irValue);                                         //Also detecting a heartbeat
  if(irValue > 7000){                                           //If a finger is detected
    if (checkForBeat(irValue) == true)                        //If a heart beat is detected
    {                                  
//    Serial.print("BPM: ");
      Serial.print(beatAvg);
      
      
      long delta = millis() - lastBeat;                   //Measure duration between two beats
      lastBeat = millis();
  
      beatsPerMinute = 60 / (delta / 1000.0);           //Calculating the BPM
  
      if (beatsPerMinute < 255 && beatsPerMinute > 20)               //To calculate the average we strore some values (4) then do some math to calculate the average
      {
        rates[rateSpot++] = (byte)beatsPerMinute; //Store this reading in the array
        rateSpot %= RATE_SIZE; //Wrap variable
  
        //Take average of readings
        beatAvg = 0;
        for (byte x = 0 ; x < RATE_SIZE ; x++)
          beatAvg += rates[x];
        beatAvg /= RATE_SIZE;
      }
    }  
  }
  else{       //If no finger is detected it inform the user and put the average BPM to 0 or it will be stored for the next measure
    beatAvg=0;
    Serial.print(0);
    delay(250);
    return;
  }
}
bool detected_strip() {
  int i = 0;
  while (1) {
      if (Serial.available()){
        command = Serial.readString();
      }
      if (command == "G_off") {
        return false;
      }
      int FS4_value1 = analogRead(FILL_SUFF);
      delay(10);
      int FS4_value2 = analogRead(FILL_SUFF);
      if (abs(FS4_value2 - FS4_value1) <= 30){
        if (!isSweat && (FS4_value2 >= 430 && FS4_value2<=460)){
        //if ((FS4_value2 >= 500 && FS4_value2<=510) || (FS4_value2 >= 450 && FS4_value2 <= 460)){
          i++;
        }
       
        if (isSweat && (FS4_value2 >= 475 && FS4_value2 <= 510)) {
          //Serial.println(i);
          i++;
        }
      }
      else {
        i = 0;
      }
      if (i == 100){
        i=0;
        return true;
      }
  }
}

bool glucometer(){
  float volts0;
  int16_t adc0;
  long start_time;
  long duration = 0;
  byte k = 0;
  float volts0_max = 0;
  while (true) {
    if (Serial.available()){
      command = Serial.readString();
    }
    if (command == "G_off") {
      return false;
    }
    adc0 = ads.readADC_SingleEnded(0);
    volts0 = ads.computeVolts(ads.readADC_SingleEnded(0));
    

    if (!hasStartedTimer){
//      Serial.println(volts0);
      if (volts0 < 2.52) {
        if (glucometer_i<AVG_SIZE) {
          start_voltages[glucometer_i] = volts0;
          glucometer_i++;
          sum += volts0;
        }
        else {
          sum -= start_voltages[0];
          for (byte j = 1; j < AVG_SIZE; j++) {
            start_voltages[j-1] = start_voltages[j]; 
          }
          start_voltages[AVG_SIZE - 1] = volts0;
          sum += volts0;
          average = sum / AVG_SIZE; 
        }
      }
      else if (volts0 >= 2.52 && glucometer_i >= AVG_SIZE) {
        Serial.print(average,6);
        delay(100);
        start_time = millis();
        Serial.print(F("dipped"));
        hasStartedTimer = true;
      }
    }
    
    if (hasStartedTimer) {
      duration = millis() - start_time;
//      if (duration > 1000 && volts0 > volts0_max) {
//          //take max reading after 0.5s
//          volts0_max = volts0;
//      }
      if (duration > 2500 && isSweat) {
        Serial.print(volts0,6);
//        Serial.println(volts0_max, 6);
//        Serial.print((volts0 - average),6);
        //reset variables for subsequent reading
        hasStartedTimer = false;
        glucometer_i = 0;
        sum = 0;
        average = 0;
        return true;
      }
      else if (duration > 2000 && !isSweat) {
        Serial.print(volts0,6);
        //reset variables for subsequent reading
        hasStartedTimer = false;
        glucometer_i = 0;
        sum = 0;
        average = 0;
        return true;
      }
//      else if (k<3 && duration > 1500) {
//        Serial.println(volts0,6);
//        k++;
//      }
//      else if (k<2 && duration > 1000) {
//        Serial.println(volts0,6);
//        k++;
//      }
//      else if (k<1 && duration > 500) {
//        Serial.println(volts0,6);
//        k++;
//      }
    }
  }
}


void reset(){
  //FOR BLOOD RESET
  digitalWrite(reset, HIGH);
}
