# Практическая работа №10: интерфейсы, полиморфизм и замена условной логики

Преснухин Илья, ЭФБО-12-25, вариант 16.

Расчёт стоимости доставки без `if-else` по типам: каждый способ доставки — реализация интерфейса `DeliveryPolicy` (паттерн Strategy), `DeliveryPolicyFactory` выбирает реализацию по `DeliveryType` (Factory), `DeliveryService` вызывает `supports` и `calculateCost` полиморфно. Правила R1–R9 и решения по варианту — в [DECISIONS.md](DECISIONS.md), отчёт — [REPORT.docx](REPORT.docx).

Пакет `ru.edu.pr07` сохранён по требованию задания (исторический номер).

| Тип | Ограничение | Стоимость |
|---|---|---|
| STANDARD | вес ≤ 20 кг | max(500, км × 50) |
| EXPRESS | вес ≤ 10 кг | max(1000, км × 100) + 1% суммы заказа |
| PICKUP | нет | 0 |
| POSTAL (вариант 16) | вес ≤ 2 кг | max(300, км × 20) |

## Вариант 16

- Часть А: новый тип `POSTAL` в `DeliveryType`, класс `PostalPolicy`, ветка `case POSTAL` в фабрике. Остальные политики и `DeliveryService.calculate` не менялись (OCP, R9).
- Часть Б: `DeliveryService.addPostalCost(DeliveryService, DeliveryRequest)` → `double` — стоимость почтовой доставки для тех же расстояния, веса и суммы через `service.calculate`.

## Структура

```
src/ru/edu/pr07/
  DeliveryType.java            enum типов (+ POSTAL)
  DeliveryRequest.java         record запроса
  DeliveryPolicy.java          интерфейс-контракт
  StandardCourierPolicy.java   стандартный курьер
  ExpressPolicy.java           экспресс
  PickupPolicy.java            самовывоз
  PostalPolicy.java            почта (вариант 16)
  DeliveryPolicyFactory.java   выбор политики по типу
  DeliveryService.java         calculate + addPostalCost (вариант 16)
  SelfCheck.java               unit-проверки, data-driven по sample.csv, интерактивный режим
data/sample.csv                type;distanceKm;weightKg;orderAmount;expected
diagrams/                      activity.dot/.png, class.dot/.png
```

## Сборка и запуск (Java 21+, без Maven)

Windows:
```
run_selfcheck.bat       проверки
run_interactive.bat     проверки, затем ввод запросов с консоли
```

Вручную:
```bash
mkdir -p out
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out ru.edu.pr07.SelfCheck                 # проверки
java -cp out ru.edu.pr07.SelfCheck --interactive   # + интерактивный режим (можно -i)
```

Интерактивный режим: `type distanceKm weightKg orderAmount` через пробел или `;`, например `EXPRESS 5 1.2 4000`; выход — `exit` или пустая строка.

`expected` в sample.csv: `OK:cost=<double>` (сравнение с допуском 0.001) или `SKIP:<причина>`.
