package controllers;

import common.JsonCreator;
import models.SensorReading;
import models.TimerSetting;

import java.util.Calendar;
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

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.JsonNode;

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
                JsonNodeFactory jsonNodeFactory = new JsonNodeFactory(false);
                JsonNode response = (JsonNode)jsonNodeFactory.objectNode();

                if (json.findPath("sensorStatus").textValue().toUpperCase().equals("OK")) {
                    String sensorId = json.findPath("sensorId").textValue();
                    double temperature = json.findPath("currentTemp").doubleValue();
                    double humidity = json.findPath("currentHumidity").doubleValue();
                    double pressure = json.findPath("currentAirPressure").doubleValue();

                    int wifiSignal = json.findPath("wifiSignal").intValue();
                    LOGGER.info(String.format("Listener.checkin(): WiFi signal strength = %d dB", wifiSignal));

                    SensorReading reading = new SensorReading(sensorId, temperature, humidity, pressure);
                    JPA.em().persist(reading);

                    boolean[] relayStatus = getNewRelayStatusList();

                    JsonCreator jsonCreator = new JsonCreator();
                    response = jsonCreator.createCheckinResponse(relayStatus);
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

    private Date getStartTime(Date departureTime) {
        Calendar target = Calendar.getInstance();
        target.setTime(departureTime);
        target.add(Calendar.HOUR, -2); // departureTime - 2h
        return target.getTime();
    }

    private Date getStopTime(Date departureTime) {
        Calendar target = Calendar.getInstance();
        target.setTime(departureTime);
        target.add(Calendar.HOUR, 1); // departureTime + grace period
        return target.getTime();
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
                    Date departureTime = timer.getDepartureTime();
                    Date startTime = getStartTime(departureTime);
                    Date stopTime = getStopTime(departureTime);
                    if (startTime.before(new Date()) && stopTime.after(new Date())) {
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
        boolean[] result = { false, false };

        result[0] = getNewRelayStatus(1);
        result[1] = getNewRelayStatus(2);

        return result;
    }
}
