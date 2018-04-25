package tmap.iuh.personalexpenses.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import tmap.iuh.personalexpenses.R;
import tmap.iuh.personalexpenses.models.MoneySource;
import tmap.iuh.personalexpenses.models.Report;

public class ReportViewHolder extends RecyclerView.ViewHolder {

    public TextView montthReportHead;
    public TextView incomeTotal;
    public TextView expenseTotal;
    public ImageButton moreButton;

    public ReportViewHolder(View itemView) {
        super(itemView);
        montthReportHead = itemView.findViewById(R.id.head_month_report_item_textview);
        incomeTotal = itemView.findViewById(R.id.income_of_month_report_item_textview);
        expenseTotal = itemView.findViewById(R.id.expense_of_month_report_item_textview);
        moreButton = itemView.findViewById(R.id.image_button_item_report);
    }

    public void bindToReport(Report model, View.OnClickListener moreClickListener) {
        montthReportHead.setText(("Tháng " + model.monthYear));
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        formatter.applyPattern("#,###,###,###");
        incomeTotal.setText((formatter.format(model.incomeTotal)+"đ"));
        expenseTotal.setText((formatter.format(model.expenseTotal)+"đ"));

        moreButton.setOnClickListener(moreClickListener);
    }

    public ImageButton getImageButton() {
        return moreButton;
    }
}
