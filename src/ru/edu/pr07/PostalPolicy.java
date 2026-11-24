package ru.edu.pr07;

// Вариант 16, часть А: почтовая доставка — вес до 2 кг, стоимость max(300, distanceKm * 20)
public class PostalPolicy implements DeliveryPolicy {
    static final double MAX_WEIGHT_KG = 2.0;
    static final double MIN_COST = 300.0;
    static final double COST_PER_KM = 20.0;

    @Override
    public boolean supports(DeliveryRequest req) {
        return req.type() == DeliveryType.POSTAL && req.weightKg() <= MAX_WEIGHT_KG;
    }

    @Override
    public double calculateCost(DeliveryRequest req) {
        return Math.max(MIN_COST, req.distanceKm() * COST_PER_KM);
    }
}
