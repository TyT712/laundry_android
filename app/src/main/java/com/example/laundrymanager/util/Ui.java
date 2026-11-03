package com.example.laundrymanager.util;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.laundrymanager.model.Labeled;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Các tiện ích dựng giao diện dùng chung. */
public final class Ui {

    /** Chuyển một phần tử thành chuỗi hiển thị. */
    public interface Text<T> {
        String of(T item);
    }

    private Ui() {
    }

    public static String text(TextView view) {
        return view.getText().toString().trim();
    }

    /** Chuỗi số thực dương (chấp nhận dấu phẩy), hoặc {@code null} nếu không hợp lệ. */
    public static Double positiveNumber(String raw) {
        try {
            double value = Double.parseDouble(raw.trim().replace(',', '.'));
            return value > 0 ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Số thực không có phần thập phân thừa, ví dụ 3.0 → "3". */
    public static String plainNumber(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    public static <T> ArrayAdapter<String> listAdapter(Context context, List<T> items, Text<T> text) {
        return adapter(context, android.R.layout.simple_list_item_1, items, text);
    }

    public static <T> ArrayAdapter<String> spinnerAdapter(Context context, List<T> items, Text<T> text) {
        return adapter(context, android.R.layout.simple_spinner_dropdown_item, items, text);
    }

    public static <E extends Enum<E> & Labeled> ArrayAdapter<String> enumAdapter(Context context, E[] values) {
        List<String> labels = new ArrayList<>();
        for (E value : values) {
            labels.add(context.getString(value.label()));
        }
        return new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, labels);
    }

    public static <T> T selected(Spinner spinner, List<T> items) {
        int position = spinner.getSelectedItemPosition();
        return position >= 0 && position < items.size() ? items.get(position) : null;
    }

    public static <T> T selected(Spinner spinner, T[] items) {
        int position = spinner.getSelectedItemPosition();
        return position >= 0 && position < items.length ? items[position] : null;
    }

    public static void onTextChanged(EditText field, Runnable action) {
        field.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                action.run();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public static void onItemSelected(Spinner spinner, Runnable action) {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                action.run();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private static <T> ArrayAdapter<String> adapter(Context context, int layout, List<T> items, Text<T> text) {
        List<String> rows = new ArrayList<>(items.size());
        for (T item : items) {
            rows.add(text.of(item));
        }
        return new ArrayAdapter<>(context, layout, rows);
    }
}

