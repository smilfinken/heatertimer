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
        this.heater = -1;
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
            calendar.setTime(new Date());
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);

            int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            LOGGER.info(String.format("TimerSetting.getDepartureTime(): currentDayOfWeek = %d", currentDayOfWeek));
            int[] activeDays = new int[days.size()];
            for (int i = 0; i < days.size(); i++) {
                try {
                    int day = days.get(i);
                    activeDays[i] = (day < currentDayOfWeek) ? day + 7 : day;
                    LOGGER.info(String.format("TimerSetting.getDepartureTime(): activeDays[%d] = %d", i, activeDays[i]));
                } catch (Exception e) {}
            }
            java.util.Arrays.sort(activeDays);
            int nextActiveDay = -1;
            for (int i = 0; i < activeDays.length && nextActiveDay < 0; i++) {
                LOGGER.info(String.format("TimerSetting.getDepartureTime(): [sorted] activeDays[%d] = %d", i, activeDays[i]));
                if (activeDays[i] >= currentDayOfWeek) {
                    nextActiveDay = activeDays[i] > 7 ? activeDays[i] - 7 : activeDays[i];
                }
            }
            LOGGER.info(String.format("TimerSetting.getDepartureTime(): nextActiveDay = %d", nextActiveDay));
            if (nextActiveDay >= 0) {
                calendar.add(Calendar.DAY_OF_YEAR, nextActiveDay);
            }

            result = calendar.getTime();
            LOGGER.info(String.format("TimerSetting.getDepartureTime(): result = %s", result.toString()));
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
