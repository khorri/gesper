package  com.axelor.rh.utils;


import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class Utils{

    public static LocalDate convertString2Date(String date, String format) {
        DateTimeFormatter formatter= DateTimeFormat.forPattern(format);
        return formatter.parseLocalDate(date);
    }

}