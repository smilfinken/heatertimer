package controllers;

import java.util.logging.Logger;
import java.util.logging.ConsoleHandler;

import views.html.*;

import play.*;
import play.mvc.*;

public class Application extends Controller {
    private static final Logger LOGGER = Logger.getLogger("GLOBAL");

    public Application() {
        if (LOGGER.getHandlers().length == 0) {
            LOGGER.addHandler(new ConsoleHandler());
        }
    }

    public Result index() {
        return ok(index.render());
    }
}
