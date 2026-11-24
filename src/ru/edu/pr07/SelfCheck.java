package ru.edu.pr07;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public class SelfCheck {
    private static final String DATA_FILE = "data/sample.csv";
    private static final double EPS = 0.001;

    private static final DeliveryPolicyFactory FACTORY = new DeliveryPolicyFactory();
    private static final DeliveryService SERVICE = new DeliveryService(FACTORY);

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        runUnitChecks();
        System.out.println("Unit: " + passed + "/" + (passed + failed) + " passed");

        int passedBefore = passed;
        int failedBefore = failed;
        runDataChecks();
        int dataPassed = passed - passedBefore;
        int dataTotal = dataPassed + (failed - failedBefore);
        System.out.println("Data-driven: " + dataPassed + "/" + dataTotal + " passed");

        if (args.length > 0 && (args[0].equals("-i") || args[0].equals("--interactive"))) {
            runInteractive();
        }
    }

    // ---------- Режим 1: unit-проверки ----------

    private static void runUnitChecks() {
        DeliveryPolicy pickup = new PickupPolicy();
        DeliveryPolicy standard = new StandardCourierPolicy();
        DeliveryPolicy express = new ExpressPolicy();
        DeliveryPolicy postal = new PostalPolicy();

        // 1–2: PickupPolicy (R5)
        try {
            boolean ok = pickup.supports(req(DeliveryType.PICKUP, 50, 300, 1000))
                    && pickup.supports(req(DeliveryType.PICKUP, 0, 0, 0));
            expect("1 PickupPolicy.supports: любой запрос", ok, "false");
        } catch (RuntimeException e) {
            unexpected("1 PickupPolicy.supports", e);
        }
        try {
            double cost = pickup.calculateCost(req(DeliveryType.PICKUP, 50, 30, 50000));
            expectCost("2 PickupPolicy.calculateCost: всегда 0.0", 0.0, cost);
        } catch (RuntimeException e) {
            unexpected("2 PickupPolicy.calculateCost", e);
        }

        // 3–7: StandardCourierPolicy (R6)
        try {
            expect("3 Standard.supports: STANDARD, вес 15",
                    standard.supports(req(DeliveryType.STANDARD, 10, 15, 1000)), "false");
        } catch (RuntimeException e) {
            unexpected("3 Standard.supports", e);
        }
        try {
            expect("4 Standard.supports: EXPRESS — чужой тип",
                    !standard.supports(req(DeliveryType.EXPRESS, 10, 5, 1000)), "true");
        } catch (RuntimeException e) {
            unexpected("4 Standard.supports", e);
        }
        try {
            expect("5 Standard.supports: вес 25 > 20",
                    !standard.supports(req(DeliveryType.STANDARD, 10, 25, 1000)), "true");
        } catch (RuntimeException e) {
            unexpected("5 Standard.supports", e);
        }
        try {
            expectCost("6 Standard.calculateCost: 12 км → max(500, 600)", 600.0,
                    standard.calculateCost(req(DeliveryType.STANDARD, 12, 4.5, 12000)));
        } catch (RuntimeException e) {
            unexpected("6 Standard.calculateCost", e);
        }
        try {
            expectCost("7 Standard.calculateCost: 5 км → минимум 500", 500.0,
                    standard.calculateCost(req(DeliveryType.STANDARD, 5, 1, 500)));
        } catch (RuntimeException e) {
            unexpected("7 Standard.calculateCost", e);
        }

        // 8–11: ExpressPolicy (R7)
        try {
            expect("8 Express.supports: EXPRESS, вес 5",
                    express.supports(req(DeliveryType.EXPRESS, 5, 5, 1000)), "false");
        } catch (RuntimeException e) {
            unexpected("8 Express.supports", e);
        }
        try {
            expect("9 Express.supports: вес 15 > 10",
                    !express.supports(req(DeliveryType.EXPRESS, 5, 15, 1000)), "true");
        } catch (RuntimeException e) {
            unexpected("9 Express.supports", e);
        }
        try {
            expectCost("10 Express.calculateCost: 5 км, заказ 4000 → 1000 + 40", 1040.0,
                    express.calculateCost(req(DeliveryType.EXPRESS, 5, 1.2, 4000)));
        } catch (RuntimeException e) {
            unexpected("10 Express.calculateCost", e);
        }
        try {
            expectCost("11 Express.calculateCost: 20 км, заказ 10000 → 2000 + 100", 2100.0,
                    express.calculateCost(req(DeliveryType.EXPRESS, 20, 5, 10000)));
        } catch (RuntimeException e) {
            unexpected("11 Express.calculateCost", e);
        }

        // 12–14: DeliveryPolicyFactory (R4)
        try {
            DeliveryPolicy p = FACTORY.get(DeliveryType.STANDARD);
            expect("12 Factory.get(STANDARD) → StandardCourierPolicy", p instanceof StandardCourierPolicy, name(p));
        } catch (RuntimeException e) {
            unexpected("12 Factory.get(STANDARD)", e);
        }
        try {
            DeliveryPolicy p = FACTORY.get(DeliveryType.EXPRESS);
            expect("13 Factory.get(EXPRESS) → ExpressPolicy", p instanceof ExpressPolicy, name(p));
        } catch (RuntimeException e) {
            unexpected("13 Factory.get(EXPRESS)", e);
        }
        try {
            DeliveryPolicy p = FACTORY.get(DeliveryType.PICKUP);
            expect("14 Factory.get(PICKUP) → PickupPolicy", p instanceof PickupPolicy, name(p));
        } catch (RuntimeException e) {
            unexpected("14 Factory.get(PICKUP)", e);
        }

        // 15–18: DeliveryService (R2, R3, R8)
        try {
            expectCost("15 Service: STANDARD 12 км, 4.5 кг", 600.0,
                    SERVICE.calculate(req(DeliveryType.STANDARD, 12, 4.5, 12000)));
        } catch (RuntimeException e) {
            unexpected("15 Service: STANDARD", e);
        }
        try {
            SERVICE.calculate(req(DeliveryType.STANDARD, 10, 25, 5000));
            noError("16 Service: STANDARD 25 кг не поддерживается (R3)");
        } catch (IllegalArgumentException e) {
            checkMessage("16 Service: STANDARD 25 кг не поддерживается (R3)", e, "Unsupported delivery");
        }
        try {
            expectCost("17 Service: PICKUP, 0 км", 0.0, SERVICE.calculate(req(DeliveryType.PICKUP, 0, 10, 8000)));
        } catch (RuntimeException e) {
            unexpected("17 Service: PICKUP", e);
        }
        try {
            expectCost("18 Service: EXPRESS 5 км, 1.2 кг, заказ 4000", 1040.0,
                    SERVICE.calculate(req(DeliveryType.EXPRESS, 5, 1.2, 4000)));
        } catch (RuntimeException e) {
            unexpected("18 Service: EXPRESS", e);
        }

        // дополнительные проверки
        try {
            expect("доп: Standard.supports: вес 20 — граница включена",
                    standard.supports(req(DeliveryType.STANDARD, 1, 20, 0)), "false");
        } catch (RuntimeException e) {
            unexpected("доп: Standard граница", e);
        }
        try {
            expect("доп: Express.supports: вес 10 — граница включена",
                    express.supports(req(DeliveryType.EXPRESS, 1, 10, 0)), "false");
        } catch (RuntimeException e) {
            unexpected("доп: Express граница", e);
        }
        try {
            req(DeliveryType.STANDARD, -1, 1, 100);
            noError("доп: DeliveryRequest с distanceKm < 0");
        } catch (IllegalArgumentException e) {
            checkMessage("доп: DeliveryRequest с distanceKm < 0", e, "distanceKm must be >= 0");
        }
        try {
            DeliveryType.valueOf("DRONE");
            noError("доп: DeliveryType.valueOf(\"DRONE\")");
        } catch (IllegalArgumentException e) {
            checkMessage("доп: DeliveryType.valueOf(\"DRONE\")", e, "No enum constant");
        }

        // вариант 16, часть А: POSTAL + PostalPolicy
        try {
            DeliveryPolicy p = FACTORY.get(DeliveryType.POSTAL);
            expect("А: Factory.get(POSTAL) → PostalPolicy", p instanceof PostalPolicy, name(p));
        } catch (RuntimeException e) {
            unexpected("А: Factory.get(POSTAL)", e);
        }
        try {
            boolean ok = postal.supports(req(DeliveryType.POSTAL, 10, 2, 1000))
                    && !postal.supports(req(DeliveryType.POSTAL, 10, 2.1, 1000))
                    && !postal.supports(req(DeliveryType.STANDARD, 10, 1, 1000));
            expect("А: Postal.supports: вес 2 да, 2.1 нет, чужой тип нет", ok, "false");
        } catch (RuntimeException e) {
            unexpected("А: Postal.supports", e);
        }
        try {
            expectCost("А: Postal.calculateCost: 10 км → минимум 300", 300.0,
                    postal.calculateCost(req(DeliveryType.POSTAL, 10, 1, 5000)));
        } catch (RuntimeException e) {
            unexpected("А: Postal минимум", e);
        }
        try {
            expectCost("А: Service: POSTAL 30 км, 1.5 кг → 600", 600.0,
                    SERVICE.calculate(req(DeliveryType.POSTAL, 30, 1.5, 5000)));
        } catch (RuntimeException e) {
            unexpected("А: Service POSTAL", e);
        }
        try {
            SERVICE.calculate(req(DeliveryType.POSTAL, 30, 3, 5000));
            noError("А: Service: POSTAL 3 кг не поддерживается");
        } catch (IllegalArgumentException e) {
            checkMessage("А: Service: POSTAL 3 кг не поддерживается", e, "Unsupported delivery");
        }

        // вариант 16, часть Б: addPostalCost
        try {
            expectCost("Б: addPostalCost для STANDARD-запроса 30 км, 1.5 кг → 600", 600.0,
                    DeliveryService.addPostalCost(SERVICE, req(DeliveryType.STANDARD, 30, 1.5, 5000)));
        } catch (RuntimeException e) {
            unexpected("Б: addPostalCost 30 км", e);
        }
        try {
            expectCost("Б: addPostalCost для EXPRESS-запроса 5 км → 300", 300.0,
                    DeliveryService.addPostalCost(SERVICE, req(DeliveryType.EXPRESS, 5, 0.5, 9000)));
        } catch (RuntimeException e) {
            unexpected("Б: addPostalCost 5 км", e);
        }
        try {
            DeliveryService.addPostalCost(SERVICE, req(DeliveryType.STANDARD, 30, 4.5, 5000));
            noError("Б: addPostalCost, 4.5 кг — почта не возьмёт");
        } catch (IllegalArgumentException e) {
            checkMessage("Б: addPostalCost, 4.5 кг — почта не возьмёт", e, "Unsupported delivery");
        }
        try {
            DeliveryService.addPostalCost(SERVICE, null);
            noError("Б: addPostalCost(service, null)");
        } catch (IllegalArgumentException e) {
            checkMessage("Б: addPostalCost(service, null)", e, "request is null");
        }
    }

    private static DeliveryRequest req(DeliveryType type, double distance, double weight, double order) {
        return new DeliveryRequest(type, distance, weight, order);
    }

    private static String name(Object o) {
        return o == null ? "null" : o.getClass().getSimpleName();
    }

    // ---------- Режим 2: data-driven по sample.csv ----------

    private static void runDataChecks() {
        List<String> lines;
        try {
            lines = Files.readAllLines(Path.of(DATA_FILE));
        } catch (IOException e) {
            report("data: чтение " + DATA_FILE, false, e.getMessage());
            return;
        }

        int caseNumber = 0;
        for (int i = 1; i < lines.size(); i++) {   // строка 0 — заголовок
            String row = lines.get(i);
            if (row.isBlank() || row.startsWith("#")) {
                continue;
            }
            caseNumber++;
            int lastSeparator = row.lastIndexOf(';');
            String request = row.substring(0, lastSeparator);
            String expected = row.substring(lastSeparator + 1);
            String actual = evaluate(request);
            report("data/" + caseNumber + " (" + row + ")", sameResult(expected, actual),
                    "ожидалось " + expected + ", получено " + actual);
        }
    }

    // "type;distanceKm;weightKg;orderAmount" → "OK:cost=N" или "SKIP:причина"
    private static String evaluate(String line) {
        DeliveryRequest request;
        try {
            request = parseRequest(line.split(";", -1));
        } catch (IllegalArgumentException e) {
            return "SKIP:" + e.getMessage();
        }
        try {
            return "OK:cost=" + SERVICE.calculate(request);
        } catch (IllegalArgumentException e) {
            // supports() вернул false: тип выбран фабрикой, значит не прошло ограничение политики по весу
            return "SKIP:weight exceeds limit";
        }
    }

    private static boolean sameResult(String expected, String actual) {
        if (expected.startsWith("OK:cost=") && actual.startsWith("OK:cost=")) {
            double e = Double.parseDouble(expected.substring(8));
            double a = Double.parseDouble(actual.substring(8));
            return Math.abs(a - e) < EPS;
        }
        if (expected.startsWith("SKIP:") && actual.startsWith("SKIP:")) {
            return actual.startsWith(expected);
        }
        return false;
    }

    private static DeliveryRequest parseRequest(String[] parts) {
        if (parts.length != 4) {
            throw new IllegalArgumentException("expected 4 values, got " + parts.length);
        }
        DeliveryType type = parseType(parts[0]);
        double distance = parseNumber(parts[1], "distanceKm");
        double weight = parseNumber(parts[2], "weightKg");
        double order = parseNumber(parts[3], "orderAmount");
        return new DeliveryRequest(type, distance, weight, order);
    }

    private static DeliveryType parseType(String value) {
        for (DeliveryType t : DeliveryType.values()) {
            if (t.name().equals(value)) {
                return t;
            }
        }
        throw new IllegalArgumentException("unknown type \"" + value + "\"");
    }

    private static double parseNumber(String value, String field) {
        if (!value.matches("-?[0-9]+(\\.[0-9]+)?")) {
            throw new IllegalArgumentException(field + " is not a number: \"" + value + "\"");
        }
        return Double.parseDouble(value);
    }

    // ---------- Режим 3: интерактивный ввод ----------

    private static void runInteractive() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Введите запрос: type distanceKm weightKg orderAmount (через пробел или ;)");
        System.out.println("Типы: STANDARD, EXPRESS, PICKUP, POSTAL. Выход — exit или пустая строка");
        while (true) {
            System.out.print("> ");
            if (!sc.hasNextLine()) {
                break;
            }
            String line = sc.nextLine().trim();
            if (line.isEmpty() || line.equals("exit")) {
                break;
            }
            try {
                DeliveryRequest request = parseRequest(line.split("[;\\s]+"));
                DeliveryPolicy policy = FACTORY.get(request.type());
                System.out.println("Политика: " + name(policy));
                System.out.println("Стоимость: " + SERVICE.calculate(request));
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    // ---------- Помощники проверок ----------

    private static void expect(String name, boolean ok, String actual) {
        report(name, ok, "получено " + actual);
    }

    private static void expectCost(String name, double expected, double actual) {
        report(name, Math.abs(actual - expected) < EPS, "ожидалось " + expected + ", получено " + actual);
    }

    private static void unexpected(String name, RuntimeException e) {
        report(name, false, "неожиданное исключение " + e);
    }

    private static void noError(String name) {
        report(name, false, "ожидалось IllegalArgumentException");
    }

    private static void checkMessage(String name, IllegalArgumentException e, String expectedPart) {
        boolean ok = e.getMessage() != null && e.getMessage().contains(expectedPart);
        report(name, ok, "ожидалось сообщение с \"" + expectedPart + "\", получено \"" + e.getMessage() + "\"");
    }

    private static void report(String name, boolean ok, String reason) {
        if (ok) {
            passed++;
            System.out.println("PASS " + name);
        } else {
            failed++;
            System.out.println("FAIL " + name + ": " + reason);
        }
    }
}
