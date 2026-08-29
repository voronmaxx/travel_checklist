
```python
#!/usr/bin/env python3
# travel_checklist.py
import argparse
import json
import csv
import os
import sys
from datetime import datetime
from colorama import init, Fore, Style

init(autoreset=True)

# Шаблоны чек-листов для разных типов путешествий
TEMPLATES = {
    "beach": {
        "name": "Пляжный отдых",
        "categories": {
            "Документы": ["Паспорт", "Билеты", "Страховка", "Виза"],
            "Одежда": ["Купальник", "Шорты", "Футболка", "Панама", "Сандалии", "Пляжное полотенце"],
            "Гигиена": ["Солнцезащитный крем", "Очки", "Шампунь", "Зубная щетка", "Гель для душа"],
            "Техника": ["Телефон", "Зарядка", "Наушники", "Повербанк"],
            "Медицина": ["Аптечка", "Средство от укусов", "Пластырь"]
        }
    },
    "mountain": {
        "name": "Горный поход",
        "categories": {
            "Документы": ["Паспорт", "Страховка", "Карта", "Компас"],
            "Одежда": ["Термобелье", "Флис", "Куртка", "Штаны", "Трекинговая обувь", "Носки", "Шапка", "Перчатки"],
            "Снаряжение": ["Рюкзак", "Палатка", "Спальник", "Каремат", "Фонарик", "Трекинговые палки"],
            "Еда": ["Горелка", "Топливо", "Посуда", "Еда в пакетах", "Вода", "Термос"],
            "Техника": ["Телефон", "Зарядка", "Навигатор", "Радиостанция"]
        }
    },
    "business": {
        "name": "Командировка",
        "categories": {
            "Документы": ["Паспорт", "Билеты", "Командировочное удостоверение", "Виза", "Страховка"],
            "Одежда": ["Костюм", "Рубашки", "Галстук", "Туфли", "Носки", "Ремень"],
            "Техника": ["Ноутбук", "Зарядка", "Презентация", "Флешка", "Телефон", "Адаптер"],
            "Гигиена": ["Дезодорант", "Зубная щетка", "Паста", "Расческа", "Шампунь"],
            "Прочее": ["Визитки", "Блокнот", "Ручка", "Зонт"]
        }
    },
    "hiking": {
        "name": "Пеший поход",
        "categories": {
            "Документы": ["Паспорт", "Карта", "Разрешение", "Страховка"],
            "Снаряжение": ["Рюкзак", "Палатка", "Спальник", "Каремат", "Котелок", "Нож"],
            "Одежда": ["Треккинговая обувь", "Носки", "Флис", "Куртка", "Штаны", "Дождевик", "Головной убор"],
            "Еда": ["Еда в пакетах", "Сухой паек", "Вода", "Термос", "Горелка"],
            "Медицина": ["Аптечка", "Эластичный бинт", "Пластырь", "Средство от насекомых"]
        }
    },
    "cruise": {
        "name": "Круиз",
        "categories": {
            "Документы": ["Паспорт", "Виза", "Билеты", "Страховка", "Медицинская справка"],
            "Одежда": ["Праздничный наряд", "Пляжная одежда", "Купальник", "Сандалии", "Туфли", "Пижама"],
            "Гигиена": ["Солнцезащитный крем", "Шампунь", "Зубная щетка", "Дезодорант", "Москитная сетка"],
            "Техника": ["Телефон", "Зарядка", "Фотоаппарат", "Наушники"],
            "Прочее": ["Книга", "Ласты", "Маска для плавания", "Бинокль"]
        }
    },
    "festival": {
        "name": "Фестиваль",
        "categories": {
            "Документы": ["Паспорт", "Билет на фестиваль", "Страховка"],
            "Одежда": ["Яркая одежда", "Дождевик", "Удобная обувь", "Головной убор", "Солнцезащитные очки"],
            "Еда": ["Вода", "Еда в пакетах", "Термос", "Пикник"],
            "Техника": ["Телефон", "Зарядка", "Наушники", "Повербанк"],
            "Прочее": ["Плед", "Стул", "Флаг", "Настольные игры"]
        }
    }
}

class Item:
    def __init__(self, id, text, category, packed=False):
        self.id = id
        self.text = text
        self.category = category
        self.packed = packed

    def to_dict(self):
        return {"id": self.id, "text": self.text, "category": self.category, "packed": self.packed}

    @classmethod
    def from_dict(cls, data):
        return cls(data["id"], data["text"], data["category"], data.get("packed", False))

class TravelChecklist:
    def __init__(self, name="Путешествие", template=None):
        self.name = name
        self.items = []
        self.next_id = 1
        if template and template in TEMPLATES:
            self.load_template(template)

    def load_template(self, template_name):
        template = TEMPLATES[template_name]
        self.name = template["name"]
        self.items = []
        self.next_id = 1
        for category, items in template["categories"].items():
            for text in items:
                self.items.append(Item(self.next_id, text, category))
                self.next_id += 1

    def to_dict(self):
        return {
            "name": self.name,
            "items": [i.to_dict() for i in self.items],
            "next_id": self.next_id
        }

    @classmethod
    def from_dict(cls, data):
        checklist = cls(data["name"])
        checklist.items = [Item.from_dict(i) for i in data.get("items", [])]
        checklist.next_id = data.get("next_id", max([i.id for i in checklist.items] + [0]) + 1)
        return checklist

    def add_item(self, text, category="Прочее"):
        item = Item(self.next_id, text, category)
        self.items.append(item)
        self.next_id += 1
        print(Fore.GREEN + f"Пункт добавлен (ID: {item.id})")
        return item.id

    def remove_item(self, item_id):
        for i, item in enumerate(self.items):
            if item.id == item_id:
                del self.items[i]
                print(Fore.YELLOW + f"Пункт #{item_id} удалён.")
                return
        print(Fore.RED + f"Пункт #{item_id} не найден.")

    def pack_item(self, item_id):
        for item in self.items:
            if item.id == item_id:
                item.packed = True
                print(Fore.GREEN + f"Пункт #{item_id} отмечен как собранный.")
                return
        print(Fore.RED + f"Пункт #{item_id} не найден.")

    def unpack_item(self, item_id):
        for item in self.items:
            if item.id == item_id:
                item.packed = False
                print(Fore.YELLOW + f"Отметка с пункта #{item_id} снята.")
                return
        print(Fore.RED + f"Пункт #{item_id} не найден.")

    def get_progress(self):
        if not self.items:
            return 0, 0
        total = len(self.items)
        packed = sum(1 for i in self.items if i.packed)
        return packed, total

    def display(self):
        if not self.items:
            print(Fore.YELLOW + "Чек-лист пуст.")
            return
        packed, total = self.get_progress()
        pct = int(packed / total * 100) if total > 0 else 0
        bar_len = 20
        filled = int(bar_len * pct / 100)
        bar = "█" * filled + "░" * (bar_len - filled)
        print(Fore.CYAN + f"📋 Чек-лист: {self.name}")
        print(Fore.GREEN + f"Прогресс: {bar} {pct}% ({packed}/{total})")
        print()
        categories = sorted(set(i.category for i in self.items))
        for cat in categories:
            cat_items = [i for i in self.items if i.category == cat]
            print(Fore.YELLOW + f"Категория: {cat}")
            for item in cat_items:
                status = Fore.GREEN + "✅" if item.packed else Fore.RED + "❌"
                print(f"  {item.id}. {status} {item.text}")

    def save(self, filename="checklist.json"):
        with open(filename, 'w') as f:
            json.dump(self.to_dict(), f, indent=2)
        print(Fore.GREEN + f"Чек-лист сохранён в {filename}")

    @classmethod
    def load(cls, filename="checklist.json"):
        try:
            with open(filename, 'r') as f:
                data = json.load(f)
                return cls.from_dict(data)
        except Exception as e:
            print(Fore.RED + f"Ошибка загрузки: {e}")
            return None

    def export_csv(self, filename):
        with open(filename, 'w', newline='') as f:
            writer = csv.writer(f)
            writer.writerow(["id", "category", "item", "packed"])
            for item in self.items:
                writer.writerow([item.id, item.category, item.text, item.packed])
        print(Fore.GREEN + f"Экспортировано в {filename} (CSV)")

    def export_txt(self, filename):
        with open(filename, 'w') as f:
            f.write(f"Чек-лист: {self.name}\n")
            f.write(f"Дата: {datetime.now().strftime('%Y-%m-%d %H:%M')}\n\n")
            categories = sorted(set(i.category for i in self.items))
            for cat in categories:
                f.write(f"=== {cat} ===\n")
                for item in [i for i in self.items if i.category == cat]:
                    f.write(f"{'[x]' if item.packed else '[ ]'} {item.text}\n")
                f.write("\n")
        print(Fore.GREEN + f"Экспортировано в {filename} (TXT)")

def main():
    parser = argparse.ArgumentParser(description="Генератор чек-листов для путешествий")
    parser.add_argument("--template", choices=list(TEMPLATES.keys()), help="Тип путешествия")
    parser.add_argument("--name", help="Название чек-листа")
    parser.add_argument("--add", help="Добавить пункт")
    parser.add_argument("--add-category", help="Категория для добавляемого пункта")
    parser.add_argument("--pack", type=int, help="Отметить пункт как собранный")
    parser.add_argument("--unpack", type=int, help="Снять отметку с пункта")
    parser.add_argument("--remove", type=int, help="Удалить пункт")
    parser.add_argument("--list", action="store_true", help="Показать чек-лист")
    parser.add_argument("--save", help="Сохранить чек-лист (файл)")
    parser.add_argument("--load", help="Загрузить чек-лист из файла")
    parser.add_argument("--new", action="store_true", help="Создать новый пустой чек-лист")
    parser.add_argument("--export-csv", help="Экспорт в CSV")
    parser.add_argument("--export-txt", help="Экспорт в TXT")
    args = parser.parse_args()

    checklist = None

    # Загрузка или создание чек-листа
    if args.load:
        checklist = TravelChecklist.load(args.load)
        if not checklist:
            sys.exit(1)
    elif args.new:
        checklist = TravelChecklist(args.name or "Новый список")
    elif args.template:
        checklist = TravelChecklist(args.name, args.template)
    else:
        # Попытка загрузить из файла по умолчанию
        if os.path.exists("checklist.json"):
            checklist = TravelChecklist.load("checklist.json")
        if not checklist:
            checklist = TravelChecklist(args.name or "Мой список")

    if args.add:
        category = args.add_category or "Прочее"
        checklist.add_item(args.add, category)

    if args.pack:
        checklist.pack_item(args.pack)

    if args.unpack:
        checklist.unpack_item(args.unpack)

    if args.remove:
        checklist.remove_item(args.remove)

    if args.list:
        checklist.display()

    if args.save:
        checklist.save(args.save)
    elif args.load is None and not args.new and not args.template and not any([args.add, args.pack, args.unpack, args.remove]):
        # Автосохранение при изменениях
        if any([args.add, args.pack, args.unpack, args.remove]):
            checklist.save("checklist.json")

    if args.export_csv:
        checklist.export_csv(args.export_csv)

    if args.export_txt:
        checklist.export_txt(args.export_txt)

    if not any(vars(args).values()):
        parser.print_help()

if __name__ == "__main__":
    main()
