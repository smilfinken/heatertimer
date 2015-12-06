package controllers;

import models.TimerSetting;
import views.html.*;

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
    @Transactional
    public Result set() {
        JsonNode json = request().body().asJson();
        if(json == null) {
            return badRequest("Error parsing data");
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
            settingForm = settingForm.fill(persistedData);
        } else {
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
        } else {
            JPA.em().persist(requestData);
        }

        return redirect(routes.Timer.list());
    }

    @Transactional
    public Result delete() {
        TimerSetting requestData = Form.form(TimerSetting.class).bindFromRequest().get();
        TimerSetting persistedData = JPA.em().find(TimerSetting.class, requestData.id);
        if (persistedData != null) {
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
