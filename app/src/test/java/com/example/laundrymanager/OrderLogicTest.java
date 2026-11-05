package com.example.laundrymanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.laundrymanager.model.Customer;
import com.example.laundrymanager.model.DetailedStats;
import com.example.laundrymanager.model.Order;
import com.example.laundrymanager.model.OrderStatus;
import com.example.laundrymanager.model.PaymentStatus;
import com.example.laundrymanager.model.Service;
import com.example.laundrymanager.model.Stats;

import java.util.Collections;

import org.junit.Test;

public class OrderLogicTest {

    @Test
    public void testServicePriceCalculation() {
        Service service = new Service(1, "Giặt sấy", 20000);
        double total = service.priceFor(3.5);
        assertEquals(70000.0, total, 0.001);
    }

    @Test
    public void testOrderOverdueDetection() {
        Customer customer = new Customer(1, "Nguyen Van A", "0901234567", "Hanoi");
        Service service = new Service(1, "Giặt sấy", 20000);

        long now = System.currentTimeMillis();
        long pastDueDate = now - 3600 * 1000L; // 1 giờ trước
        long futureDueDate = now + 3600 * 1000L; // 1 giờ sau

        // Đơn quá hạn và đang giặt -> isOverdue phải true
        Order overdueOrder = new Order(1, customer, service, 2.0, 40000, "",
                OrderStatus.WASHING, PaymentStatus.PAID, now - 7200 * 1000L, pastDueDate);
        assertTrue("Đơn quá giờ hẹn và chưa giao phải đánh dấu trễ hẹn", overdueOrder.isOverdue());

        // Đơn quá hạn nhưng ĐÃ GIAO -> isOverdue phải false
        Order deliveredOrder = new Order(2, customer, service, 2.0, 40000, "",
                OrderStatus.DELIVERED, PaymentStatus.PAID, now - 7200 * 1000L, pastDueDate);
        assertFalse("Đơn đã giao không được tính là trễ hẹn", deliveredOrder.isOverdue());

        // Đơn chưa tới giờ hẹn -> isOverdue phải false
        Order onTimeOrder = new Order(3, customer, service, 2.0, 40000, "",
                OrderStatus.WASHING, PaymentStatus.PAID, now, futureDueDate);
        assertFalse("Đơn chưa tới hạn không được tính là trễ hẹn", onTimeOrder.isOverdue());
    }

    @Test
    public void testStatsUnpaidCalculation() {
        Stats stats = new Stats(10, 500000, 350000, 8, 4);
        assertEquals(150000.0, stats.unpaid(), 0.001);

        DetailedStats detailedStats = new DetailedStats(10, 500000, 350000, 25.0, 8, 4, Collections.emptyList());
        assertEquals(150000.0, detailedStats.unpaid(), 0.001);
    }
}
