package tmap.iuh.personalexpenses.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class MoneySource{
    public String uid; //User ID
    public String msid; //Money source ID
    public String moneySourceName;
    public double firstBalance;
    public double currentBalance;

    public MoneySource() {
    }

    public MoneySource(String uid, String moneySourceName, double firstBalance, double currentBalance) {
        this.uid = uid;
        this.moneySourceName = moneySourceName;
        this.firstBalance = firstBalance;
        this.currentBalance = currentBalance;
    }

    public MoneySource(String uid, String moneySourceName, double firstBalance) {
        this.uid = uid;
        this.moneySourceName = moneySourceName;
        this.firstBalance = firstBalance;
        this.currentBalance = firstBalance;
    }

    public MoneySource(String uid, String moneySourceName, double firstBalance, String msid) {
        this.uid = uid;
        this.moneySourceName = moneySourceName;
        this.firstBalance = firstBalance;
        this.currentBalance = firstBalance;
        this.msid = msid;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("moneySourceName", moneySourceName);
        result.put("firstBalance", firstBalance);
        result.put("currentBalance", currentBalance);
        result.put("msid", msid);
        return result;
    }
}
