package models;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.*;

@Entity
@Table(name = "timersettings")
public class TimerSetting {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    public long id;

    @Column(nullable = false)
    public String heater;

    @Column(nullable = false)
    public int hour;

    @Column(nullable = false)
    public int minute;

    public String days;

    public TimerSetting() {
        this.heater = "";
        this.hour = 0;
        this.minute = 0;
        this.days = "";
    }

    public TimerSetting(String heater, int hour, int minute, String days) {
        this.heater = heater;
        this.hour = hour;
        this.minute = minute;
        this.days = days;
    }

    public void copyValues(TimerSetting source) {
        this.heater = source.heater;
        this.hour = source.hour;
        this.minute = source.minute;
        this.days = source.days;
    }

    public String getDepartureTime() {
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);

        return (new SimpleDateFormat("yyyy-MM-dd HH:mm")).format(calendar.getTime());
    }

    public boolean[] getToggleDays() {
        boolean[] result = { false, false, false, false, false, false, false };

        for (int i = 0; i < days.length(); i++) {
            try {
                result[Integer.parseInt(days.substring(i, i + 1)) - 1] = true;
            } catch (Exception e) { }
        }

        return result;
    }

    public ArrayList<DayOfWeek> getDisplayDays() {
        ArrayList<DayOfWeek> result = new ArrayList<DayOfWeek>();

        boolean[] array = getToggleDays();
        for (int i = 0; i < array.length; i++) {
            if (array[i]) {
                result.add(DayOfWeek.of(i + 1));
            }
        }

        return result;
    }
}
