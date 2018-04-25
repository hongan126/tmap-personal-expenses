package tmap.iuh.personalexpenses.models;

import android.util.Log;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

@IgnoreExtraProperties
public class Diary implements Serializable {
    public String uid; // User ID
    public String msid; //Money source ID
    public String moneySourceName;
    public double amount;
    public String category;
    public String description;
    public String needLevel;
    public String type; //type of expense log
    public HashMap<String, Object> date;

    public Diary() {
    }

    public Diary(String uid, String moneySourceName, double amount, String category, String description, String needLevel, String type, String date) {
        this.uid = uid;
        this.moneySourceName = moneySourceName;
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.needLevel = needLevel;
        this.type = type;
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        Date mdate = new Date();
        try {
            mdate = df.parse(date);
        }catch (Exception e){
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(mdate);
        this.date = new HashMap<>();
        this.date.put("day", cal.get(Calendar.DAY_OF_MONTH));
        this.date.put("month", cal.get(Calendar.MONTH)+1);
        this.date.put("year", cal.get(Calendar.YEAR));
        this.date.put("timestamp", cal.getTimeInMillis());
        this.date.put("init_date", date);
        this.date.put("month_year", (cal.get(Calendar.MONTH)+1)+"/"+cal.get(Calendar.YEAR));
    }

    public Diary(String uid, String msid, String moneySourceName, double amount, String category, String description, String needLevel, String type, String date) {
        this.uid = uid;
        this.msid = msid;
        this.moneySourceName = moneySourceName;
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.needLevel = needLevel;
        this.type = type;
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        Date mdate = new Date();
        try {
            mdate = df.parse(date);
        }catch (Exception e){
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+7:00"));
        cal.setTime(mdate);
        this.date = new HashMap<>();
        this.date.put("day", cal.get(Calendar.DAY_OF_MONTH));
        this.date.put("month", cal.get(Calendar.MONTH)+1);
        this.date.put("year", cal.get(Calendar.YEAR));
        this.date.put("timestamp", cal.getTimeInMillis());
        this.date.put("init_date", date);
        this.date.put("month_year", (cal.get(Calendar.MONTH)+1)+"/"+cal.get(Calendar.YEAR));
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("msid", msid);
        result.put("moneySourceName", moneySourceName);
        result.put("amount", amount);
        result.put("category", category);
        result.put("description", description);
        result.put("needLevel", needLevel);
        result.put("type", type);
        result.put("date", date);

        return result;
    }
}
