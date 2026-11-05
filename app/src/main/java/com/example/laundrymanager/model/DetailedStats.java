package com.example.laundrymanager.model;

import java.util.List;

public record DetailedStats(
        int orders,
        double revenue,
        double paid,
        double totalWeight,
        int customersCount,
        int servicesCount,
        List<ServiceStat> serviceBreakdown
) {
    public double unpaid() {
        return Math.max(0, revenue - paid);
    }

    public record ServiceStat(
            int serviceId,
            String serviceName,
            int orderCount,
            double totalWeight,
            double totalRevenue
    ) {}
}
