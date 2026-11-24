package ru.edu.pr07;

// R6: стандартный курьер — вес до 20 кг, стоимость max(500, distanceKm * 50)
public class StandardCourierPolicy implements DeliveryPolicy {
    static final double MAX_WEIGHT_KG = 20.0;
    static final double MIN_COST = 500.0;
    static final double COST_PER_KM = 50.0;

    // TODO 1: supports
    @Override
    public boolean supports(DeliveryRequest req) {
        return req.type() == DeliveryType.STANDARD && req.weightKg() <= MAX_WEIGHT_KG;
    }

    // TODO 2: calculateCost
    @Override
    public double calculateCost(DeliveryRequest req) {
        return Math.max(MIN_COST, req.distanceKm() * COST_PER_KM);
    }
}
