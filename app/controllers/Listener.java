package controllers;

import models.SensorReading;

import java.util.logging.Logger;
import java.util.logging.Level;

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
    private static final Logger LOGGER = Logger.getLogger(Timer.class.getName());

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
                    double pressure = json.findPath("currentAirPressure").doubleValue();

                    SensorReading reading = new SensorReading(sensorId, temperature, humidity, pressure);
                    JPA.em().persist(reading);

                    boolean[] relayStatus = getNewRelayStatus();

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

    private boolean[] getNewRelayStatus() {
        boolean[] result = { false, true };

        try {
            Double latestTemperature;
            TypedQuery<Double> query = JPA.em().createQuery("SELECT sr.temperature FROM SensorReadings AS sr ORDER BY sr.timestamp DESC");
            latestTemperature = query.setMaxResults(1).getResultList()[0];
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception occurred when getting latest temperature reading", e);
        }

        return result;
    }
}
