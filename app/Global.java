import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.Date;

import models.Configuration;

import play.Application;
import play.GlobalSettings;

import play.db.jpa.Transactional;
import play.db.jpa.JPA;
import javax.persistence.*;

public class Global extends GlobalSettings {
    private static final Logger LOGGER = Logger.getLogger("GLOBAL");
    static ThreadLocal<EntityManager> EM = new ThreadLocal<>();

    @Override
    public void onStart(Application app) {
        LOGGER.info(String.format("Global.onStart(): application starting on %s", (new Date()).toString()));

        EntityManager em = EM.get();
//        JPA.bindForCurrentThread(em);
//        JPA.withTransaction(() -> {
//            setConfig();
//        });
    }

    private void setConfig() {
        TypedQuery<Configuration> query;
        ArrayList<Configuration> configurationList;

        query = JPA.em().createQuery("SELECT c FROM Configuration AS c WHERE c.key = :key", Configuration.class);
        query.setParameter("key", "graceperiod");
        query.setMaxResults(1);
        configurationList = (ArrayList<Configuration>) query.getResultList();
        //if (configurationList.size() == 0) {
            //Configuration gracePeriod = new Configuration("graceperiod", 1);
            //JPA.em().persist(gracePeriod);
        //}

        query = JPA.em().createQuery("SELECT c FROM Configuration AS c WHERE c.key = :key", Configuration.class);
        query.setParameter("key", "warmupperiod");
        query.setMaxResults(1);
        configurationList = (ArrayList<Configuration>) query.getResultList();
        //if (configurationList.size() == 0) {
            //Configuration warmupPeriod = new Configuration("warmupperiod", 2);
            //JPA.em().persist(warmupPeriod);
        //}
    }
}
