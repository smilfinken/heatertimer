package controllers;

import models.SensorReading;
import models.TimerSetting;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;
import java.util.Date;

import play.mvc.BodyParser;
import play.mvc.Result;
import play.mvc.Controller;
import play.db.jpa.Transactional;
import play.db.jpa.JPA;
import javax.persistence.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import static play.libs.Json.*;

public class Listener extends Controller {
    private static final Logger LOGGER = Logger.getLogger("GLOBAL");

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

                    boolean[] relayStatus = getNewRelayStatusList();

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

    private Double getLatestTemperature() {
        Double result = null;

        try {
            TypedQuery<Double> query = JPA.em().createQuery("SELECT sr.temperature FROM SensorReading AS sr ORDER BY sr.timestamp DESC", Double.class);
            result = query.setMaxResults(1).getResultList().get(0);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Listener.getLatestTemperature(): Exception occurred when getting latest temperature reading", e);
        }

        return result;
    }
    private boolean getNewRelayStatus(int heater) {
        boolean result = false;
        Double latestTemperature = getLatestTemperature();

        if (latestTemperature != null) {
            try {
                TypedQuery<TimerSetting> query = JPA.em().createQuery("SELECT ts FROM TimerSetting AS ts WHERE ts.heater = :heater", TimerSetting.class);
                query.setParameter("heater", heater);
                List<TimerSetting> timers = query.getResultList();
                for (TimerSetting timer : timers) {
                    if (timer.getDepartureTime().before(new Date())) {
                        result = true;
                    }
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Listener.getNewRelayStatus(): Exception occurred when getting timer settings", e);
            }
        }

        return result;
    }

    private boolean[] getNewRelayStatusList() {
        boolean[] result = { false, true };

        for (int i = 0; i < 2; i++) {
            result[i] = getNewRelayStatus(i);
        }

        return result;
    }
}
