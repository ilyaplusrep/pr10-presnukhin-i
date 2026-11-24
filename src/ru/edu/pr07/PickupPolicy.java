package ru.edu.pr07;

// R5: самовывоз всегда возможен и бесплатен (логика стартового проекта не менялась)
public class PickupPolicy implements DeliveryPolicy {
    @Override
    public boolean supports(DeliveryRequest r) {
        return true;
    }

    @Override
    public double calculateCost(DeliveryRequest r) {
        return 0;
    }
}
