package tmap.iuh.personalexpenses;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class MoneyTextWatcher implements TextWatcher {
    private final WeakReference<EditText> editTextWeakReference;

    public MoneyTextWatcher(EditText editText) {
        editTextWeakReference = new WeakReference<EditText>(editText);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable editable) {
        EditText editText = editTextWeakReference.get();
        if (editText == null) return;
        String s = editable.toString();
        if (s.equals("")) return;
        editText.removeTextChangedListener(this);
        String cleanString = s.toString().replaceAll("[$,.]", "");
        long longval;
        try {
            longval = Long.parseLong(cleanString);
        } catch (Exception e) {
            e.printStackTrace();
            cleanString = cleanString.substring(0, cleanString.length() - 1);
            longval = Long.parseLong(cleanString);
        }

        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        formatter.applyPattern("#,###,###,###");
        String formattedString = formatter.format(longval);
        editText.setText(formattedString);
        editText.setSelection(formattedString.length());
        editText.addTextChangedListener(this);
    }
}
