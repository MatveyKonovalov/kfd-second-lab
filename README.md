# Ускорь индексацию

Подборка доступных товаров по рекомендациям с учётом исключений и повторений.

## Начало работы

[Полное задание](docs/TASK.md). Работа выполняется в собственной копии репозитория.

| Файл | Назначение |
| --- | --- |
| `src/main/kotlin/lab2/Product.kt` | Тип данных |
| `src/main/kotlin/lab2/ProductSelection.kt` | Функция `selectProducts` (основное задание) |
| `src/main/kotlin/lab2/ProductSelectionService.kt` | Сервис с предварительной индексацией каталога (необязательное продолжение) |
| `src/test/kotlin/lab2/ProductSelectionTest.kt` | Тесты основной функции |
| `src/test/kotlin/lab2/ProductSelectionServiceTest.kt` | Тесты сервиса |
| `README.md` | Инструкция запуска и ответы по работе |

Запуск всех тестов из корня репозитория:

```bash
./gradlew test
```

Windows PowerShell:

```powershell
.\gradlew test
```

## Основное задание

### Решение

**Как устроена функция?**

1. Исключения преобразуются в `HashSet` (`excludedIdsSet`) для проверки за O(1).
2. Из каталога оставляются только доступные товары (`available == true`), которых нет в исключениях. По ним строится `HashMap<id, Product>` (`productMap`).
3. Проходим по `recommendedIds` в порядке приоритета. Для каждого id выполняем `remove` из карты: если товар найден — добавляем его в результат. `remove` гарантирует, что один и тот же товар попадёт в результат только один раз.
4. Цикл можно прервать досрочно, если карта уже пуста (больше нечего добавлять).

Входные списки не модифицируются. Возвращаются оригинальные объекты `Product`.

**Почему выбран именно этот способ? Как обрабатываются исключённые, недоступные и неизвестные товары?**

- Фильтрация `available` и исключений выполняется один раз при построении карты — O(p).
- Неизвестные id и уже обработанные товары просто пропускаются (`remove` возвращает `null`).
- Порядок результата полностью определяется первым появлением id в рекомендациях; порядок каталога не влияет.
- Использование `HashMap` + `remove` даёт корректную дедупликацию и быстрый доступ без дополнительных структур для уже добавленных id.

### Сложность

Обозначения:

- `p` — число товаров в каталоге;
- `r` — число идентификаторов в рекомендациях;
- `e` — число идентификаторов в исключениях.

**Время выполнения:**  
O(p + e + r)

- построение множества исключений — O(e);
- фильтрация каталога и построение карты — O(p);
- один проход по рекомендациям с операциями `remove`/`contains` за O(1) амортизированно — O(r).

Оценка справедлива при использовании хеш-таблиц (ожидаемое время O(1) на операцию).

**Дополнительная память:**  
O(p + e) + O(s), где `s` — размер результата (s ≤ min(p, r)).

- `excludedIdsSet` — O(e);
- `productMap` — O(p) в худшем случае (все товары доступны и не исключены);
- `result` — O(s) (память результата указывается отдельно, как требуется).

### Тесты

| Что проверяется | Тесты |
| --- | --- |
| Обычная подборка; порядок рекомендаций отличается от порядка каталога | `selectsAvailableProductsInRecommendationOrder`, `check simple combination && maintaining order`, `check maintaining order` |
| Повторяющиеся рекомендации и исключения | `duplicate recommendations do not duplicate products`, `duplicate excluded ids do not change result` |
| Недоступные, исключённые и неизвестные товары | `select zero products when available = false`, `unknown ids in recommendations and exclusions are ignored` |
| Пустой каталог, пустые рекомендации, пустые исключения | `returnsEmptyListWhenThereAreNoRecommendations`, `empty catalog yields empty result`, `empty exclusions and recommendations are handled` |
| Ни один товар не подходит | `no products match when all are excluded`, `no products match when recommendations only contain unknown ids` |
| Входные данные не изменяются; повторные вызовы независимы | `input collections are not modified`, `repeated calls return identical results and do not share state`, `mutating result does not affect subsequent calls` |

## Необязательное продолжение

**Статус:** Выполнено

### Организация решения

**Файлы с реализацией и пример вызова:**

- `src/main/kotlin/lab2/ProductSelectionService.kt`
- Основная функция `selectProducts` остаётся доступной и продолжает проходить свои тесты.

Пример использования:

```kotlin
val service = ProductSelectionService(catalog)          // каталог индексируется один раз

val result1 = service.selectProducts(
    recommendedIds = listOf(3L, 2L, 3L, 99L, 1L, 4L),
    excludedIds = listOf(1L),
    limit = 1
) // [Monitor]

val result2 = service.selectProducts(
    recommendedIds = listOf(3L, 2L, 3L, 99L, 1L, 4L),
    excludedIds = listOf(1L),
    limit = 10
) // [Monitor, Webcam]
```

**Что выполняется при загрузке каталога, а что — при каждом запросе? Как обеспечиваются независимость запросов и переход на новый каталог?**

- При создании сервиса (`ProductSelectionService(products)`):
    - один раз фильтруются доступные товары;
    - строится неизменяемая `HashMap<id, Product>` (`hashTableProducts`).
- При каждом запросе:
    - строится локальное множество исключений;
    - создаются локальные `result` и `addedElements`;
    - проход по рекомендациям с проверкой `limit`.
- Данные одного запроса (исключения, `addedElements`) живут только внутри вызова метода — запросы полностью независимы.
- Переход на новый каталог выполняется созданием нового экземпляра сервиса.

**Когда прекращается просмотр рекомендаций? От чего зависит число просмотренных элементов?**

Просмотр останавливается, как только в результат добавлено `limit` товаров (`if (sizeAdded >= limit) break`).  
Число просмотренных рекомендаций зависит от:
- значения `limit`;
- плотности подходящих (доступных и неисключённых) id в начале списка рекомендаций;
- количества дубликатов и «мусорных» id.

### Сложность

| Этап | Время | Дополнительная память |
| --- | --- | --- |
| Подготовка каталога | O(p) | O(p) (карта доступных товаров) |
| Один запрос | O(e + min(r, t)) | O(e + s) |

где:
- `t` — позиция, на которой набирается `limit` подходящих товаров (или `r`, если подходящих меньше);
- `s` — фактический размер результата (s ≤ limit).

**Обоснование оценок и память для результата:**

- Подготовка: один проход по каталогу + построение хеш-таблицы.
- Запрос: построение множества исключений O(e) + проход по рекомендациям до набора `limit` элементов (каждая операция O(1)).
- Память результата O(s) учитывается отдельно; локальные структуры (`excludedIdsSet`, `addedElements`) живут только во время запроса.

### Проверка продолжения

| Что проверяется | Тесты |
| --- | --- |
| Нулевой лимит | `returns empty list when limit is zero` |
| Досрочное заполнение результата | `early exit stops processing once limit is reached`, `returns only first limit items` |
| Подходящих товаров меньше лимита | `returns all matching products if they are fewer than limit` |
| Несколько независимых запросов к одному каталогу | `multiple requests to the same catalog do not affect each other`, `exclusions from one request do not leak into another` |
| Переход на новый каталог | создание нового экземпляра `ProductSelectionService` с другим каталогом (логика независимости покрыта тестами `empty catalog returns empty result` и `multiple requests...`) |

**Как подтверждено досрочное завершение?**

В тесте `early exit stops processing once limit is reached` при `limit = 1` и рекомендациях `[3, 4, 5]` результат содержит только `Monitor`.  
Логика функции прерывает цикл сразу после добавления первого подходящего товара (`if (sizeAdded >= limit) break`), поэтому оставшиеся рекомендации (`4` и `5`) не обрабатываются. Число просмотренных элементов в этом случае равно 1 (позиция первого подходящего id).