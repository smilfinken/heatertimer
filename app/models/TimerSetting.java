package models;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.persistence.*;

@Entity
@Table(name = "timersettings")
public class TimerSetting {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    public String id;

    @Column(nullable = false)
    public String heater;

    @Column(nullable = false)
    public Date departure;

    public String days;

    public TimerSetting() { }

    public TimerSetting(String heater, Date departure, String days) {
        this.heater = heater;
        this.departure = departure;
        this.days = days;
    }

    public String getDepartureTime() {
        return (new SimpleDateFormat("HH:mm")).format(departure);
    }
}
