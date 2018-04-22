package tmap.iuh.personalexpenses.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import tmap.iuh.personalexpenses.R;
import tmap.iuh.personalexpenses.models.Diary;
import tmap.iuh.personalexpenses.models.MoneySource;

public class MoneySourceViewHolder extends RecyclerView.ViewHolder {

    public TextView moneySourceName;
    public TextView currentBalance;
    public ImageButton moreButton;

    public MoneySourceViewHolder(View itemView) {
        super(itemView);
        moneySourceName = itemView.findViewById(R.id.money_src_name_item);
        currentBalance = itemView.findViewById(R.id.current_balance_monesrc_item);
        moreButton = itemView.findViewById(R.id.menu_moneysrc_image_button);

    }

    public void bindToMoneySource(MoneySource model, View.OnClickListener moreClickListener) {
        moneySourceName.setText(model.moneySourceName);
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        formatter.applyPattern("#,###,###,###");
        currentBalance.setText(formatter.format(model.currentBalance));

        moreButton.setOnClickListener(moreClickListener);
    }

    public ImageButton getImageButton(){
        return moreButton;
    }
}
