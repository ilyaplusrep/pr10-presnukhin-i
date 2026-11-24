package ru.edu.pr07;

// R4: фабрика только выбирает реализацию, формул здесь нет
public class DeliveryPolicyFactory {

    // Задание 5 каркаса: switch по enum без default — компилятор требует ветку для каждого значения (R9)
    public DeliveryPolicy get(DeliveryType type) {
        if (type == null)
            throw new IllegalArgumentException("type is null");
        return switch (type) {
            case STANDARD -> new StandardCourierPolicy();
            case EXPRESS -> new ExpressPolicy();
            case PICKUP -> new PickupPolicy();
            case POSTAL -> new PostalPolicy();   // вариант 16
        };
    }
}
