package us.mn.state.dot.mnroad;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.*;

/**
 * This class collects the data for one row of values
 * A row includes all sensors polled for the same cell,day,hour,qhr, and minute
 */
public class ValueData {
    private int cell;
    private Date day;
    private int hour;
    private int qhr;
    private int minute;
    private String qualityFlag;
    LinkedHashMap hm;

    static java.text.DecimalFormat df = new java.text.DecimalFormat("###.#####");
    static String defaultQualityFlag = "1000000000000000";

    ValueData(int c_cell, Date c_day, int c_hour, int c_qhr, int c_minute) {
        cell = c_cell;
        day = c_day;
        hour = c_hour;
        qhr = c_qhr;
        minute = c_minute;
        qualityFlag = defaultQualityFlag;
        hm = new LinkedHashMap();
    }

    ValueData(int c_cell, Date c_day, int c_hour, int c_qhr, int c_minute, String c_qualityFlag) {
        cell = c_cell;
        day = c_day;
        hour = c_hour;
        qhr = c_qhr;
        minute = c_minute;
        qualityFlag = c_qualityFlag;
        hm = new LinkedHashMap();
    }

    public void addValue (String key, BigDecimal d) {
        if (d == null)
            hm.put(key, d);
        else
            hm.put(key, new Double(d.doubleValue()));
    }

    public String[] toStringArray() {
        ArrayList al = new ArrayList();
        al.add(String.format("%d",cell));
        al.add(String.format("%s",day));
        al.add(String.format("%d",hour));
        al.add(String.format("%d",qhr));
        al.add(String.format("%d",minute));
        al.add(String.format("\"%s\"",qualityFlag==null?defaultQualityFlag:qualityFlag));
        al.addAll(hm.values());
        return MrUtils.toArrayOfStrings(al);
    }

    public String toString() {
        StringBuffer values = new StringBuffer();
        for (Object value : hm.values()) {
            if (value == null) {
                values.append(",");
            } else {
                values.append(String.format(",%s",df.format(value)));
            }
        }
        return String.format("%d,%s,%d,%d,%d,%s,%s", cell, day, hour, qhr, minute, qualityFlag==null?defaultQualityFlag:qualityFlag, values.toString());
    }

    public boolean hasValues() {
        return hm.keySet().size() > 0;
    }
}
