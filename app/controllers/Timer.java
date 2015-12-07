package controllers;

import models.TimerSetting;
import views.html.*;

import java.util.logging.Logger;
import java.util.logging.Level;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import play.data.Form;
import play.db.jpa.Transactional;
import play.db.jpa.JPA;
import play.mvc.Controller;
import play.mvc.Result;
import javax.persistence.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import static play.libs.Json.*;

public class Timer extends Controller {
    private static final Logger LOGGER = Logger.getLogger(Timer.class.getName());

    @Transactional
    public Result set() {
        LOGGER.info("set timer api called");

        JsonNode json = request().body().asJson();
        if(json == null) {
            LOGGER.warning("Error parsing json data");
            return badRequest("Error parsing data, record not stored.");
        } else {
            try {
                String heater = json.findPath("heater").textValue();
                int hour = json.findPath("hour").intValue();
                int minute = json.findPath("minute").intValue();
                String days = json.findPath("days").textValue();

                Query query = JPA.em().createQuery("select ts from TimerSetting ts where ts.heater = :heater");
                query.setMaxResults(1);
                query.setParameter("heater", heater);
                List<TimerSetting> timerSettings = (List<TimerSetting>) query.getResultList();
                if (timerSettings.isEmpty()) {
                    TimerSetting timerSetting = new models.TimerSetting(heater, hour, minute, days);
                    JPA.em().persist(timerSetting);
                } else {
                    TimerSetting timerSetting = timerSettings.get(0);
                    timerSetting.hour = hour;
                    timerSetting.minute = minute;
                    timerSetting.days = days;
                    JPA.em().persist(timerSetting);
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Exception occurred while processing json data", e);
                return badRequest("Error processing data, record not stored.");
            }
        }

        return ok();
    }


    @Transactional
    public Result edit() {
        Form<TimerSetting> settingForm = Form.form(TimerSetting.class);
        TimerSetting requestData = Form.form(TimerSetting.class).bindFromRequest().get();
        TimerSetting persistedData = JPA.em().find(TimerSetting.class, requestData.id);
        if (persistedData != null) {
            LOGGER.info("Editing existing record.");
            settingForm = settingForm.fill(persistedData);
        } else {
            LOGGER.info("Creating new record.");
            settingForm = settingForm.fill(requestData);
        }

        return ok(timerset.render(settingForm));
    }

    @Transactional
    public Result save() {
        TimerSetting requestData = Form.form(TimerSetting.class).bindFromRequest().get();
        TimerSetting persistedData = JPA.em().find(TimerSetting.class, requestData.id);
        if (persistedData != null) {
            LOGGER.info("Updating existing record.");
            persistedData.copyValues(requestData);
        } else {
            LOGGER.info("Saving new record.");
            JPA.em().persist(requestData);
        }

        return redirect(routes.Timer.list());
    }

    @Transactional
    public Result delete() {
        TimerSetting requestData = Form.form(TimerSetting.class).bindFromRequest().get();
        TimerSetting persistedData = JPA.em().find(TimerSetting.class, requestData.id);
        if (persistedData != null) {
            LOGGER.info("Deleting existing record.");
            JPA.em().remove(persistedData);
        }

        return redirect(routes.Timer.list());
    }

    @Transactional(readOnly = true)
    public Result list() {
        List<TimerSetting> timerSettings = (List<TimerSetting>) JPA.em().createQuery("select ts from TimerSetting ts").getResultList();
        return ok(timerlist.render(timerSettings));
    }
}
