package tmap.iuh.personalexpenses.models;

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
public class SavingPlan implements Serializable{
    public String uid;
    public String planName;
    public double targetAmount;
    public double savedAmount;
    public HashMap<String, Object> dueDate;
    public boolean done;

    public SavingPlan() {
    }

    public SavingPlan(String uid, String planName, double targetAmount, String dueDate) {
        this.uid = uid;
        this.planName = planName;
        this.targetAmount = targetAmount;
        this.savedAmount = 0.0;
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        Date mdate = new Date();
        try {
            mdate = df.parse(dueDate);
        }catch (Exception e){
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+7:00"));
        cal.setTime(mdate);
        this.dueDate = new HashMap<>();
        this.dueDate.put("timestamp", cal.getTimeInMillis());
        this.dueDate.put("init_date", dueDate);
        this.done = false;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("planName", planName);
        result.put("targetAmount", targetAmount);
        result.put("savedAmount", savedAmount);
        result.put("dueDate", dueDate);
        result.put("done", done);

        return result;
    }
}
