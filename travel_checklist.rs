// travel_checklist.rs
use clap::{App, Arg};
use colored::*;
use serde::{Deserialize, Serialize};
use serde_json;
use std::collections::{HashMap, HashSet};
use std::fs;
use std::io::Write;
use std::time::SystemTime;

const DATA_FILE: &str = "checklist.json";

#[derive(Serialize, Deserialize)]
struct Item {
    id: u32,
    text: String,
    category: String,
    packed: bool,
}

#[derive(Serialize, Deserialize)]
struct Checklist {
    name: String,
    items: Vec<Item>,
    next_id: u32,
}

impl Checklist {
    fn new(name: &str) -> Self {
        Checklist {
            name: name.to_string(),
            items: Vec::new(),
            next_id: 1,
        }
    }

    fn load_template(&mut self, template_name: &str) {
        let templates = get_templates();
        let tmpl = match templates.get(template_name) {
            Some(t) => t,
            None => return,
        };
        self.name = tmpl.name.clone();
        self.items = Vec::new();
        self.next_id = 1;
        for (category, items) in &tmpl.categories {
            for text in items {
                self.items.push(Item {
                    id: self.next_id,
                    text: text.clone(),
                    category: category.clone(),
                    packed: false,
                });
                self.next_id += 1;
            }
        }
    }

    fn add_item(&mut self, text: &str, category: &str) {
        let cat = if category.is_empty() { "Прочее" } else { category };
        let item = Item {
            id: self.next_id,
            text: text.to_string(),
            category: cat.to_string(),
            packed: false,
        };
        self.items.push(item);
        self.next_id += 1;
        println!("{}", format!("Пункт добавлен (ID: {})", self.next_id - 1).green());
    }

    fn remove_item(&mut self, id: u32) {
        if let Some(pos) = self.items.iter().position(|i| i.id == id) {
            self.items.remove(pos);
            println!("{}", format!("Пункт #{} удалён.", id).yellow());
        } else {
            println!("{}", format!("Пункт #{} не найден.", id).red());
        }
    }

    fn pack_item(&mut self, id: u32) {
        if let Some(item) = self.items.iter_mut().find(|i| i.id == id) {
            item.packed = true;
            println!("{}", format!("Пункт #{} отмечен как собранный.", id).green());
        } else {
            println!("{}", format!("Пункт #{} не найден.", id).red());
        }
    }

    fn unpack_item(&mut self, id: u32) {
        if let Some(item) = self.items.iter_mut().find(|i| i.id == id) {
            item.packed = false;
            println!("{}", format!("Отметка с пункта #{} снята.", id).yellow());
        } else {
            println!("{}", format!("Пункт #{} не найден.", id).red());
        }
    }

    fn display(&self) {
        if self.items.is_empty() {
            println!("{}", "Чек-лист пуст.".yellow());
            return;
        }
        let total = self.items.len();
        let packed = self.items.iter().filter(|i| i.packed).count();
        let pct = if total > 0 { packed * 100 / total } else { 0 };
        let bar_len = 20;
        let filled = pct * bar_len / 100;
        let bar = "█".repeat(filled) + "░".repeat(bar_len - filled);
        println!("{}", format!("📋 Чек-лист: {}", self.name).cyan());
        println!("{}", format!("Прогресс: {} {}% ({}/{})", bar, pct, packed, total).green());
        println!();

        let mut categories: Vec<String> = self.items.iter().map(|i| i.category.clone()).collect();
        categories.sort();
        categories.dedup();

        for cat in categories {
            println!("{}", format!("Категория: {}", cat).yellow());
            for item in self.items.iter().filter(|i| i.category == cat) {
                let status = if item.packed { "✅".green() } else { "❌".red() };
                println!("  {}. {} {}", item.id, status, item.text);
            }
        }
    }

    fn save(&self, filename: &str) {
        let json = serde_json::to_string_pretty(&self).unwrap();
        fs::write(filename, json).unwrap();
        println!("{}", format!("Чек-лист сохранён в {}", filename).green());
    }

    fn load(filename: &str) -> Option<Self> {
        if let Ok(data) = fs::read_to_string(filename) {
            if let Ok(checklist) = serde_json::from_str(&data) {
                return Some(checklist);
            }
        }
        println!("{}", "Ошибка загрузки.".red());
        None
    }

    fn export_csv(&self, filename: &str) {
        let mut wtr = csv::Writer::from_path(filename).unwrap();
        wtr.write_record(&["id", "category", "item", "packed"]).unwrap();
        for item in &self.items {
            wtr.write_record(&[
                &item.id.to_string(),
                &item.category,
                &item.text,
                &item.packed.to_string(),
            ]).unwrap();
        }
        wtr.flush().unwrap();
        println!("{}", format!("Экспортировано в {} (CSV)", filename).green());
    }

    fn export_txt(&self, filename: &str) {
        let mut content = format!("Чек-лист: {}\n", self.name);
        content.push_str(&format!("Дата: {:?}\n\n", SystemTime::now()));
        let mut categories: Vec<String> = self.items.iter().map(|i| i.category.clone()).collect();
        categories.sort();
        categories.dedup();
        for cat in categories {
            content.push_str(&format!("=== {} ===\n", cat));
            for item in self.items.iter().filter(|i| i.category == cat) {
                let status = if item.packed { "[x]" } else { "[ ]" };
                content.push_str(&format!("{} {}\n", status, item.text));
            }
            content.push('\n');
        }
        fs::write(filename, content).unwrap();
        println!("{}", format!("Экспортировано в {} (TXT)", filename).green());
    }
}

struct Template {
    name: String,
    categories: HashMap<String, Vec<String>>,
}

fn get_templates() -> HashMap<String, Template> {
    let mut templates = HashMap::new();
    
    let mut beach_cats = HashMap::new();
    beach_cats.insert("Документы".to_string(), vec!["Паспорт".to_string(), "Билеты".to_string(), "Страховка".to_string(), "Виза".to_string()]);
    beach_cats.insert("Одежда".to_string(), vec!["Купальник".to_string(), "Шорты".to_string(), "Футболка".to_string(), "Панама".to_string(), "Сандалии".to_string(), "Пляжное полотенце".to_string()]);
    beach_cats.insert("Гигиена".to_string(), vec!["Солнцезащитный крем".to_string(), "Очки".to_string(), "Шампунь".to_string(), "Зубная щетка".to_string(), "Гель для душа".to_string()]);
    beach_cats.insert("Техника".to_string(), vec!["Телефон".to_string(), "Зарядка".to_string(), "Наушники".to_string(), "Повербанк".to_string()]);
    beach_cats.insert("Медицина".to_string(), vec!["Аптечка".to_string(), "Средство от укусов".to_string(), "Пластырь".to_string()]);
    templates.insert("beach".to_string(), Template { name: "Пляжный отдых".to_string(), categories: beach_cats });

    let mut mountain_cats = HashMap::new();
    mountain_cats.insert("Документы".to_string(), vec!["Паспорт".to_string(), "Страховка".to_string(), "Карта".to_string(), "Компас".to_string()]);
    mountain_cats.insert("Одежда".to_string(), vec!["Термобелье".to_string(), "Флис".to_string(), "Куртка".to_string(), "Штаны".to_string(), "Трекинговая обувь".to_string(), "Носки".to_string(), "Шапка".to_string(), "Перчатки".to_string()]);
    mountain_cats.insert("Снаряжение".to_string(), vec!["Рюкзак".to_string(), "Палатка".to_string(), "Спальник".to_string(), "Каремат".to_string(), "Фонарик".to_string(), "Трекинговые палки".to_string()]);
    mountain_cats.insert("Еда".to_string(), vec!["Горелка".to_string(), "Топливо".to_string(), "Посуда".to_string(), "Еда в пакетах".to_string(), "Вода".to_string(), "Термос".to_string()]);
    mountain_cats.insert("Техника".to_string(), vec!["Телефон".to_string(), "Зарядка".to_string(), "Навигатор".to_string(), "Радиостанция".to_string()]);
    templates.insert("mountain".to_string(), Template { name: "Горный поход".to_string(), categories: mountain_cats });

    // ... (остальные шаблоны аналогично, сокращённо для краткости)
    let mut business_cats = HashMap::new();
    business_cats.insert("Документы".to_string(), vec!["Паспорт".to_string(), "Билеты".to_string(), "Командировочное удостоверение".to_string(), "Виза".to_string(), "Страховка".to_string()]);
    business_cats.insert("Одежда".to_string(), vec!["Костюм".to_string(), "Рубашки".to_string(), "Галстук".to_string(), "Туфли".to_string(), "Носки".to_string(), "Ремень".to_string()]);
    business_cats.insert("Техника".to_string(), vec!["Ноутбук".to_string(), "Зарядка".to_string(), "Презентация".to_string(), "Флешка".to_string(), "Телефон".to_string(), "Адаптер".to_string()]);
    business_cats.insert("Гигиена".to_string(), vec!["Дезодорант".to_string(), "Зубная щетка".to_string(), "Паста".to_string(), "Расческа".to_string(), "Шампунь".to_string()]);
    business_cats.insert("Прочее".to_string(), vec!["Визитки".to_string(), "Блокнот".to_string(), "Ручка".to_string(), "Зонт".to_string()]);
    templates.insert("business".to_string(), Template { name: "Командировка".to_string(), categories: business_cats });

    templates
}

fn main() {
    let matches = App::new("Travel Checklist")
        .arg(Arg::with_name("template").long("template").takes_value(true).help("Тип путешествия"))
        .arg(Arg::with_name("name").long("name").takes_value(true).help("Название чек-листа"))
        .arg(Arg::with_name("add").long("add").takes_value(true).help("Добавить пункт"))
        .arg(Arg::with_name("add-category").long("add-category").takes_value(true).help("Категория для добавляемого пункта"))
        .arg(Arg::with_name("pack").long("pack").takes_value(true).help("Отметить пункт как собранный"))
        .arg(Arg::with_name("unpack").long("unpack").takes_value(true).help("Снять отметку с пункта"))
        .arg(Arg::with_name("remove").long("remove").takes_value(true).help("Удалить пункт"))
        .arg(Arg::with_name("list").long("list").help("Показать чек-лист"))
        .arg(Arg::with_name("save").long("save").takes_value(true).help("Сохранить чек-лист"))
        .arg(Arg::with_name("load").long("load").takes_value(true).help("Загрузить чек-лист"))
        .arg(Arg::with_name("new").long("new").help("Создать новый пустой чек-лист"))
        .arg(Arg::with_name("export-csv").long("export-csv").takes_value(true).help("Экспорт в CSV"))
        .arg(Arg::with_name("export-txt").long("export-txt").takes_value(true).help("Экспорт в TXT"))
        .get_matches();

    let mut checklist = if let Some(file) = matches.value_of("load") {
        if let Some(c) = Checklist::load(file) { c } else { std::process::exit(1); }
    } else if matches.is_present("new") {
        Checklist::new(matches.value_of("name").unwrap_or("Новый список"))
    } else if let Some(tmpl) = matches.value_of("template") {
        let mut c = Checklist::new(matches.value_of("name").unwrap_or("Путешествие"));
        c.load_template(tmpl);
        c
    } else {
        if let Some(c) = Checklist::load(DATA_FILE) { c } else { Checklist::new("Мой список") }
    };

    if let Some(text) = matches.value_of("add") {
        let category = matches.value_of("add-category").unwrap_or("Прочее");
        checklist.add_item(text, category);
    }
    if let Some(id_str) = matches.value_of("pack") {
        checklist.pack_item(id_str.parse().unwrap());
    }
    if let Some(id_str) = matches.value_of("unpack") {
        checklist.unpack_item(id_str.parse().unwrap());
    }
    if let Some(id_str) = matches.value_of("remove") {
        checklist.remove_item(id_str.parse().unwrap());
    }
    if matches.is_present("list") {
        checklist.display();
    }
    if let Some(file) = matches.value_of("save") {
        checklist.save(file);
    }
    if let Some(file) = matches.value_of("export-csv") {
        checklist.export_csv(file);
    }
    if let Some(file) = matches.value_of("export-txt") {
        checklist.export_txt(file);
    }

    // Автосохранение
    if !matches.is_present("load") && !matches.is_present("new") && matches.value_of("template").is_none() &&
       !matches.is_present("save") && (matches.is_present("add") || matches.is_present("pack") ||
       matches.is_present("unpack") || matches.is_present("remove")) {
        checklist.save(DATA_FILE);
    }

    if matches.args_present() == 0 {
        println!("Используйте --help для справки.");
    }
}
