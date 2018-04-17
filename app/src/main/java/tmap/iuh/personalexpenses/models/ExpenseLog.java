package tmap.iuh.personalexpenses.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;

@IgnoreExtraProperties
public class ExpenseLog {
    public String uid; // User ID
    public String msid; // Money source ID
    public String moneySourceName;
    public double amount;
    public String category;
    public String description;
    public String needLevel;
    public String type; //type of expense log
    public Date date;

    public ExpenseLog() {
    }

    public ExpenseLog(String uid, String msid, String moneySourceName, double amount, String category, String description, String needLevel, String type, Date date) {
        this.uid = uid;
        this.msid = msid;
        this.moneySourceName = moneySourceName;
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.needLevel = needLevel;
        this.type = type;
        this.date = date;
    }
}
