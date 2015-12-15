package controllers;

import models.TimerSetting;
import views.html.*;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.ArrayList;

import play.data.Form;
import play.data.DynamicForm;
import play.db.jpa.Transactional;
import play.db.jpa.JPA;
import play.mvc.Controller;
import play.mvc.Result;
import javax.persistence.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import play.libs.Json.*;

public class Timer extends Controller {
    private static final Logger LOGGER = Logger.getLogger("GLOBAL");

    @Transactional
    public Result set() {
        LOGGER.info("Timer.set(): set timer api called");

        JsonNode json = request().body().asJson();
        if (json == null) {
            LOGGER.warning("Timer.set(): Error parsing json data");
            return badRequest("Error parsing data, record not stored.");
        } else {
            try {
                int heater = json.findPath("heater").intValue();
                String label = json.findPath("label").textValue();
                int hour = json.findPath("hour").intValue();
                int minute = json.findPath("minute").intValue();
                String dayString = json.findPath("days").textValue();

                // TODO: change set to just insert and create update method
                Query query = JPA.em().createQuery("SELECT ts FROM TimerSetting ts WHERE ts.heater = :heater");
                query.setMaxResults(1);
                query.setParameter("heater", heater);
                ArrayList<TimerSetting> timerSettings = (ArrayList<TimerSetting>) query.getResultList();
                if (timerSettings.isEmpty()) {
                    ArrayList<Integer> days = new ArrayList<>();
                    for (int i = 0; i < dayString.length(); i++) {
                        try {
                            days.add(Integer.parseInt(dayString.substring(i, i + 1)));
                        } catch (Exception e) {}
                    }
                    TimerSetting timerSetting = new models.TimerSetting(heater, label, hour, minute, days);
                    JPA.em().persist(timerSetting);
                } else {
                    TimerSetting timerSetting = timerSettings.get(0);
                    timerSetting.hour = hour;
                    timerSetting.minute = minute;
                    JPA.em().persist(timerSetting);
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Timer.set(): Exception occurred while processing json data", e);
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
            LOGGER.info("Timer.edit(): Editing existing record.");
            settingForm = settingForm.fill(persistedData);
        } else {
            LOGGER.info("Timer.edit(): Creating new record.");
            settingForm = settingForm.fill(requestData);
        }

        return ok(timerset.render(settingForm));
    }

    @Transactional
    public Result save() {
        TimerSetting requestData = Form.form(TimerSetting.class).bindFromRequest().get();
        TimerSetting persistedData = JPA.em().find(TimerSetting.class, requestData.id);
        if (persistedData != null) {
            persistedData.copyValues(requestData);
            LOGGER.info(String.format("Timer.save(): Updated existing record: %s", persistedData.toString()));
        } else {
            JPA.em().merge(requestData);
            LOGGER.info(String.format("Timer.save(): Saved new record: %s", requestData.toString()));
        }

        return redirect(routes.Timer.list());
    }

    @Transactional
    public Result delete() {
        TimerSetting requestData = Form.form(TimerSetting.class).bindFromRequest().get();
        TimerSetting persistedData = JPA.em().find(TimerSetting.class, requestData.id);
        if (persistedData != null) {
            JPA.em().remove(persistedData);
            LOGGER.info(String.format("Timer.delete(): Deleted existing record: %s", persistedData.toString()));
        }

        return redirect(routes.Timer.list());
    }

    @Transactional(readOnly = true)
    public Result list() {
        ArrayList<TimerSetting> timerSettings = (ArrayList<TimerSetting>) JPA.em().createQuery("SELECT ts FROM TimerSetting ts ORDER BY heater DESC").getResultList();
        return ok(timerlist.render(timerSettings));
    }
}
