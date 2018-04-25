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
public class Report implements Serializable {
    public String uid;
    public String monthYear;
    public long timestamp;
    public double expenseTotal;
    public double incomeTotal;
    public HashMap<String, Object> category = new HashMap<>();

    public Report() {
    }

    public Report(String uid, String monthYear, long timestamp) {
        this.uid = uid;
        this.monthYear = monthYear;
        this.timestamp = timestamp;
        this.expenseTotal = 0.0;
        this.incomeTotal = 0.0;
    }

    public Report(String uid, String date) {
        this.uid = uid;
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        Date mdate = new Date();
        try {
            mdate = df.parse(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+7:00"));
        cal.setTime(mdate);
        this.monthYear = (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR);
        this.timestamp = cal.getTimeInMillis();
        this.expenseTotal = 0.0;
        this.incomeTotal = 0.0;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("monthYear", monthYear);
        result.put("timestamp", timestamp);
        result.put("expenseTotal", expenseTotal);
        result.put("incomeTotal", incomeTotal);
        result.put("category", category);
        return result;
    }

    public void addDiary(Diary model){
        if(model.type.equalsIgnoreCase("Chi")){
            this.expenseTotal += model.amount;
            String fieldKey = Category.getFieldKey(model.category);
            if(this.category.get(fieldKey)!=null){
                double newValue = Double.parseDouble(this.category.get(fieldKey).toString()) + model.amount;
                this.category.put(fieldKey, newValue);
            }else{
                this.category.put(fieldKey, model.amount);
            }
        }else{
            this.incomeTotal += model.amount;
        }
    }

    public void minusDiary(Diary model){
        if(model.type.equalsIgnoreCase("Chi")){
            this.expenseTotal -= model.amount;
            String fieldKey = Category.getFieldKey(model.category);
            if(this.category.get(fieldKey)!=null){
                double newValue = Double.parseDouble(this.category.get(fieldKey).toString()) - model.amount;
                this.category.put(fieldKey, newValue);
            }
        }else{
            this.incomeTotal -= model.amount;
        }
    }
}
