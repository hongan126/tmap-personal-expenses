package tmap.iuh.personalexpenses.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Calendar;

@IgnoreExtraProperties
public class User {

    public String username;
    public String email;
    public double totalBalance;
    public double savingBalance;
    public double limitAmount;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String email) {
        this.username = username;
        this.email = email;
        this.totalBalance = 0;
        this.savingBalance = 0;
        this.limitAmount = 0;
    }

    public void setTotalBalance(double totalBalance) {
        this.totalBalance = totalBalance;
        updateLimitAmount();
    }

    public void setSavingBalance(double savingBalance) {
        this.savingBalance = savingBalance;
        updateLimitAmount();
    }

    public void updateLimitAmount() {
        Calendar calendar = Calendar.getInstance();
        int numDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH) - calendar.get(Calendar.DAY_OF_MONTH);
        this.limitAmount = (totalBalance - savingBalance) / numDays;
    }
}