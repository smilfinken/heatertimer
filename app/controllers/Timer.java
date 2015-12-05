package controllers;

import models.TimerSetting;
import views.html.*;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import play.mvc.Controller;
import play.mvc.Result;
import play.db.jpa.Transactional;
import play.db.jpa.JPA;
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
                Date departureDate = (new SimpleDateFormat("HH:mm")).parse(json.findPath("departure").textValue());
                String days = json.findPath("days").textValue();

                Query query = JPA.em().createQuery("select ts from TimerSetting ts where ts.heater = :heater");
                query.setMaxResults(1);
                query.setParameter("heater", heater);
                List<TimerSetting> timerSettings = (List<TimerSetting>) query.getResultList();
                if (timerSettings.isEmpty()) {
                    TimerSetting timerSetting = new models.TimerSetting(heater, departureDate, days);
                    JPA.em().persist(timerSetting);
                } else {
                    TimerSetting timerSetting = timerSettings.get(0);
                    timerSetting.departure = departureDate;
                    timerSetting.days = days;
                    JPA.em().persist(timerSetting);
                }
            } catch (Exception e) {
            }
        }

        return list();
    }

    @Transactional(readOnly = true)
    public Result list() {
        List<TimerSetting> timerSettings = (List<TimerSetting>) JPA.em().createQuery("select ts from TimerSetting ts").getResultList();
        return ok(timerlist.render(timerSettings));
    }
}
