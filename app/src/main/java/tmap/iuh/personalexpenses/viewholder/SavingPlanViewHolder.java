package tmap.iuh.personalexpenses.viewholder;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import tmap.iuh.personalexpenses.R;
import tmap.iuh.personalexpenses.models.MoneySource;
import tmap.iuh.personalexpenses.models.SavingPlan;

public class SavingPlanViewHolder extends RecyclerView.ViewHolder {

    public TextView planName;
    public TextView targetAmount;
    public TextView planProgress;
    public TextView dueDate;
    public ImageButton moreButton;

    public SavingPlanViewHolder(View itemView) {
        super(itemView);
        planName = itemView.findViewById(R.id.plan_name_item);
        targetAmount = itemView.findViewById(R.id.amount_plan_item_text_view);
        planProgress = (TextView) itemView.findViewById(R.id.progress_plan_item_text_view);
        dueDate = itemView.findViewById(R.id.due_date_plan_item_textview);
        moreButton = itemView.findViewById(R.id.menu_plan_image_button);

    }

    public void bindToSavingPlan(SavingPlan model, View.OnClickListener moreClickListener) {
        planName.setText(model.planName);

        if (model.targetAmount == model.savedAmount) {
            planProgress.setText(R.string.done);
            planProgress.setTextColor(Color.parseColor("#7cb342"));
        } else {
            DecimalFormat f = new DecimalFormat("0.0");
            Double progress = (model.savedAmount / model.targetAmount) * 100.0;
            String progressStr = f.format(progress) + "%";
            planProgress.setText(progressStr);
            planProgress.setTextColor(Color.parseColor("#c41c00"));
        }

        dueDate.setText(model.dueDate.get("init_date").toString());

        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        formatter.applyPattern("#,###,###,###");
        targetAmount.setText(formatter.format(model.targetAmount));

        moreButton.setOnClickListener(moreClickListener);
    }

    public ImageButton getImageButton() {
        return moreButton;
    }
}
