/*
 * system config
 */

#define DEBUG
//SYSTEM_THREAD(ENABLED);


/*
 * includes
 */

#include "SparkCorePolledTimer/SparkCorePolledTimer.h"
#include "PietteTech_DHT/PietteTech_DHT.h"
#include "Adafruit_BMP085/Adafruit_BMP085.h"
#include "SparkJson/SparkJson.h"
#include "HttpClient/HttpClient.h"


/*
 * defines and declarations for the DHT sensor
 */

#define DHTTYPE DHT22
#define DHTPIN  D6

void dht_wrapper(); // must be declared before the lib initialization
PietteTech_DHT dht(DHTPIN, DHTTYPE, dht_wrapper);
void dht_wrapper() {
    dht.isrCallback();
}


/*
 * defines and declarations for the air pressure sensor
 */

#define I2C_DATA D0
#define I2C_CLOCK D1

Adafruit_BMP085 bmp;


/*
 * defines and declarations for connected output components
 */

#define SIGNAL_ON true
#define SIGNAL_OFF false
#define RELAY_ON LOW
#define RELAY_OFF HIGH

#define RELAYPIN_1 A0
#define RELAYPIN_2 A1
#define POWERPIN_R D2
#define POWERPIN_G D3
#define POWERPIN_B D4

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

SparkCorePolledTimer getReadingsTimer(SENSOR_INTERVAL);
SparkCorePolledTimer callServerTimer(CHECKIN_INTERVAL);

/*
 * declarations for current state
 */

char deviceId[64] = "";
double currentTemperature = MINVALUE;
double currentHumidity = MINVALUE;
double currentAirPressure = MINVALUE;
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
    for (int i = 0; i < (sizeof(RELAYS)/sizeof(int)); i++) {
        pinMode(RELAYS[i], OUTPUT);
        setRelay(i + 1, SIGNAL_OFF);
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

    // set status
    setStatusConnecting();
}

void setStatusOK() {
    setPowerLED(0, 255, 0); // green
}

void setStatusError(char* message) {
#ifdef DEBUG
    Serial.printf("error message = %s\n", message);
#endif
    setPowerLED(255, 0, 0); // red
}

void setStatusConnecting() {
    setPowerLED(155, 165, 0); // kinda orange
}

void initSensors() {
    pinMode(DHTPIN, INPUT_PULLUP);
    //dht.acquire();
    bmp.begin();
}

void initWifi() {
    // select external antenna
    WiFi.selectAntenna(ANT_EXTERNAL);
}

bool performAction(ArduinoJson::JsonObject& action) {
    bool result = false;
    
    const char* state = action["action"];
    const int target = atoi(action["target"]);
    bool signal = !strcmp(state, "on") ? SIGNAL_ON : SIGNAL_OFF;

#ifdef DEBUG
    Serial.printf("setting state '%s' on target '%d'\n", state, target);
#endif
    setRelay(target, signal);
    result = true;

    return result;
}

void getReadings() {
    currentTemperature = bmp.readTemperature();
    currentHumidity = MINVALUE;
    currentAirPressure = MINVALUE;

    dht.acquire();
    delay(100);
    if (!dht.acquiring()) {
        int result = dht.getStatus();

        switch (result) {
            case DHTLIB_OK:
                strcpy(sensorStatus, "OK");
                currentTemperature = dht.getCelsius();
                currentHumidity = dht.getHumidity();
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
                //strcpy(sensorStatus, "Not started");
                strcpy(sensorStatus, "OK");
                break;
            default:
                strcpy(sensorStatus, "Unknown error");
                break;
        }
    }
    
    currentAirPressure = bmp.readPressure();
}

void callServer() {
    if (WiFi.ready()) {
        setStatusConnecting();
      
        http_request_t request;
        request.hostname = SERVICE_HOST;
        request.port = SERVICE_PORT;
        request.path = SERVICE_PATH;
    
        // request body using sparkjson lib
        StaticJsonBuffer<480> jsonBuffer;
        JsonObject& jsonBody = jsonBuffer.createObject();
      
        jsonBody["sensorId"] = deviceId;
        jsonBody["sensorStatus"] = sensorStatus;
        jsonBody["currentTemp"] = currentTemperature;
        jsonBody["currentHumidity"] = currentHumidity;
        jsonBody["currentAirPressure"] = currentAirPressure;
        jsonBody["wifiSignal"] = WiFi.RSSI();
        
        char requestBody[480];
        jsonBody.printTo(requestBody, sizeof(requestBody));
        request.body = requestBody;
    
#ifdef DEBUG
        Serial.printf("signal strength = %d dB\n", WiFi.RSSI());
#endif
        http_header_t headers[] = {
            { "Content-Type", "application/json" },
            { "Accept" , "application/json" },
            { NULL, NULL } // NOTE: Always terminate headers with NULL
        };
        http_response_t response;
        
        HttpClient http;
        http.post(request, response, headers);
        
        char responseBody[480];
        strcpy(responseBody, response.body);
#ifdef DEBUG
        Serial.printf("response body = %s\n", responseBody);
#endif

        JsonObject& jsonResponse = jsonBuffer.parseObject(responseBody);
        if (jsonResponse.success()) {
            setStatusOK();
            if (jsonResponse["actions"].is<JsonArray&>())
            {
                JsonArray& actions = jsonResponse["actions"].asArray();
                for (int i = 0; i < actions.size(); i++) {
                    char commandString[64];
                    strcpy(commandString, actions[i]);
#ifdef DEBUG
                    Serial.printf("commandString = %s\n", commandString);
#endif
                    JsonObject& command = jsonBuffer.parseObject(commandString);
                    if (command.success()) {
                        performAction(command);
                    } else {
                        setStatusError("error parsing command");
                    }
                }
            }
        } else {
            setStatusError("error parsing response");
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
    
    // initialise
    initPowerLED();
    initRelays();
    initSensors();
    initWifi();

    // setup timer events
    getReadingsTimer.SetCallback(getReadings);
    callServerTimer.SetCallback(callServer);

    // do one cycle at startup
    getReadings();
    callServer();
}


/*
 * application main loop
 */
 
void loop() {
    getReadingsTimer.Update();
    callServerTimer.Update();
}
