package tmap.iuh.personalexpenses.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import tmap.iuh.personalexpenses.R;
import tmap.iuh.personalexpenses.models.Diary;

public class DiaryViewHolder extends RecyclerView.ViewHolder {

    public TextView date;
    public TextView typeDiary;
    public TextView category;
    public TextView description;
    public TextView amount;
    public TextView moneySource;

    public DiaryViewHolder(View itemView) {
        super(itemView);

        date = itemView.findViewById(R.id.date_diary_item);
        typeDiary = itemView.findViewById(R.id.type_diary_item);
        category = itemView.findViewById(R.id.category_diary_item);
        description = itemView.findViewById(R.id.description_diary_item);
        amount = itemView.findViewById(R.id.amount_diary_item);
        moneySource = itemView.findViewById(R.id.money_source_diary_item);
    }

    public void bindToDiary(Diary diary) {
        date.setText(diary.date.get("init_date").toString());
        typeDiary.setText(diary.type + ": ");
        category.setText(diary.category);
        description.setText(diary.description);
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        formatter.applyPattern("#,###,###,###");
        amount.setText(formatter.format((diary.amount)));
        moneySource.setText(diary.moneySourceName);
    }
}
