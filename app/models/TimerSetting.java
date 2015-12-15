package models;

import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Logger;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.*;

@Entity
@Table(name = "timersettings")
public class TimerSetting {
    private static final Logger LOGGER = Logger.getLogger("GLOBAL");

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;

    @Column(nullable = false)
    public int heater;

    @Column()
    public String label;

    @Column(nullable = false)
    public int hour;

    @Column(nullable = false)
    public int minute;

    @Column(nullable = false)
    public ArrayList<Integer> days;

    public TimerSetting() {
        this.heater = 0;
        this.label = "";
        this.hour = 0;
        this.minute = 0;
        this.days = new ArrayList<>();
    }

    public TimerSetting(int heater, String label, int hour, int minute, ArrayList<Integer> days) {
        LOGGER.info(String.format("TimerSetting: constructing TimerSetting with values %d, %s, %d, %d, %s", heater, label, hour, minute, days.toString()));

        this.heater = heater;
        this.label = label;
        this.hour = hour;
        this.minute = minute;
        this.days = days;
    }

    public TimerSetting(int heater, String label, int hour, int minute, Integer[] days) {
        LOGGER.info(String.format("TimerSetting: constructing TimerSetting with values %d, %s, %d, %d, %s", heater, label, hour, minute, days.toString()));

        copyValues(new TimerSetting(heater, label, hour, minute, (ArrayList<Integer>)Arrays.asList(days)));
    }

    public void copyValues(TimerSetting source) {
        this.heater = source.heater;
        this.label = source.label;
        this.hour = source.hour;
        this.minute = source.minute;
        this.days = source.days;
    }

    public String toString() {
        return String.format("id = %d, heater = %d, label = %s, hour = %d, minute = %d, days = %s", id, heater, label, hour, minute, days);
    }

    public Boolean[] getActiveDays() {
        Boolean[] result = { false, false, false, false, false, false, false };

        for (Integer day : days) {
            result[day] = true;
        }

        return result;
    }

    public Date getDepartureTime() {
        Date result = null;

        if (days.size() != 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);

            for (int i = 0; i < 8; i++) {
                Integer day = calendar.get(Calendar.DAY_OF_WEEK) - 1;
                if (this.days.contains(day) && calendar.getTime().after(new Date())) {
                    result = calendar.getTime();
                    break;
                }
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
        }

        return result;
    }

    public String getDepartureString() {
        String result = "";

        Date departureTime = getDepartureTime();
        if (departureTime != null) {
            result = (new SimpleDateFormat("yyyy-MM-dd HH:mm")).format(departureTime);
        }

        return result;
    }

    public ArrayList<String> getDisplayDays() {
        ArrayList<String> result = new ArrayList<>();

        for (Integer day : days) {
            if (day != null) { result.add(DayOfWeek.of(day).getDisplayName(TextStyle.FULL, Locale.getDefault())); }
        }

        return result;
    }
}
