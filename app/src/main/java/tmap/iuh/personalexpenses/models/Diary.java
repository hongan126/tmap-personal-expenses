package tmap.iuh.personalexpenses.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Diary {
    public String uid; // User ID
    public String moneySourceName;
    public double amount;
    public String category;
    public String description;
    public String needLevel;
    public String type; //type of expense log
    public String date;

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
        this.date = date;
    }

    // [START post_to_map]
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
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
