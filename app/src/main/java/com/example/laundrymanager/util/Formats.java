package com.example.laundrymanager.util;

import android.content.Context;

import com.example.laundrymanager.R;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Định dạng hiển thị theo chuẩn Việt Nam. */
public final class Formats {
    private static final Locale VN = Locale.forLanguageTag("vi-VN");

    private Formats() {
    }

    /** Số tiền dạng "15.000" (không đơn vị). */
    public static String amount(double value) {
        NumberFormat format = NumberFormat.getNumberInstance(VN);
        format.setMaximumFractionDigits(0);
        return format.format(value);
    }

    /** Số tiền kèm đơn vị, ví dụ "15.000 VNĐ". */
    public static String money(Context context, double value) {
        return context.getString(R.string.money_vnd, amount(value));
    }

    public static String weight(double kg) {
        NumberFormat format = NumberFormat.getNumberInstance(VN);
        format.setMaximumFractionDigits(2);
        return format.format(kg);
    }

    public static String dateTime(long millis) {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm", VN).format(new Date(millis));
    }
}

