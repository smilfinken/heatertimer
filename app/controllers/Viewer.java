package controllers;

import models.SensorReading;
import views.html.*;

import java.util.List;
import java.util.logging.Logger;

import play.mvc.Controller;
import play.mvc.Result;
import play.db.jpa.Transactional;
import play.db.jpa.JPA;

public class Viewer extends Controller {
    private static final Logger LOGGER = Logger.getLogger("GLOBAL");

    @Transactional(readOnly = true)
    public Result list() {
        List<SensorReading> readings = JPA.em().createQuery("SELECT sr FROM SensorReading sr ORDER BY timestamp ASC", SensorReading.class).getResultList();
        return ok(readinglist.render(readings));
    }
}
