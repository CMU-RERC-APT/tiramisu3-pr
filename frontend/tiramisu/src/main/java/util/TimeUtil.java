package main.java.util;

import java.util.Calendar;
import java.util.TimeZone;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class TimeUtil {
    public static int HOURS_IN_DAY = 24;
    public static int SECONDS_IN_HOUR = 3600;
    public static int SECONDS_IN_MINUTE = 60;
    public static long SECONDS_IN_DAY = 86400L;
    
    public static String getTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        return dateFormat.format(cal.getTime());
    }

    public static long getTimeMillis() {
        return System.currentTimeMillis();
    }

    public static String convertMillisToDate(long timeStamp, String timeZone) {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
    	if (timeZone!=null) dateFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeStamp);
        return dateFormat.format(cal.getTime());
    }

    /* Takes # of seconds after midnight, converts to HH:MM (24 hour clock, 0-23)*/
    public static String convertSecondsMidnight(int seconds) {
        if (seconds < 0) seconds += SECONDS_IN_HOUR * HOURS_IN_DAY;
        int hours = seconds / SECONDS_IN_HOUR;
        int minutes = (seconds - hours * SECONDS_IN_HOUR) / SECONDS_IN_MINUTE;
        return Integer.toString(hours) + ":" + Integer.toString(minutes);
    }
    
    /* Take the string representation of time and parse to posix time in seconds
     * Sample format of input (from PAAC): "YYYYMMDD HH:MM" 
     * */
    public static long converStringToPosix(String time, String timeFormat, String timeZone){
    	DateFormat dfm = new SimpleDateFormat(timeFormat);
    	if (timeZone!=null) dfm.setTimeZone(TimeZone.getTimeZone(timeZone));
    	long t;
		try {
			/* getTime() returns time in miliseconds*/
			t = dfm.parse(time).getTime()/1000;
	    	return t;
		} catch (ParseException e) {
			e.printStackTrace();
			return 0;
		}
    }
}
