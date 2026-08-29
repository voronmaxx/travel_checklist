// travel_checklist.cpp
#include <iostream>
#include <fstream>
#include <string>
#include <vector>
#include <map>
#include <algorithm>
#include <ctime>
#include <iomanip>
#include <sstream>
#include <json/json.h> // using jsoncpp

using namespace std;

const string DATA_FILE = "checklist.json";

struct Item {
    int id;
    string text;
    string category;
    bool packed;
};

struct ChecklistData {
    string name;
    vector<Item> items;
    int nextId = 1;
};

class TravelChecklist {
private:
    ChecklistData data;
    map<string, map<string, vector<string>>> templates;

    void initTemplates() {
        // Пляж
        templates["beach"]["Документы"] = {"Паспорт", "Билеты", "Страховка", "Виза"};
        templates["beach"]["Одежда"] = {"Купальник", "Шорты", "Футболка", "Панама", "Сандалии", "Пляжное полотенце"};
        templates["beach"]["Гигиена"] = {"Солнцезащитный крем", "Очки", "Шампунь", "Зубная щетка", "Гель для душа"};
        templates["beach"]["Техника"] = {"Телефон", "Зарядка", "Наушники", "Повербанк"};
        templates["beach"]["Медицина"] = {"Аптечка", "Средство от укусов", "Пластырь"};

        // Горы
        templates["mountain"]["Документы"] = {"Паспорт", "Страховка", "Карта", "Компас"};
        templates["mountain"]["Одежда"] = {"Термобелье", "Флис", "Куртка", "Штаны", "Трекинговая обувь", "Носки", "Шапка", "Перчатки"};
        templates["mountain"]["Снаряжение"] = {"Рюкзак", "Палатка", "Спальник", "Каремат", "Фонарик", "Трекинговые палки"};
        templates["mountain"]["Еда"] = {"Горелка", "Топливо", "Посуда", "Еда в пакетах", "Вода", "Термос"};
        templates["mountain"]["Техника"] = {"Телефон", "Зарядка", "Навигатор", "Радиостанция"};

        // Командировка
        templates["business"]["Документы"] = {"Паспорт", "Билеты", "Командировочное удостоверение", "Виза", "Страховка"};
        templates["business"]["Одежда"] = {"Костюм", "Рубашки", "Галстук", "Туфли", "Носки", "Ремень"};
        templates["business"]["Техника"] = {"Ноутбук", "Зарядка", "Презентация", "Флешка", "Телефон", "Адаптер"};
        templates["business"]["Гигиена"] = {"Дезодорант", "Зубная щетка", "Паста", "Расческа", "Шампунь"};
        templates["business"]["Прочее"] = {"Визитки", "Блокнот", "Ручка", "Зонт"};

        // Поход
        templates["hiking"]["Документы"] = {"Паспорт", "Карта", "Разрешение", "Страховка"};
        templates["hiking"]["Снаряжение"] = {"Рюкзак", "Палатка", "Спальник", "Каремат", "Котелок", "Нож"};
        templates["hiking"]["Одежда"] = {"Треккинговая обувь", "Носки", "Флис", "Куртка", "Штаны", "Дождевик", "Головной убор"};
        templates["hiking"]["Еда"] = {"Еда в пакетах", "Сухой паек", "Вода", "Термос", "Горелка"};
        templates["hiking"]["Медицина"] = {"Аптечка", "Эластичный бинт", "Пластырь", "Средство от насекомых"};

        // Круиз
        templates["cruise"]["Документы"] = {"Паспорт", "Виза", "Билеты", "Страховка", "Медицинская справка"};
        templates["cruise"]["Одежда"] = {"Праздничный наряд", "Пляжная одежда", "Купальник", "Сандалии", "Туфли", "Пижама"};
        templates["cruise"]["Гигиена"] = {"Солнцезащитный крем", "Шампунь", "Зубная щетка", "Дезодорант", "Москитная сетка"};
        templates["cruise"]["Техника"] = {"Телефон", "Зарядка", "Фотоаппарат", "Наушники"};
        templates["cruise"]["Прочее"] = {"Книга", "Ласты", "Маска для плавания", "Бинокль"};

        // Фестиваль
        templates["festival"]["Документы"] = {"Паспорт", "Билет на фестиваль", "Страховка"};
        templates["festival"]["Одежда"] = {"Яркая одежда", "Дождевик", "Удобная обувь", "Головной убор", "Солнцезащитные очки"};
        templates["festival"]["Еда"] = {"Вода", "Еда в пакетах", "Термос", "Пикник"};
        templates["festival"]["Техника"] = {"Телефон", "Зарядка", "Наушники", "Повербанк"};
        templates["festival"]["Прочее"] = {"Плед", "Стул", "Флаг", "Настольные игры"};
    }

    void load() {
        ifstream ifs(DATA_FILE);
        if (!ifs) return;
        Json::Value root;
        ifs >> root;
        data.name = root["name"].asString();
        for (const auto& item : root["items"]) {
            Item i;
            i.id = item["id"].asInt();
            i.text = item["text"].asString();
            i.category = item["category"].asString();
            i.packed = item["packed"].asBool();
            data.items.push_back(i);
        }
        data.nextId = root["next_id"].asInt();
    }

    void save(const string& filename) {
        Json::Value root;
        root["name"] = data.name;
        for (const auto& i : data.items) {
            Json::Value item;
            item["id"] = i.id;
            item["text"] = i.text;
            item["category"] = i.category;
            item["packed"] = i.packed;
            root["items"].append(item);
        }
        root["next_id"] = data.nextId;
        ofstream ofs(filename);
        ofs << root.toStyledString();
        cout << "\033[32mЧек-лист сохранён в " << filename << "\033[0m" << endl;
    }

    string currentTime() {
        time_t t = time(nullptr);
        char buf[64];
        strftime(buf, sizeof(buf), "%Y-%m-%d %H:%M:%S", localtime(&t));
        return string(buf);
    }

public:
    TravelChecklist() {
        initTemplates();
        data.name = "Путешествие";
        data.nextId = 1;
    }

    void loadTemplate(const string& tmplName) {
        if (templates.find(tmplName) == templates.end()) return;
        data.items.clear();
        data.nextId = 1;
        if (tmplName == "beach") data.name = "Пляжный отдых";
        else if (tmplName == "mountain") data.name = "Горный поход";
        else if (tmplName == "business") data.name = "Командировка";
        else if (tmplName == "hiking") data.name = "Пеший поход";
        else if (tmplName == "cruise") data.name = "Круиз";
        else if (tmplName == "festival") data.name = "Фестиваль";
        else data.name = "Путешествие";
        for (const auto& kv : templates[tmplName]) {
            for (const auto& text : kv.second) {
                data.items.push_back({data.nextId++, text, kv.first, false});
            }
        }
    }

    void addItem(const string& text, const string& category) {
        string cat = category.empty() ? "Прочее" : category;
        data.items.push_back({data.nextId++, text, cat, false});
        cout << "\033[32mПункт добавлен (ID: " << data.nextId-1 << ")\033[0m" << endl;
    }

    void removeItem(int id) {
        auto it = remove_if(data.items.begin(), data.items.end(), [id](const Item& i) { return i.id == id; });
        if (it != data.items.end()) {
            data.items.erase(it, data.items.end());
            cout << "\033[33mПункт #" << id << " удалён.\033[0m" << endl;
        } else {
            cout << "\033[31mПункт #" << id << " не найден.\033[0m" << endl;
        }
    }

    void packItem(int id) {
        for (auto& i : data.items) {
            if (i.id == id) {
                i.packed = true;
                cout << "\033[32mПункт #" << id << " отмечен как собранный.\033[0m" << endl;
                return;
            }
        }
        cout << "\033[31mПункт #" << id << " не найден.\033[0m" << endl;
    }

    void unpackItem(int id) {
        for (auto& i : data.items) {
            if (i.id == id) {
                i.packed = false;
                cout << "\033[33mОтметка с пункта #" << id << " снята.\033[0m" << endl;
                return;
            }
        }
        cout << "\033[31mПункт #" << id << " не найден.\033[0m" << endl;
    }

    void display() {
        if (data.items.empty()) {
            cout << "\033[33mЧек-лист пуст.\033[0m" << endl;
            return;
        }
        int total = data.items.size();
        int packed = 0;
        for (const auto& i : data.items) if (i.packed) packed++;
        int pct = total > 0 ? packed * 100 / total : 0;
        int barLen = 20;
        int filled = pct * barLen / 100;
        string bar(filled, '█');
        bar.append(barLen - filled, '░');
        cout << "\033[36m📋 Чек-лист: " << data.name << "\033[0m" << endl;
        cout << "\033[32mПрогресс: " << bar << " " << pct << "% (" << packed << "/" << total << ")\033[0m" << endl << endl;

        set<string> categories;
        for (const auto& i : data.items) categories.insert(i.category);
        for (const auto& cat : categories) {
            cout << "\033[33mКатегория: " << cat << "\033[0m" << endl;
            for (const auto& i : data.items) {
                if (i.category == cat) {
                    string status = i.packed ? "\033[32m✅\033[0m" : "\033[31m❌\033[0m";
                    cout << "  " << i.id << ". " << status << " " << i.text << endl;
                }
            }
        }
    }

    void exportCSV(const string& filename) {
        ofstream ofs(filename);
        ofs << "id,category,item,packed\n";
        for (const auto& i : data.items) {
            ofs << i.id << "," << i.category << "," << i.text << "," << i.packed << "\n";
        }
        cout << "\033[32mЭкспортировано в " << filename << " (CSV)\033[0m" << endl;
    }

    void exportTXT(const string& filename) {
        ofstream ofs(filename);
        ofs << "Чек-лист: " << data.name << "\n";
        ofs << "Дата: " << currentTime() << "\n\n";
        set<string> categories;
        for (const auto& i : data.items) categories.insert(i.category);
        for (const auto& cat : categories) {
            ofs << "=== " << cat << " ===\n";
            for (const auto& i : data.items) {
                if (i.category == cat) {
                    ofs << (i.packed ? "[x]" : "[ ]") << " " << i.text << "\n";
                }
            }
            ofs << "\n";
        }
        cout << "\033[32mЭкспортировано в " << filename << " (TXT)\033[0m" << endl;
    }

    void loadFromFile(const string& filename) {
        load();
    }
};

int main(int argc, char* argv[]) {
    string templateType, name, add, addCategory, saveFile, loadFile, exportCsv, exportTxt;
    int pack = 0, unpack = 0, remove = 0;
    bool list = false, newFlag = false;

    for (int i = 1; i < argc; ++i) {
        string arg = argv[i];
        if (arg == "--template" && i+1 < argc) templateType = argv[++i];
        else if (arg == "--name" && i+1 < argc) name = argv[++i];
        else if (arg == "--add" && i+1 < argc) add = argv[++i];
        else if (arg == "--add-category" && i+1 < argc) addCategory = argv[++i];
        else if (arg == "--pack" && i+1 < argc) pack = stoi(argv[++i]);
        else if (arg == "--unpack" && i+1 < argc) unpack = stoi(argv[++i]);
        else if (arg == "--remove" && i+1 < argc) remove = stoi(argv[++i]);
        else if (arg == "--list") list = true;
        else if (arg == "--save" && i+1 < argc) saveFile = argv[++i];
        else if (arg == "--load" && i+1 < argc) loadFile = argv[++i];
        else if (arg == "--new") newFlag = true;
        else if (arg == "--export-csv" && i+1 < argc) exportCsv = argv[++i];
        else if (arg == "--export-txt" && i+1 < argc) exportTxt = argv[++i];
    }

    TravelChecklist checklist;

    if (!loadFile.empty()) {
        checklist.loadFromFile(loadFile);
    } else if (newFlag) {
        // уже пустой
    } else if (!templateType.empty()) {
        checklist.loadTemplate(templateType);
        if (!name.empty()) checklist.data.name = name;
    } else {
        // Попытка загрузить из файла по умолчанию
        ifstream test(DATA_FILE);
        if (test) {
            checklist.loadFromFile(DATA_FILE);
        } else {
            checklist.data.name = name.empty() ? "Мой список" : name;
        }
    }

    if (!add.empty()) {
        checklist.addItem(add, addCategory);
    }
    if (pack != 0) checklist.packItem(pack);
    if (unpack != 0) checklist.unpackItem(unpack);
    if (remove != 0) checklist.removeItem(remove);
    if (list) checklist.display();
    if (!saveFile.empty()) checklist.save(saveFile);
    if (!exportCsv.empty()) checklist.exportCSV(exportCsv);
    if (!exportTxt.empty()) checklist.exportTXT(exportTxt);

    if (loadFile.empty() && !newFlag && templateType.empty() && saveFile.empty() &&
        (!add.empty() || pack != 0 || unpack != 0 || remove != 0)) {
        checklist.save(DATA_FILE);
    }

    return 0;
}
