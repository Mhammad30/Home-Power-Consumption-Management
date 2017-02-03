//==================================================== ESP8266 DevKit Takes Current Samples Through A0 (its ADC), and integrates these Samples to obtain irms, 
//==================================================== then power consumption will (irms * vrms *power factor) 
//==================================================== By connection with a local server, ESP8266 sends power consumption values to cluod (both sparkfun and thingspeak)
//==================================================== Each time an android HOMEPOWERMANAGEMENT application wants to be ESP8266's client, ESP8266 go to serve it then return ============
#include <ESP8266WiFi.h>
#define RelayPin 5
WiFiServer server(80);
const char *ssid = "Plug001";
const char *password = "12345678";
//const char* ssid1 = "mylen";          "IKIU"                  //===================== Router (WiFi) parameters to be configured
//const char* password1 = "12345@ml";   ""
const char* ssid1 ="aaa";                                    //====================== Router (WiFi) parameters to be configured
const char* password1 ="123@45678" ;   
//const char* ssid1 ="IKIU";                                 //======================== Router (WiFi) parameters to be configured
//const char* password1 ="" ;    
//=========================================
String TurnOff;
String TurnOn;
String Moment;                                              //======================== I made the matrix 62 elements, with initializing zeros for (i=1-->60) for fill up condition to work well
int ConsumptionArray [62]= {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1};   
int CopyArray [60]= {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19}; 
int oneminute= 60000;                                                      //=============================================== One minute 60 second*1000 ms
int onehour= 3600000;                                                     //================================================ One hour 3600 second *1000 ms
//========================================
#define Volt_rms 220                                                   //========== Use a Suffix is ensuring that the result of a computation doesn't overflow
const unsigned long sampleTime = 1000UL;                              //=========== Sample over 100ms, it is an exact number of cycles for both 50Hz and 60Hz mains
const unsigned long numSamples = 250UL;                              //============ Choose the number of samples to divide sampleTime exactly, but low enough for the ADC to keep up
const unsigned long sampleInterval = sampleTime/numSamples;         //============= The sampling interval, must be longer than then ADC conversion time
const int adc_zero = 510;           //============================================= Relative digital zero of the arudino input from ACS712 (could make this a variable and auto-adjust it)
//**===============
const float Power_Factor = 0.81;
int Power_Consumption=1000;
unsigned long voltageAcv = 0;
unsigned long currentAcc = 0;
unsigned int count = 0;
//**================
const char* host1 = "data.sparkfun.com";
const char* streamId   = "6Jwy5AG7Y2fw3N3Wg47R";
const char* privateKey = "Ww4alyBp79uMlRlg4Nyr";
//==========
const char* host2 = "api.thingspeak.com";
const char* writeAPIKey = "3S8250CFDF4GITEU";
//=============================================== SETUP PARAMETERS =====================================
//================================================================================================================
void setup() {
  // put your setup code here, to run once:
  delay(1000);
  Serial.begin(115200);
  Serial.print("Configuring Plug point...\r\n");
  Serial.println("Power_Plug"+String(0)+String(0)+String(1)+"\r\n");
  Serial.println();
  Serial.println();
  Serial.print("Connecting to ");
//WiFi.mode(WIFI_AP_STA);
  Serial.println();
  WiFi.begin(ssid1, password1);        //======== We start by connecting to a WiFi network
  Serial.println(ssid1);
  //========================================================================================
  WiFi.softAP(ssid,password);          //======== Provide the (SSID, password) of ESP
  WiFi.mode(WIFI_AP_STA);
while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.print(".");
  }
  IPAddress myIP = WiFi.softAPIP();
  Serial.print("Plug001 IP address: ");
  Serial.println(myIP);
  pinMode(RelayPin, OUTPUT);               //==== GPIO5 is an OUTPUT pin
  digitalWrite(RelayPin, LOW);            //===== Initial state is ON  
  TurnOff="L";     //============================ if the request starts with "L" then it will be LOW
  TurnOn="H";     //============================= if the request starts with "H" then it will be HIGH   
  Moment ="#";    //============================= if the request starts with "#" then it will be Number which is the desired moment         
  //============================================= START HTTP Server
server.begin();                            
IPAddress HTTPS_ServerIP= WiFi.softAPIP(); //==== Obtain the IP of the Server 
Serial.print("Server IP is: ");        
Serial.println(HTTPS_ServerIP);
}
//===============================================================================================================================================================
void loop() {                        //=============================== START TAKE SAMPLES AND CALCULATE POWER CONSUMPTION =======================================
                                             
WiFiClient client = server.available();        
unsigned long prevMicros = micros() - sampleInterval ;
if (!client) {
    while (count < numSamples){
  if (micros() - prevMicros >= sampleInterval)
   {
    int adc_curnt = (analogRead(A0)*5) - adc_zero;    //============================ This is because [0 A >> Vcc/2 = 2.5 V >> 1024/2 = 511 (ADC offset)], Multiply by five due to volt divider
     currentAcc += (unsigned long)(adc_curnt* adc_curnt);
     ++count;
     prevMicros += sampleInterval;
     }
       }   
                   
float Curnt_rms = sqrt((float)currentAcc/(float)numSamples) * (75.7576 / 1024.0);//== This formula [Irms = Ipeak/sqrt(2)] and relates to relation between current sensor and ADC
Power_Consumption = int (Curnt_rms * Volt_rms * Power_Factor);
//============================================================================================ filling up   ConsumptionArray,and copy results in CopyArray, to read from    
for (int i =1; i<=60;i++){ 
  if (!digitalRead(RelayPin)){  
 if ((ConsumptionArray [i-1]>0) && (ConsumptionArray [i+1]==0)) { 
if (ConsumptionArray [i]==0 ){                                     
if((micros()/oneminute)==0)
{
  ConsumptionArray [i]=Power_Consumption; 
  CopyArray [i-1]= ConsumptionArray [i];      //=================================== When next one hour starts, we erase ConsumptionArray, CopyArray allow us to read values
}}}}}
if (ConsumptionArray [60]>0){  //================================================== This means that one hour occued, we can't use micro() for this cause it resets after almost 70 minutes
                                                  
for (int i =1; i>=60;i++){   ConsumptionArray [i]=0;}}           //================ Return ConsumptionArray,except first and last elements, to zeros
      
  
 

  //**================================================================ SEND DATA To SPARKFUN SO ESP IS A CLIENT(STATION POINT)===============================   

  Serial.println("Power_Plug"+String(0)+String(0)+String(1)+"\r\n");
  Serial.print("connecting to ");
  Serial.println(host1);                 
  
  WiFiClient client;                          //====================== Use WiFiClient class to create TCP connections
  const int httpPort = 80;
  if (!client.connect(host1, httpPort)) {
    Serial.println("connection failed");
    return; }
  
  // We now create a URI for the request
  String url = "/input/";
  url += streamId;
  url += "?private_key=";
  url += privateKey;
  url += "&Watt=";
  url += Power_Consumption ;
  
  Serial.print("Requesting URL: ");
  Serial.println(url);
  
  // This will send the request to the server
  client.print(String("GET ") + url + " HTTP/1.1\r\n" +
               "Host: " + host1 + "\r\n" + 
               "Connection: close\r\n\r\n");
  unsigned long timeout = millis();
  while (client.available() == 0) {
    if (millis() - timeout > 5000) {
      Serial.println(">>> Client Timeout !");
      client.stop();
      
      return;
    }
  }
  
  // Read all the lines of the reply from server and print them to Serial
  while(client.available()){
     String response = client.readStringUntil('\r');       //=================== Read all the lines of the reply from server and print them to Serial
    String line = response.substring(0, 16); //=============================== Trimmed to just the first 17 char.
    Serial.print(line);
    // Serial.print(response);
      delay(1000);
  }
  
  Serial.println();
  Serial.println("closing connection");
//===============
//=================================================================== SEND DATA To THINGSPEAK SO ESP IS A CLIENT(STATION POINT)
Serial.print("connecting to ");
  Serial.println(host2);
  if (!client.connect(host2, httpPort)) {
    Serial.println("connection failed");
    return; }
  
  // We now create a URI for the request
  String url2 = "/update?key=";
  url2+=writeAPIKey;
  url2+="&field1=";
  url2+=Power_Consumption;
  //url+="\r\n"; this causes an eror
 
  
  Serial.print("Requesting URL: ");
  Serial.println(url);
  
 
  client.print(String("GET ") + url + " HTTP/1.1\r\n" +
               "Host: " + host2 + "\r\n" + 
               "Connection: close\r\n\r\n");                   //===================== This will send the request to the server
 // unsigned long timeout = millis();
  while (client.available() == 0) {                     
    if (millis() - timeout > 5000) {
      Serial.println(">>> Client Timeout !");
      client.stop();
      return;
    }
  }
  
  
  while(client.available()){
    String response = client.readStringUntil('\r');       //=================== Read all the lines of the reply from server and print them to Serial
    String line = response.substring(0, 16); //============================== Trimmed to just the first 17 char.
    Serial.print(line);
    // Serial.print(response);
    delay(1000);
     }
  
  Serial.println();
  Serial.println("closing connection");
  //===============
  return;
}
//====================================================================================================================
//========================== IF THERE IS A CLIENT LEAVE PREVIOUS LOOP TO HANDLE IT =============================
Serial.println("Somebody has connected !!!");
delay(1); 
Serial.println("Client disonnected");
String request = client.readString();            //============ Read what the client has sent into a String class and print the request to the monitor
String Subrequest = request.substring(6,10);    //============== Trimmed to just read user desired action start in char 8 and end in char 9
Serial.println(Subrequest);
String Subrequest2 = Subrequest.substring(0,1); //============== take first char from incomming string to detrmine desired command
if (TurnOff.equals(Subrequest2)){
  pinMode(RelayPin,HIGH);
  client.flush();
String S = "Turned Off";
    String httpResponse;
     String httpHeader;
     httpHeader = "HTTP/1.1 200 OK\r\nContent-Type: text/html; charset=UTF-8\r\n"; 
     httpHeader += "Content-Length: ";
     httpHeader += S.length();
     httpHeader += "\r\n";
     httpHeader +="Connection: close\r\n\r\n";
     httpResponse = httpHeader + S + " ";  
       client.print(httpResponse); 
     }
//====================================================================================================================
else if (TurnOn.equals(Subrequest2)){
   pinMode(RelayPin,LOW);
  client.flush();
String S = "Turned On";
    String httpResponse;
     String httpHeader;
     httpHeader = "HTTP/1.1 200 OK\r\nContent-Type: text/html; charset=UTF-8\r\n"; 
     httpHeader += "Content-Length: ";
     httpHeader += S.length();
     httpHeader += "\r\n";
     httpHeader +="Connection: close\r\n\r\n";
     httpResponse = httpHeader + S + " ";
       client.print(httpResponse); 
     }
//====================================================================================================================
else if (Moment.equals(Subrequest2)){ 
 String Subrequest3 = Subrequest.substring(1,3); //=================== take 2nd and 3rd chars from incomming string (moment number) to return the proper consumption value
int j = Subrequest3.toInt();
    if (j>0 && j < 61){ 
int s = CopyArray[j-1];
String S = String(s);
     S += "Watt";
     String httpResponse;
     String httpHeader;
     httpHeader = "HTTP/1.1 200 OK\r\nContent-Type: text/html; charset=UTF-8\r\n"; 
     httpHeader += "Content-Length: ";
     httpHeader += S.length();
     httpHeader += "\r\n";
     httpHeader +="Connection: close\r\n\r\n";
     httpResponse = httpHeader + S+" "; 
       client.print(httpResponse); 
        client.flush();
        }
//====================================================================================================================       
         else{              //========================= This condition due to alert user the return value is in correct
          String WrongMessage ="Please Enter Moment between 1 and 60";
  String httpResponse;
     String httpHeader;
     httpHeader = "HTTP/1.1 200 OK\r\nContent-Type: text/html; charset=UTF-8\r\n"; 
     httpHeader += "Content-Length: ";
     httpHeader += WrongMessage.length();
     httpHeader += "\r\n";
     httpHeader +="Connection: close\r\n\r\n";
     httpResponse = httpHeader + WrongMessage + " "; 
       client.print(httpResponse);
       client.flush();
       }
     }
  delay(1);
  Serial.println("Client disonnected");         //======================================================= END ===========================================================
}
