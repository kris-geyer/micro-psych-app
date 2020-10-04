#include <SoftwareSerial.h>
#include <TinyGPS++.h>
#include <SPI.h>
#include <SD.h>

SoftwareSerial gpsSerial(3, 4); //(tx, rx)
TinyGPSPlus gps;
double latitude,longitude;
long data;

String nameOfFile;

String msg = "";
bool phoneActive = true;
long alarmTime; 
long startAgainAt;

bool initializeSD = true;
void setup() {
  gpsSerial.begin(9600);
  waitForSerial();
  if (initializeSD){
    initializeSDCard();
    initializeSD = false;  
  }
  nameOfFile = "l.txt";
  Serial.println("Setup starting");
  // put your setup code here, to run once:
  alarmTime = millis();
  startAgainAt = millis();
}

void loop() {
  //check if there is a message recieved
  if(Serial.available()){
   phoneActive = true;
   msg += char(Serial.read());
   readMessageFromPhone(msg);
   msg = "";
  }  

  //if phone is active
  if (phoneActive){
    //check that a long period has occurred before last transmission from phone
    checkLengthyInactive();
  }else{
    recordGPS();
  }
}

void recordGPS(){
  if (millis() >= startAgainAt){
    
    while (gpsSerial.available()){
      data = gpsSerial.read();
      if (gps.encode(data)>0){
        if (gps.location.isUpdated() != true){
          break;
        }
        latitude =  (gps.location.lat());
        longitude = (gps.location.lng());
        if (latitude != 0){
          recordGPS(latitude, longitude, gps.time.value());
          startAgainAt = (millis() + 15000);     
        }
      }
    }
  }
}

void checkLengthyInactive(){
  if((alarmTime + 5000) < millis()){
    Serial.println("Start logging GPS");
    phoneActive = false;
      //start logging GPS data
    }
}

void recordGPS(float latitude, float longitude, long timestamp){
  String d = "lat: ";
  d += String(latitude,6);
  d += "; long: ";
  d += String(longitude,6);
  d += "; time: ";
  d += timestamp;
  writeData(d, nameOfFile);
  readData(nameOfFile);
}

// wait for serial port to connect. Needed for native USB port only
void waitForSerial(){
  Serial.begin(9600);
  while (!Serial) {
  ; 
  }
}

void initializeSDCard(){
  Serial.print("Initializing SD card...");
  if (!SD.begin(10)) {
    Serial.println("initialization failed!");
    while (1);
  }
  Serial.println("initialization done.");
}

void writeData(String data, String fileName){
  File file = SD.open(fileName, FILE_WRITE);
  if (file) {
    file.print(data);
    file.print(",");
    file.close();
  }else{
    Serial.println("Could not write data");
    initializeSDCard();
  }
  
}

void readData(String fileName){
  File file = SD.open(fileName);
  if (file) {
    Serial.print(fileName);
    Serial.println(" data:");
    
    while (file.available()) {
      Serial.write(file.read());
    }
    // close the file:
    file.close();
  }else{
      Serial.println("Could not read data");
     }
}

bool deleteFile(String fileName){
  bool didDelete = SD.remove(fileName);
  bool result = SD.exists(fileName);
  Serial.println("File deletition result: ");
  Serial.println(result);
  return result;
}

void readMessageFromPhone(String msg){
  if (msg == "h"){
    alarmTime = millis();
    Serial.println("Stop logging GPS");
  } else if (msg == "e"){
    deleteFile("location.txt");
    deleteFile("l.txt");
  }
  Serial.print("Message: ");
  Serial.println(msg);
}
