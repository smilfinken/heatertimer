package controllers;

import models.SensorReading;

import java.util.List;

import play.mvc.BodyParser;
import play.mvc.Result;
import play.mvc.Controller;
import play.db.jpa.Transactional;
import play.db.jpa.JPA;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import static play.libs.Json.*;

public class Listener extends Controller {
    @BodyParser.Of(BodyParser.Json.class)
    @Transactional
    public Result checkin() {
        JsonNode json = request().body().asJson();
        if(json == null) {
            return badRequest("Error parsing data");
        } else {
            try {
                ObjectNode response = Json.newObject();

                if (json.findPath("sensorStatus").textValue().toUpperCase().equals("OK")) {
                    String sensorId = json.findPath("sensorId").textValue();
                    double temperature = json.findPath("currentTemp").doubleValue();
                    double humidity = json.findPath("currentHumidity").doubleValue();

                    SensorReading reading = new SensorReading(sensorId, temperature, humidity);
                    JPA.em().persist(reading);

                    if (temperature > 23.5) {
                        response.put("action", "off");
                    } else {
                        response.put("action", "on");
                    }
                } else {
                    response.put("action", "none");
                }

                return ok(response);
            } catch (Exception e) {
                return badRequest("Error parsing data");
            }
        }
    }
}
