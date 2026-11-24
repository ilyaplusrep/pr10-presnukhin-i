package ru.edu.pr07;

// Сервис: factory + полиморфный вызов, без if-else по типам доставки (R8)
public class DeliveryService {
    private final DeliveryPolicyFactory factory;

    public DeliveryService(DeliveryPolicyFactory f) {
        factory = f;
    }

    // Логика стартового проекта не менялась
    public double calculate(DeliveryRequest r) {
        DeliveryPolicy p = factory.get(r.type());
        if (!p.supports(r))
            throw new IllegalArgumentException("Unsupported delivery");
        return p.calculateCost(r);
    }

    // Вариант 16, часть Б: стоимость почтовой доставки для тех же параметров
    public static double addPostalCost(DeliveryService service, DeliveryRequest req) {
        if (service == null)
            throw new IllegalArgumentException("service is null");
        if (req == null)
            throw new IllegalArgumentException("request is null");
        DeliveryRequest postal = new DeliveryRequest(DeliveryType.POSTAL,
                req.distanceKm(), req.weightKg(), req.orderAmount());
        return service.calculate(postal);
    }
}
