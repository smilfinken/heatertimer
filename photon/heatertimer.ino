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

#define RELAYPIN1 D0
#define RELAYPIN2 D1

/*
 * constants
 */

#define MINVALUE -9999
#define CHECKIN_INTERVAL    5 * 60 * 1000  // once every 5 minutes
#define SENSOR_INTERVAL     1 * 60 * 1000  // once every minute
#define SERVICE_HOST "projects.smilfinken.net"
#define SERVICE_PORT 9000
#define SERVICE_PATH "/checkin"

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
    digitalWrite(RELAYPIN2, LOW);

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
    HttpClient http;
    http.post(request, response, headers);
    
    char responseBody[200];
    response.body.toCharArray(responseBody, 200);
    JsonObject& jsonResponse = jsonBuffer.parseObject(responseBody);
    if (jsonResponse.success()) {
        const char* action = jsonResponse["action"];
        if (!strcmp(action, "on")) {
            digitalWrite(RELAYPIN1, HIGH);
            digitalWrite(RELAYPIN2, LOW);
        } else if (!strcmp(action, "off")) {
            digitalWrite(RELAYPIN1, LOW);
            digitalWrite(RELAYPIN2, LOW);
        } else {
            digitalWrite(RELAYPIN1, HIGH);
            digitalWrite(RELAYPIN2, HIGH);
        }
    } else {
        digitalWrite(RELAYPIN1, LOW);
        digitalWrite(RELAYPIN2, HIGH);
    }
}


/*
 * application initialisation
 */
 
void setup() {
    // get device ID
    strcpy(deviceId, System.deviceID());
    
    // setup sensor pins
    pinMode(DHTPIN, INPUT_PULLUP);

    // setup controller pins
    pinMode(RELAYPIN1, OUTPUT);
    pinMode(RELAYPIN2, OUTPUT);

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
