package ru.edu.pr07;

// R7: экспресс — вес до 10 кг, стоимость max(1000, distanceKm * 100) + 1% от суммы заказа
public class ExpressPolicy implements DeliveryPolicy {
    static final double MAX_WEIGHT_KG = 10.0;
    static final double MIN_COST = 1000.0;
    static final double COST_PER_KM = 100.0;
    static final double INSURANCE_RATE = 0.01;

    // TODO 3: supports
    @Override
    public boolean supports(DeliveryRequest req) {
        return req.type() == DeliveryType.EXPRESS && req.weightKg() <= MAX_WEIGHT_KG;
    }

    // TODO 4: calculateCost
    @Override
    public double calculateCost(DeliveryRequest req) {
        return Math.max(MIN_COST, req.distanceKm() * COST_PER_KM) + req.orderAmount() * INSURANCE_RATE;
    }
}
