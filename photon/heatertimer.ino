/*
 * system config
 */

#define NODEBUG
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

#define SIGNAL_ON true
#define SIGNAL_OFF false
#define RELAY_ON LOW
#define RELAY_OFF HIGH

#define RELAYPIN_1 A0
#define RELAYPIN_2 A1
#define POWERPIN_R D0
#define POWERPIN_G D1
#define POWERPIN_B D2

/*
 * constants
 */

const int RELAYS[] = { RELAYPIN_1, RELAYPIN_2 };
const int MINVALUE = -9999;
const int CHECKIN_INTERVAL = 5 * 60 * 1000;  // once every 5 minutes
const int SENSOR_INTERVAL = 1 * 60 * 1000;  // once every minute
const char SERVICE_HOST[] = "projects.smilfinken.net";
const int SERVICE_PORT = 9000;
const char SERVICE_PATH[] = "/api/checkin";

/*
 * declarations for the timer events
 */

SparkCorePolledTimer callServerTimer(CHECKIN_INTERVAL);
SparkCorePolledTimer getTemperatureTimer(SENSOR_INTERVAL);
//Timer callServerTimer(CHECKIN_INTERVAL, callServer);
//Timer getTemperatureTimer(SENSOR_INTERVAL, getTemperature);


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
    #ifdef DEBUG
    Serial.printf("relay %d going %s\n", relay, on ? "ON" : "OFF");
    #endif
    if (relay <= (sizeof(RELAYS)/sizeof(int))) {
        digitalWrite(RELAYS[relay - 1], on ? RELAY_ON : RELAY_OFF);
    }
}

void initRelays() {
    #ifdef DEBUG
    Serial.printf("init relays: %d %d\n", sizeof(RELAYS), sizeof(int));
    #endif
    for (int i = 0; i < (sizeof(RELAYS)/sizeof(int)); i++) {
        pinMode(RELAYS[i], OUTPUT);
        setRelay(i, SIGNAL_OFF);
    }
}

void setPowerLED(int red, int green, int blue) {
    #ifdef DEBUG
    Serial.printf("setting LED to %d %d %d\n", red, green, blue);
    #endif
    analogWrite(POWERPIN_R, red);
    analogWrite(POWERPIN_G, green);
    analogWrite(POWERPIN_B, blue);
}

void initPowerLED() {
    pinMode(POWERPIN_R, OUTPUT);
    pinMode(POWERPIN_G, OUTPUT);
    pinMode(POWERPIN_B, OUTPUT);

    // set status
    setStatusConnecting();
}

void setStatusOK() {
    setPowerLED(0, 255, 0); // green
}

void setStatusError() {
    setPowerLED(255, 0, 0); // red
}

void setStatusConnecting() {
    setPowerLED(155, 165, 0); // kinda orange
}

void performAction(const char* action, const char*arguments) {
    #ifdef DEBUG
    Serial.printf("performing action '%s' with arguments '%s'\n", action, arguments);
    #endif
    
    // TODO: split arguments
    if (!strcmp(action, "on")) {
        setRelay(1, SIGNAL_ON);
        setRelay(2, SIGNAL_ON);
    } else if (!strcmp(action, "off")) {
        setRelay(1, SIGNAL_OFF);
        setRelay(2, SIGNAL_OFF);
    } else {
        setStatusError();
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
            setStatusOK();
            if (jsonResponse["actions"].is<JsonArray&>())
            {
                const char* array = jsonResponse["actions"];
                #ifdef DEBUG
                Serial.printf("actions = '%s'\n", array);
                #endif
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
    #ifdef DEBUG
    Serial.begin(9600);
    #endif
 
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

//    callServerTimer.start();
//    getTemperatureTimer.start();
}


/*
 * application main loop
 */
 
void loop() {
    getTemperatureTimer.Update();
    callServerTimer.Update();
}
