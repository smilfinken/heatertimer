/*
 * system config
 */
 
//SYSTEM_THREAD(ENABLED);


/*
 * includes
 */

#include "SparkCorePolledTimer/SparkCorePolledTimer.h"
#include "PietteTech_DHT/PietteTech_DHT.h"
#include "SparkJson/SparkJson.h"
#include "HttpClient/HttpClient.h"


/*
 * defines and declarations for the DHT sensor
 */

#define DHTTYPE DHT22
#define DHTPIN  D6

void dht_wrapper(); // must be declared before the lib initialization
PietteTech_DHT DHT(DHTPIN, DHTTYPE, dht_wrapper);
void dht_wrapper() {
    DHT.isrCallback();
}


/*
 * defines and declarations for connected components
 */

#define ON true
#define OFF false

#define RELAYPIN_1 D0
#define RELAYPIN_2 D1
#define POWERPIN_R A0
#define POWERPIN_G A1
#define POWERPIN_B A2

/*
 * constants
 */

const int RELAYS[] = { RELAYPIN_1, RELAYPIN_2 };
const int MINVALUE = -9999;
const int CHECKIN_INTERVAL = 5 * 60 * 1000;  // once every 5 minutes
const int SENSOR_INTERVAL = 1 * 60 * 1000;  // once every minute
const char SERVICE_HOST[] = "projects.smilfinken.net";
const int SERVICE_PORT = 9000;
const char SERVICE_PATH[] = "/checkin";

/*
 * declarations for the timer events
 */

SparkCorePolledTimer callServerTimer(CHECKIN_INTERVAL);
SparkCorePolledTimer getTemperatureTimer(SENSOR_INTERVAL);


/*
 * declarations for current state
 */

char deviceId[64] = "";
double currentTemp = MINVALUE;
double currentHumidity = MINVALUE;
char sensorStatus[32] = "";

/*
 * private methods
 */

void setRelay(int relay, bool on) {
    if (relay <= (sizeof(RELAYS)/sizeof(int))) {
        digitalWrite(RELAYS[relay], on ? HIGH : LOW);
    }
}

void initRelays() {
    for (int i = 0; i < (sizeof(RELAYS)/sizeof(int)); i++) {
        pinMode(RELAYS[i], OUTPUT);
        setRelay(i, OFF);
    }
}

void setPowerLED(int red, int green, int blue) {
    analogWrite(POWERPIN_R, red);
    analogWrite(POWERPIN_G, green);
    analogWrite(POWERPIN_B, blue);
}

void initPowerLED() {
    pinMode(POWERPIN_R, OUTPUT);
    pinMode(POWERPIN_G, OUTPUT);
    pinMode(POWERPIN_B, OUTPUT);
    setPowerLED(0, 0, 0);
}

void setStatusOK() {
    setPowerLED(0, 255, 0);
}

void setStatusError() {
    setPowerLED(255, 0, 0);
}

void setStatusConnecting() {
    setPowerLED(128, 0, 128);
}

void performAction(const char* action, const char*arguments) {
    Serial.printf("performing action %s with arguments %s", action, arguments);
    
    // split arguments
    if (!strcmp(action, "on")) {
        digitalWrite(RELAYPIN_1, HIGH);
        digitalWrite(RELAYPIN_2, LOW);
    } else if (!strcmp(action, "off")) {
        digitalWrite(RELAYPIN_1, LOW);
        digitalWrite(RELAYPIN_2, LOW);
    } else {
        digitalWrite(RELAYPIN_1, HIGH);
        digitalWrite(RELAYPIN_2, HIGH);
    }
}

void performAction(ArduinoJson::JsonObject& action) {
    performAction(action["action"], action["arguments"]);
}

void getTemperature() {
    int result = DHT.acquireAndWait();
    currentTemp = MINVALUE;
    currentHumidity = MINVALUE;

    switch (result) {
        case DHTLIB_OK:
            strcpy(sensorStatus, "OK");
            
            currentTemp = DHT.getCelsius();
            currentHumidity = DHT.getHumidity();
            break;
        case DHTLIB_ERROR_CHECKSUM:
            strcpy(sensorStatus, "Checksum error");
            break;
        case DHTLIB_ERROR_ISR_TIMEOUT:
            strcpy(sensorStatus, "ISR time out error");
            break;
        case DHTLIB_ERROR_RESPONSE_TIMEOUT:
            strcpy(sensorStatus, "Response time out error");
            break;
        case DHTLIB_ERROR_DATA_TIMEOUT:
            strcpy(sensorStatus, "Data time out error");
            break;
        case DHTLIB_ERROR_ACQUIRING:
            strcpy(sensorStatus, "Acquiring");
            break;
        case DHTLIB_ERROR_DELTA:
            strcpy(sensorStatus, "Delta time to small");
            break;
        case DHTLIB_ERROR_NOTSTARTED:
            strcpy(sensorStatus, "Not started");
            break;
        default:
            strcpy(sensorStatus, "Unknown error");
            break;
    }
}

void callServer() {
    setStatusConnecting();

    http_request_t request;
    request.hostname = SERVICE_HOST;
    request.port = SERVICE_PORT;
    request.path = SERVICE_PATH;

    // request body using sparkjson lib
    StaticJsonBuffer<400> jsonBuffer;
    JsonObject& jsonBody = jsonBuffer.createObject();
  
    jsonBody["sensorId"] = deviceId;
    jsonBody["sensorStatus"] = sensorStatus;
    jsonBody["currentTemp"] = currentTemp;
    jsonBody["currentHumidity"] = currentHumidity;
    
    char requestBody[400];
    jsonBody.printTo(requestBody, sizeof(requestBody));
    request.body = requestBody;
  
    http_header_t headers[] = {
        { "Content-Type", "application/json" },
        //{ "Accept" , "*/*" },
        { NULL, NULL } // NOTE: Always terminate headers will NULL
    };
    http_response_t response;
    
    if (WiFi.ready()) {
        HttpClient http;
        http.post(request, response, headers);
        
        char responseBody[200];
        response.body.toCharArray(responseBody, 200);
        JsonObject& jsonResponse = jsonBuffer.parseObject(responseBody);
        if (jsonResponse.success()) {
            if (jsonResponse["actions"].is<JsonArray&>())
            {
                const char* array = jsonResponse["actions"];
                JsonArray& actions = jsonBuffer.parseArray(const_cast<char*>(array));
                for (int i = 0; i < actions.size(); i++) {
                    performAction(actions[i]);
                }
            } else {
                performAction(jsonResponse["action"], jsonResponse["arguments"]);
            }
        } else {
            setStatusError();    
        }
    }
}


/*
 * application initialisation
 */
 
void setup() {
    Serial.begin(9600);
 
    // set status
    setStatusConnecting();
    
    // get device ID
    strcpy(deviceId, System.deviceID());
    
    // initialise pins
    pinMode(DHTPIN, INPUT_PULLUP);
    initPowerLED();
    initRelays();

    // setup timer events
    getTemperatureTimer.SetCallback(getTemperature);
    callServerTimer.SetCallback(callServer);
    
    // do one cycle at startup
    getTemperature();
    callServer();
}


/*
 * application main loop
 */
 
void loop() {
    getTemperatureTimer.Update();
    callServerTimer.Update();
}
