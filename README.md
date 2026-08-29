# Генератор чек-листов (путешествия)

Многоязычное консольное приложение для создания и управления чек-листами для путешествий.  
Позволяет создавать списки вещей на основе шаблонов (пляж, горы, командировка, поход), добавлять/удалять пункты, отмечать собранное и экспортировать данные.

## Особенности
- Готовые шаблоны для различных типов путешествий: пляж, горы, командировка, поход, круиз, фестиваль.
- Добавление, удаление и редактирование пунктов чек-листа.
- Отметка пунктов как собранных (✓).
- Категоризация предметов (одежда, обувь, документы, техника, гигиена, медицина, еда, прочее).
- Прогресс-бар с процентом готовности.
- Сохранение и загрузка чек-листов в JSON.
- Экспорт в CSV, TXT и цветной вывод в терминале.
- Поддержка аргументов командной строки для всех операций.

## Установка и запуск
Для каждого языка требуются соответствующие инструменты и зависимости (указаны ниже).

### Запуск на разных языках

1. **Python**  
   Установка: `pip install colorama` (опционально).  
   Запуск: `python travel_checklist.py --template beach --name "Майорка 2026"`

2. **JavaScript (Node.js)**  
   Установка: `npm install commander chalk`  
   Запуск: `node travel_checklist.js --template beach --name "Майорка 2026"`

3. **Go**  
   Запуск: `go run travel_checklist.go --template beach --name "Майорка 2026"`

4. **Rust**  
   Сборка: `cargo build --release`  
   Запуск: `cargo run -- --template beach --name "Майорка 2026"`

5. **Java**  
   Сборка: `javac -cp gson.jar TravelChecklist.java`  
   Запуск: `java -cp .;gson.jar TravelChecklist --template beach --name "Майорка 2026"`

6. **C# (.NET Core)**  
   Установка: `dotnet add package Newtonsoft.Json`  
   Запуск: `dotnet run -- --template beach --name "Майорка 2026"`

7. **C++ (Linux)**  
   Сборка: `g++ -std=c++11 -o travel_checklist travel_checklist.cpp -ljsoncpp`  
   Запуск: `./travel_checklist --template beach --name "Майорка 2026"`

8. **Kotlin (JVM)**  
   Сборка: `kotlinc -cp gson.jar TravelChecklist.kt`  
   Запуск: `kotlin -cp .;gson.jar TravelChecklistKt --template beach --name "Майорка 2026"`

## Использование

Общие аргументы командной строки (везде, где поддерживается):

- `--template <тип>` – тип путешествия: `beach`, `mountain`, `business`, `hiking`, `cruise`, `festival`.
- `--name <название>` – название чек-листа (по умолчанию "Путешествие").
- `--add <текст>` – добавить пункт в текущий чек-лист.
- `--add-category <категория>` – добавить пункт в категорию.
- `--pack <ID>` – отметить пункт как собранный (по ID).
- `--unpack <ID>` – снять отметку с пункта.
- `--remove <ID>` – удалить пункт по ID.
- `--list` – показать текущий чек-лист.
- `--save` – сохранить текущий чек-лист (по умолчанию `checklist.json`).
- `--load <файл>` – загрузить чек-лист из файла.
- `--export-csv <файл>` – экспортировать в CSV.
- `--export-txt <файл>` – экспортировать в TXT.
- `--new` – создать новый чек-лист (очистить текущий).
- `--help` – справка.

Пример (Python):
```bash
python travel_checklist.py --template beach --name "Отдых в Сочи"
python travel_checklist.py --add-category "Документы" --add "Паспорт"
python travel_checklist.py --add "Крем от загара" --add-category "Гигиена"
python travel_checklist.py --pack 1
python travel_checklist.py --list
python travel_checklist.py --save my_list.json
python travel_checklist.py --export-csv checklist.csv
Пример вывода (--list):

text
📋 Чек-лист: Отдых в Сочи (пляж)
Прогресс: ████████░░░░░░░░ 60% (6/10)

Категория: Документы
  1. ✅ Паспорт
  2. ❌ Билеты

Категория: Одежда
  3. ✅ Купальник
  4. ❌ Шорты
  5. ✅ Футболка
Структура репозитория
text
/
├── README.md
├── travel_checklist.py
├── travel_checklist.js
├── travel_checklist.go
├── travel_checklist.rs
├── TravelChecklist.java
├── TravelChecklist.cs
├── travel_checklist.cpp
└── TravelChecklist.kt
Лицензия
MIT
