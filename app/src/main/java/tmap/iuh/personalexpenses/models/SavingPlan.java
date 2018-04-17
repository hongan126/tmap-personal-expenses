package tmap.iuh.personalexpenses.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;

@IgnoreExtraProperties
public class SavingPlan {
    public String uid;
    public String planName;
    public double targetAmount;
    public double savedAmount;
    public Date dueDate;

    public SavingPlan() {
    }

    public SavingPlan(String uid, String planName, double targetAmount, double savedAmount, Date dueDate) {
        this.uid = uid;
        this.planName = planName;
        this.targetAmount = targetAmount;
        this.savedAmount = savedAmount;
        this.dueDate = dueDate;
    }
}
