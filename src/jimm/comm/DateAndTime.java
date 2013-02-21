package jimm.comm;

import jimm.Options;
import jimm.util.ResourceBundle;

import java.util.Calendar;
import java.util.Date;

public class DateAndTime {
    private final static String error_str = "<no_date>";
    final public static int TIME_SECOND = 0;
    final public static int TIME_MINUTE = 1;
    final public static int TIME_HOUR = 2;
    final public static int TIME_DAY = 3;
    final public static int TIME_MON = 4;
    final public static int TIME_YEAR = 5;

    final private static byte[] dayCounts = Util.explodeToBytes("31,28,31,30,31,30,31,31,30,31,30,31", ',', 10);

    final private static int[] monthIndexes = {
            Calendar.JANUARY,
            Calendar.FEBRUARY, Calendar.MARCH, Calendar.APRIL, Calendar.MAY,
            Calendar.JUNE, Calendar.JULY, Calendar.AUGUST, Calendar.SEPTEMBER,
            Calendar.OCTOBER, Calendar.NOVEMBER, Calendar.DECEMBER
    };

    static private int convertDateMonToSimpleMon(int dateMon) {
        for (int i = 0; i < monthIndexes.length; i++) {
            if (monthIndexes[i] == dateMon) {
                return i + 1;
            }
        }
        return -1;
    }

    public static String currentDate = getCurrentDate();

    /* Creates current date (GMT or local) */
    public static long createCurrentDate(boolean gmt) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        long result = createLongTime
                (
                        calendar.get(Calendar.YEAR),
                        convertDateMonToSimpleMon(calendar.get(Calendar.MONTH)),
                        calendar.get(Calendar.DAY_OF_MONTH),
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        calendar.get(Calendar.SECOND)
                );

        /* convert result to GMT time */
        long diff = Options.getInt(Options.OPTIONS_LOCAL_OFFSET);
        result += (diff * 3600);

        /* returns GMT or local time */
        return gmt ? result : gmtTimeToLocalTime(result);
    }

    /* Show date string */
    public static String getDateString(boolean onlyTime, boolean fullTime, long date) {
        if (date == 0) {
            return error_str;
        }

        int[] loclaDate = createDate(date);

        StringBuffer sb = new StringBuffer();

        if (!onlyTime) {
            sb.append(Util.makeTwo(loclaDate[TIME_DAY])).append('.').append(Util.makeTwo(loclaDate[TIME_MON]))
                    .append('.').append(loclaDate[TIME_YEAR]).append(' ');

            StringBuffer strBuf = new StringBuffer();
            strBuf.append(loclaDate[TIME_DAY]).append('.').append(loclaDate[TIME_MON]);
        }

        sb.append(Util.makeTwo(loclaDate[TIME_HOUR])).append(':').append(Util.makeTwo(loclaDate[TIME_MINUTE]));

        if (fullTime) {
            sb.append(':').append(Util.makeTwo(loclaDate[TIME_SECOND]));
        }

        return sb.toString();
    }

    private static String getCurrentDate() {
        long date = createCurrentDate(false);
        if (date == 0) {
            return error_str;
        }
        int[] loclaDate = createDate(date);
        StringBuffer sb = new StringBuffer();
        sb.append(loclaDate[TIME_DAY]).append('.').append(loclaDate[TIME_MON]);
        return sb.toString();
    }

    public static boolean nextDay() {
        if (!currentDate.equals(getCurrentDate())) {
            currentDate = getCurrentDate();
            return true;
        }
        return false;
    }

    /* Generates seconds count from 1st Jan 1970 till mentioned date */
    public static long createLongTime(int year, int mon, int day, int hour, int min, int sec) {
        int day_count, i, febCount;

        day_count = (year - 1970) * 365 + day;
        day_count += (year - 1968) / 4;
        if (year >= 2000) day_count--;

        if ((year % 4 == 0) && (year != 2000)) {
            day_count--;
            febCount = 29;
        } else {
            febCount = 28;
        }

        for (i = 0; i < mon - 1; i++) day_count += (i == 1) ? febCount : dayCounts[i];

        return day_count * 24L * 3600L + hour * 3600L + min * 60L + sec;
    }

    // Creates array of calendar values form value of seconds since 1st jan 1970 (GMT)
    public static int[] createDate(long value) {
        int total_days, last_days, i;
        int sec, min, hour, day, mon, year;

        sec = (int) (value % 60);

        min = (int) ((value / 60) % 60); // min
        value -= 60 * min;

        hour = (int) ((value / 3600) % 24); // hour
        value -= 3600 * hour;

        total_days = (int) (value / (3600 * 24));

        year = 1970;
        for (; ;) {
            last_days = total_days - ((year % 4 == 0) && (year != 2000) ? 366 : 365);
            if (last_days <= 0) break;
            total_days = last_days;
            year++;
        } // year

        int febrDays = ((year % 4 == 0) && (year != 2000)) ? 29 : 28;

        mon = 1;
        for (i = 0; i < 12; i++) {
            last_days = total_days - ((i == 1) ? febrDays : dayCounts[i]);
            if (last_days <= 0) break;
            mon++;
            total_days = last_days;
        } // mon

        day = total_days; // day

        return new int[]{sec, min, hour, day, mon, year};
    }

    public static String getDateString(boolean onlyTime, boolean fullTime) {
        return getDateString(onlyTime, fullTime, createCurrentDate(false));
    }

    public static long gmtTimeToLocalTime(long gmtTime) {
        long diff = Options.getInt(Options.OPTIONS_GMT_OFFSET);
        return gmtTime + diff * 3600L;
    }

    public static String longitudeToString(long seconds) {
        StringBuffer buf = new StringBuffer();
        int days = (int) (seconds / 86400);
        seconds %= 86400;
        int hours = (int) (seconds / 3600);
        seconds %= 3600;
        int minutes = (int) (seconds / 60);

        if (days != 0) {
            buf.append(days).append(' ').append(ResourceBundle.getString("days")).append(' ');
        }
        if (hours != 0) {
            buf.append(hours).append(' ').append(ResourceBundle.getString("time_hour")).append(' ');
        }
        if (minutes != 0) {
            buf.append(minutes).append(' ').append(ResourceBundle.getString("minutes"));
        }

        return buf.toString();
    }

    public static String getCurrentDay() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        String day = "monday";

        switch (cal.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY:
                day = "monday";
                break;
            case Calendar.TUESDAY:
                day = "tuesday";
                break;
            case Calendar.WEDNESDAY:
                day = "wednesday";
                break;
            case Calendar.THURSDAY:
                day = "thursday";
                break;
            case Calendar.FRIDAY:
                day = "friday";
                break;
            case Calendar.SATURDAY:
                day = "saturday";
                break;
            case Calendar.SUNDAY:
                day = "sunday";
                break;
        }
        return ResourceBundle.getString(day);
    }
}