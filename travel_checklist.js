#!/usr/bin/env node
// travel_checklist.js
const { program } = require('commander');
const fs = require('fs');
const chalk = require('chalk');

const TEMPLATES = {
    beach: {
        name: 'Пляжный отдых',
        categories: {
            'Документы': ['Паспорт', 'Билеты', 'Страховка', 'Виза'],
            'Одежда': ['Купальник', 'Шорты', 'Футболка', 'Панама', 'Сандалии', 'Пляжное полотенце'],
            'Гигиена': ['Солнцезащитный крем', 'Очки', 'Шампунь', 'Зубная щетка', 'Гель для душа'],
            'Техника': ['Телефон', 'Зарядка', 'Наушники', 'Повербанк'],
            'Медицина': ['Аптечка', 'Средство от укусов', 'Пластырь']
        }
    },
    mountain: {
        name: 'Горный поход',
        categories: {
            'Документы': ['Паспорт', 'Страховка', 'Карта', 'Компас'],
            'Одежда': ['Термобелье', 'Флис', 'Куртка', 'Штаны', 'Трекинговая обувь', 'Носки', 'Шапка', 'Перчатки'],
            'Снаряжение': ['Рюкзак', 'Палатка', 'Спальник', 'Каремат', 'Фонарик', 'Трекинговые палки'],
            'Еда': ['Горелка', 'Топливо', 'Посуда', 'Еда в пакетах', 'Вода', 'Термос'],
            'Техника': ['Телефон', 'Зарядка', 'Навигатор', 'Радиостанция']
        }
    },
    business: {
        name: 'Командировка',
        categories: {
            'Документы': ['Паспорт', 'Билеты', 'Командировочное удостоверение', 'Виза', 'Страховка'],
            'Одежда': ['Костюм', 'Рубашки', 'Галстук', 'Туфли', 'Носки', 'Ремень'],
            'Техника': ['Ноутбук', 'Зарядка', 'Презентация', 'Флешка', 'Телефон', 'Адаптер'],
            'Гигиена': ['Дезодорант', 'Зубная щетка', 'Паста', 'Расческа', 'Шампунь'],
            'Прочее': ['Визитки', 'Блокнот', 'Ручка', 'Зонт']
        }
    },
    hiking: {
        name: 'Пеший поход',
        categories: {
            'Документы': ['Паспорт', 'Карта', 'Разрешение', 'Страховка'],
            'Снаряжение': ['Рюкзак', 'Палатка', 'Спальник', 'Каремат', 'Котелок', 'Нож'],
            'Одежда': ['Треккинговая обувь', 'Носки', 'Флис', 'Куртка', 'Штаны', 'Дождевик', 'Головной убор'],
            'Еда': ['Еда в пакетах', 'Сухой паек', 'Вода', 'Термос', 'Горелка'],
            'Медицина': ['Аптечка', 'Эластичный бинт', 'Пластырь', 'Средство от насекомых']
        }
    },
    cruise: {
        name: 'Круиз',
        categories: {
            'Документы': ['Паспорт', 'Виза', 'Билеты', 'Страховка', 'Медицинская справка'],
            'Одежда': ['Праздничный наряд', 'Пляжная одежда', 'Купальник', 'Сандалии', 'Туфли', 'Пижама'],
            'Гигиена': ['Солнцезащитный крем', 'Шампунь', 'Зубная щетка', 'Дезодорант', 'Москитная сетка'],
            'Техника': ['Телефон', 'Зарядка', 'Фотоаппарат', 'Наушники'],
            'Прочее': ['Книга', 'Ласты', 'Маска для плавания', 'Бинокль']
        }
    },
    festival: {
        name: 'Фестиваль',
        categories: {
            'Документы': ['Паспорт', 'Билет на фестиваль', 'Страховка'],
            'Одежда': ['Яркая одежда', 'Дождевик', 'Удобная обувь', 'Головной убор', 'Солнцезащитные очки'],
            'Еда': ['Вода', 'Еда в пакетах', 'Термос', 'Пикник'],
            'Техника': ['Телефон', 'Зарядка', 'Наушники', 'Повербанк'],
            'Прочее': ['Плед', 'Стул', 'Флаг', 'Настольные игры']
        }
    }
};

class Item {
    constructor(id, text, category, packed = false) {
        this.id = id;
        this.text = text;
        this.category = category;
        this.packed = packed;
    }
}

class TravelChecklist {
    constructor(name = 'Путешествие', template = null) {
        this.name = name;
        this.items = [];
        this.nextId = 1;
        if (template && TEMPLATES[template]) {
            this.loadTemplate(template);
        }
    }

    loadTemplate(templateName) {
        const template = TEMPLATES[templateName];
        this.name = template.name;
        this.items = [];
        this.nextId = 1;
        for (const [category, items] of Object.entries(template.categories)) {
            for (const text of items) {
                this.items.push(new Item(this.nextId, text, category));
                this.nextId++;
            }
        }
    }

    toJSON() {
        return {
            name: this.name,
            items: this.items.map(i => ({ id: i.id, text: i.text, category: i.category, packed: i.packed })),
            nextId: this.nextId
        };
    }

    static fromJSON(data) {
        const checklist = new TravelChecklist(data.name);
        checklist.items = data.items.map(i => new Item(i.id, i.text, i.category, i.packed));
        checklist.nextId = data.nextId || Math.max(...checklist.items.map(i => i.id), 0) + 1;
        return checklist;
    }

    addItem(text, category = 'Прочее') {
        const item = new Item(this.nextId, text, category);
        this.items.push(item);
        this.nextId++;
        console.log(chalk.green(`Пункт добавлен (ID: ${item.id})`));
        return item.id;
    }

    removeItem(id) {
        const idx = this.items.findIndex(i => i.id === id);
        if (idx === -1) {
            console.log(chalk.red(`Пункт #${id} не найден.`));
            return;
        }
        this.items.splice(idx, 1);
        console.log(chalk.yellow(`Пункт #${id} удалён.`));
    }

    packItem(id) {
        const item = this.items.find(i => i.id === id);
        if (!item) {
            console.log(chalk.red(`Пункт #${id} не найден.`));
            return;
        }
        item.packed = true;
        console.log(chalk.green(`Пункт #${id} отмечен как собранный.`));
    }

    unpackItem(id) {
        const item = this.items.find(i => i.id === id);
        if (!item) {
            console.log(chalk.red(`Пункт #${id} не найден.`));
            return;
        }
        item.packed = false;
        console.log(chalk.yellow(`Отметка с пункта #${id} снята.`));
    }

    getProgress() {
        const total = this.items.length;
        const packed = this.items.filter(i => i.packed).length;
        return { packed, total };
    }

    display() {
        if (this.items.length === 0) {
            console.log(chalk.yellow('Чек-лист пуст.'));
            return;
        }
        const { packed, total } = this.getProgress();
        const pct = total > 0 ? Math.floor(packed / total * 100) : 0;
        const barLen = 20;
        const filled = Math.floor(barLen * pct / 100);
        const bar = '█'.repeat(filled) + '░'.repeat(barLen - filled);
        console.log(chalk.cyan(`📋 Чек-лист: ${this.name}`));
        console.log(chalk.green(`Прогресс: ${bar} ${pct}% (${packed}/${total})`));
        console.log();
        const categories = [...new Set(this.items.map(i => i.category))].sort();
        for (const cat of categories) {
            const catItems = this.items.filter(i => i.category === cat);
            console.log(chalk.yellow(`Категория: ${cat}`));
            for (const item of catItems) {
                const status = item.packed ? chalk.green('✅') : chalk.red('❌');
                console.log(`  ${item.id}. ${status} ${item.text}`);
            }
        }
    }

    save(filename = 'checklist.json') {
        fs.writeFileSync(filename, JSON.stringify(this.toJSON(), null, 2));
        console.log(chalk.green(`Чек-лист сохранён в ${filename}`));
    }

    static load(filename = 'checklist.json') {
        try {
            const data = JSON.parse(fs.readFileSync(filename, 'utf8'));
            return TravelChecklist.fromJSON(data);
        } catch (e) {
            console.log(chalk.red(`Ошибка загрузки: ${e.message}`));
            return null;
        }
    }

    exportCsv(filename) {
        const header = 'id,category,item,packed\n';
        const rows = this.items.map(i => `${i.id},${i.category},${i.text},${i.packed}`).join('\n');
        fs.writeFileSync(filename, header + rows);
        console.log(chalk.green(`Экспортировано в ${filename} (CSV)`));
    }

    exportTxt(filename) {
        let content = `Чек-лист: ${this.name}\n`;
        content += `Дата: ${new Date().toLocaleString()}\n\n`;
        const categories = [...new Set(this.items.map(i => i.category))].sort();
        for (const cat of categories) {
            content += `=== ${cat} ===\n`;
            for (const item of this.items.filter(i => i.category === cat)) {
                content += `${item.packed ? '[x]' : '[ ]'} ${item.text}\n`;
            }
            content += '\n';
        }
        fs.writeFileSync(filename, content);
        console.log(chalk.green(`Экспортировано в ${filename} (TXT)`));
    }
}

program
    .option('--template <type>', 'Тип путешествия: beach, mountain, business, hiking, cruise, festival')
    .option('--name <name>', 'Название чек-листа')
    .option('--add <text>', 'Добавить пункт')
    .option('--add-category <category>', 'Категория для добавляемого пункта')
    .option('--pack <id>', 'Отметить пункт как собранный', parseInt)
    .option('--unpack <id>', 'Снять отметку с пункта', parseInt)
    .option('--remove <id>', 'Удалить пункт', parseInt)
    .option('--list', 'Показать чек-лист')
    .option('--save <file>', 'Сохранить чек-лист')
    .option('--load <file>', 'Загрузить чек-лист из файла')
    .option('--new', 'Создать новый пустой чек-лист')
    .option('--export-csv <file>', 'Экспорт в CSV')
    .option('--export-txt <file>', 'Экспорт в TXT')
    .parse(process.argv);

const opts = program.opts();
let checklist = null;

if (opts.load) {
    checklist = TravelChecklist.load(opts.load);
    if (!checklist) process.exit(1);
} else if (opts.new) {
    checklist = new TravelChecklist(opts.name || 'Новый список');
} else if (opts.template) {
    checklist = new TravelChecklist(opts.name, opts.template);
} else {
    // Попытка загрузить из файла по умолчанию
    if (fs.existsSync('checklist.json')) {
        checklist = TravelChecklist.load('checklist.json');
    }
    if (!checklist) {
        checklist = new TravelChecklist(opts.name || 'Мой список');
    }
}

if (opts.add) {
    const category = opts.addCategory || 'Прочее';
    checklist.addItem(opts.add, category);
}
if (opts.pack) checklist.packItem(opts.pack);
if (opts.unpack) checklist.unpackItem(opts.unpack);
if (opts.remove) checklist.removeItem(opts.remove);
if (opts.list) checklist.display();
if (opts.save) checklist.save(opts.save);
if (opts.exportCsv) checklist.exportCsv(opts.exportCsv);
if (opts.exportTxt) checklist.exportTxt(opts.exportTxt);

// Автосохранение
if (!opts.load && !opts.new && !opts.template && !opts.save && (opts.add || opts.pack || opts.unpack || opts.remove)) {
    checklist.save('checklist.json');
}

if (!process.argv.slice(2).length) {
    program.help();
}
