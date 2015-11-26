package controllers;

import models.SensorReading;
import views.html.*;
import java.util.List;

import play.mvc.Controller;
import play.mvc.Result;
import play.db.jpa.Transactional;
import play.db.jpa.JPA;

public class Viewer extends Controller {
    @Transactional(readOnly = true)
    public Result list() {
        List<SensorReading> readings = (List<SensorReading>) JPA.em().createQuery("select r from SensorReading r").getResultList();
        return ok(list.render(readings));
    }
}
