package solar.rpg.ticketer.models;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * A helper object that better represents a "day and time" that a screening will be shown.
 * e.g. if the value is Tuesday 12.30pm, a screening will happen at
 * that time every week during the screening's active period.
 * All of its attributes are defined in this class.
 *
 * @author Joshua Skinner
 * @version 1.0
 * @see Screening
 * @since 0.1
 */
public class ScreeningTime {

    /*private static final DateFormat DAY_OF_WEEK_FORMATTER = new SimpleDateFormat("EEEE");
    private static final DateFormat MONTH_FORMATTER = new SimpleDateFormat("MMMM");
    private static final DateFormat TIME_FORMATTER = new SimpleDateFormat("HH:mm a");*/

    private Timestamp actualDate;
    private String /*monthName,*/ dayOfWeek, time;

    public ScreeningTime(String screeningDay, String screeningTime) {
        this.dayOfWeek = screeningDay;
        this.time = screeningTime;
    }

    /**
     * @return Fully-detailed timestamp of when screening time will actually occur.
     */
    public Timestamp getActualDate() {
        return actualDate;
    }

    /**
     * @return Name of the month in which this screening is occurring.
     */
    public String getMonthName() {
        return /*monthName*/"";
    }

    /**
     * @return The day of the week that this screening will be shown on.
     */
    public String getDayOfWeek() {
        return dayOfWeek;
    }

    /**
     * @return The time of the specified day that this screening will be shown on.
     */
    public String getTime() {
        return time;
    }
}
