package tmap.iuh.personalexpenses.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class MoneySource {
    public String uid; //User ID
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
}
