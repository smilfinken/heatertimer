package controllers;

import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import models.SensorReading;
import views.html.*;

import play.mvc.Controller;
import play.mvc.Result;
import play.db.jpa.Transactional;
import play.db.jpa.JPA;
import javax.persistence.TypedQuery;

public class Application extends Controller {
    private static final Logger LOGGER = Logger.getLogger("GLOBAL");

    public Application() {
        if (LOGGER.getHandlers().length == 0) {
            LOGGER.addHandler(new ConsoleHandler());
        }
    }

    @Transactional
    private Double getAverageTemperature(Date startTime) {
        Double result = 0.0;

        TypedQuery<Double> temperatureQuery = JPA.em().createQuery("SELECT AVG(sr.temperature) FROM SensorReading sr WHERE sr.timestamp > :starttime", Double.class);
        temperatureQuery.setParameter("starttime", startTime);
        try {
            result = (Double) temperatureQuery.getSingleResult();
        } catch (Exception e) {
            LOGGER.warning(String.format("Application.index(): error getting average temperature: %s", e.toString()));
        }

        return result;
    }

    @Transactional
    private Double getLastWeekAverageTemperature() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.WEEK_OF_YEAR, -1);
        Date startTime = calendar.getTime();

        return getAverageTemperature(startTime);
    }

    @Transactional
    private Double getLastDayAverageTemperature() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        Date startTime = calendar.getTime();

        return getAverageTemperature(startTime);
    }

    @Transactional
    private Double getLastHourAverageTemperature() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, -1);
        Date startTime = calendar.getTime();

        return getAverageTemperature(startTime);
    }

    @Transactional
    public Result index() {
        Double lastHourAverageTemperature = getLastHourAverageTemperature();
        Double lastDayAverageTemperature = getLastDayAverageTemperature();
        Double lastWeekAverageTemperature = getLastWeekAverageTemperature();


        return ok(index.render(lastHourAverageTemperature, lastDayAverageTemperature, lastWeekAverageTemperature));
    }
}
