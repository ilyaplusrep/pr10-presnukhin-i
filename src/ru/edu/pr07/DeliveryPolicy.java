package ru.edu.pr07;

// R1: единый контракт для всех способов доставки
public interface DeliveryPolicy {
    boolean supports(DeliveryRequest request);

    double calculateCost(DeliveryRequest request);
}
