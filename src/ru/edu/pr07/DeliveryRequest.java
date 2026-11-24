package ru.edu.pr07;

/**
 * Запрос на расчёт стоимости доставки (иммутабельный record).
 * Компоненты не изменены; компактный конструктор только проверяет,
 * что запрос физически возможен (тип задан, числа конечные и не отрицательные).
 */
public record DeliveryRequest(DeliveryType type, double distanceKm, double weightKg, double orderAmount) {

    public DeliveryRequest {
        if (type == null)
            throw new IllegalArgumentException("type is null");
        checkNonNegative(distanceKm, "distanceKm");
        checkNonNegative(weightKg, "weightKg");
        checkNonNegative(orderAmount, "orderAmount");
    }

    private static void checkNonNegative(double value, String field) {
        if (Double.isNaN(value) || Double.isInfinite(value) || value < 0)
            throw new IllegalArgumentException(field + " must be >= 0, got " + value);
    }
}
