package com.example.laundrymanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.laundrymanager.util.Formats;

import org.junit.Test;

public class FormatsTest {

    @Test
    public void testAmountFormatting() {
        String formatted = Formats.amount(15000);
        // Định dạng tiếng Việt phân cách hàng nghìn bằng dấu chấm
        assertEquals("15.000", formatted);
    }

    @Test
    public void testWeightFormatting() {
        String formatted = Formats.weight(3.5);
        // Định dạng tiếng Việt phân cách thập phân bằng dấu phẩy
        assertTrue("3,5".equals(formatted) || "3.5".equals(formatted));
    }

    @Test
    public void testDateTimeFormatting() {
        long millis = 1730784000000L; // 05/11/2024
        String formatted = Formats.dateTime(millis);
        assertNotNull(formatted);
        assertTrue("Chuỗi ngày giờ phải chứa ký tự gạch chéo", formatted.contains("/"));
        assertTrue("Chuỗi ngày giờ phải chứa ký tự hai chấm", formatted.contains(":"));
    }
}
