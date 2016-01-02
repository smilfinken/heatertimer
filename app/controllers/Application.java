package controllers;

import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;
import java.util.Calendar;
import java.util.Date;
import java.util.Arrays;

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
            result = (Double)temperatureQuery.getSingleResult();
        } catch (Exception e) {
            LOGGER.warning(String.format("Application.index(): error getting average temperature: %s", e.toString()));
        }

        return result;
    }

    @Transactional
    private Double getAverageHumidity(Date startTime) {
        Double result = 0.0;

        TypedQuery<Double> temperatureQuery = JPA.em().createQuery("SELECT AVG(sr.humidity) FROM SensorReading sr WHERE sr.timestamp > :starttime", Double.class);
        temperatureQuery.setParameter("starttime", startTime);
        try {
            result = (Double)temperatureQuery.getSingleResult();
        } catch (Exception e) {
            LOGGER.warning(String.format("Application.index(): error getting average humidity: %s", e.toString()));
        }

        return result;
    }

    @Transactional
    private Double getAveragePressure(Date startTime) {
        Double result = 0.0;

        TypedQuery<Double> temperatureQuery = JPA.em().createQuery("SELECT AVG(sr.pressure) FROM SensorReading sr WHERE sr.timestamp > :starttime", Double.class);
        temperatureQuery.setParameter("starttime", startTime);
        try {
            result = (Double)temperatureQuery.getSingleResult() / 1000;
        } catch (Exception e) {
            LOGGER.warning(String.format("Application.index(): error getting average atmospheric pressure: %s", e.toString()));
        }

        return result;
    }

    private Date getLastTimeUnit(int timeUnit) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(timeUnit, -1);
        return calendar.getTime();
    }

    @Transactional
    private Double getLastWeekAverageTemperature() {
        return getAverageTemperature(getLastTimeUnit(Calendar.WEEK_OF_YEAR));
    }

    @Transactional
    private Double getLastDayAverageTemperature() {
        return getAverageTemperature(getLastTimeUnit(Calendar.DAY_OF_YEAR));
    }

    @Transactional
    private Double getLastHourAverageTemperature() {
        return getAverageTemperature(getLastTimeUnit(Calendar.HOUR_OF_DAY));
    }

    @Transactional
    private Double getLastWeekAverageHumidity() {
        return getAverageHumidity(getLastTimeUnit(Calendar.WEEK_OF_YEAR));
    }

    @Transactional
    private Double getLastDayAverageHumidity() {
        return getAverageHumidity(getLastTimeUnit(Calendar.DAY_OF_YEAR));
    }

    @Transactional
    private Double getLastHourAverageHumidity() {
        return getAverageHumidity(getLastTimeUnit(Calendar.HOUR_OF_DAY));
    }

    @Transactional
    private Double getLastWeekAveragePressure() {
        return getAveragePressure(getLastTimeUnit(Calendar.WEEK_OF_YEAR));
    }

    @Transactional
    private Double getLastDayAveragePressure() {
        return getAveragePressure(getLastTimeUnit(Calendar.DAY_OF_YEAR));
    }

    @Transactional
    private Double getLastHourAveragePressure() {
        return getAveragePressure(getLastTimeUnit(Calendar.HOUR_OF_DAY));
    }

    @Transactional
    public Result index() {
        Double[] averageTemperature = { getLastHourAverageTemperature(), getLastDayAverageTemperature(), getLastWeekAverageTemperature() };
        Double[] averageHumidity = { getLastHourAverageHumidity(), getLastDayAverageHumidity(), getLastWeekAverageHumidity() };
        Double[] averagePressure = { getLastHourAveragePressure(), getLastDayAveragePressure(), getLastWeekAveragePressure() };

        return ok(index.render(Arrays.asList(averageTemperature), Arrays.asList(averageHumidity), Arrays.asList(averagePressure)));
    }
}
