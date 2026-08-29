// TravelChecklist.java
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class TravelChecklist {
    private static final String DATA_FILE = "checklist.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type LIST_TYPE = new TypeToken<List<Item>>(){}.getType();

    @Parameter(names = "--template")
    private String template;
    @Parameter(names = "--name")
    private String name;
    @Parameter(names = "--add")
    private String addText;
    @Parameter(names = "--add-category")
    private String addCategory;
    @Parameter(names = "--pack")
    private Integer packId;
    @Parameter(names = "--unpack")
    private Integer unpackId;
    @Parameter(names = "--remove")
    private Integer removeId;
    @Parameter(names = "--list")
    private boolean list;
    @Parameter(names = "--save")
    private String saveFile;
    @Parameter(names = "--load")
    private String loadFile;
    @Parameter(names = "--new")
    private boolean newFlag;
    @Parameter(names = "--export-csv")
    private String exportCsv;
    @Parameter(names = "--export-txt")
    private String exportTxt;

    static class Item {
        int id;
        String text, category;
        boolean packed;
        Item(int id, String text, String category) {
            this.id = id;
            this.text = text;
            this.category = category;
            this.packed = false;
        }
    }

    static class ChecklistData {
        String name;
        List<Item> items = new ArrayList<>();
        int nextId = 1;
    }

    private ChecklistData data = new ChecklistData();
    private Map<String, Map<String, List<String>>> templates = new HashMap<>();

    public TravelChecklist() {
        initTemplates();
    }

    private void initTemplates() {
        // Пляж
        Map<String, List<String>> beach = new LinkedHashMap<>();
        beach.put("Документы", Arrays.asList("Паспорт", "Билеты", "Страховка", "Виза"));
        beach.put("Одежда", Arrays.asList("Купальник", "Шорты", "Футболка", "Панама", "Сандалии", "Пляжное полотенце"));
        beach.put("Гигиена", Arrays.asList("Солнцезащитный крем", "Очки", "Шампунь", "Зубная щетка", "Гель для душа"));
        beach.put("Техника", Arrays.asList("Телефон", "Зарядка", "Наушники", "Повербанк"));
        beach.put("Медицина", Arrays.asList("Аптечка", "Средство от укусов", "Пластырь"));
        templates.put("beach", beach);

        // Горы
        Map<String, List<String>> mountain = new LinkedHashMap<>();
        mountain.put("Документы", Arrays.asList("Паспорт", "Страховка", "Карта", "Компас"));
        mountain.put("Одежда", Arrays.asList("Термобелье", "Флис", "Куртка", "Штаны", "Трекинговая обувь", "Носки", "Шапка", "Перчатки"));
        mountain.put("Снаряжение", Arrays.asList("Рюкзак", "Палатка", "Спальник", "Каремат", "Фонарик", "Трекинговые палки"));
        mountain.put("Еда", Arrays.asList("Горелка", "Топливо", "Посуда", "Еда в пакетах", "Вода", "Термос"));
        mountain.put("Техника", Arrays.asList("Телефон", "Зарядка", "Навигатор", "Радиостанция"));
        templates.put("mountain", mountain);

        // Командировка
        Map<String, List<String>> business = new LinkedHashMap<>();
        business.put("Документы", Arrays.asList("Паспорт", "Билеты", "Командировочное удостоверение", "Виза", "Страховка"));
        business.put("Одежда", Arrays.asList("Костюм", "Рубашки", "Галстук", "Туфли", "Носки", "Ремень"));
        business.put("Техника", Arrays.asList("Ноутбук", "Зарядка", "Презентация", "Флешка", "Телефон", "Адаптер"));
        business.put("Гигиена", Arrays.asList("Дезодорант", "Зубная щетка", "Паста", "Расческа", "Шампунь"));
        business.put("Прочее", Arrays.asList("Визитки", "Блокнот", "Ручка", "Зонт"));
        templates.put("business", business);

        // Поход
        Map<String, List<String>> hiking = new LinkedHashMap<>();
        hiking.put("Документы", Arrays.asList("Паспорт", "Карта", "Разрешение", "Страховка"));
        hiking.put("Снаряжение", Arrays.asList("Рюкзак", "Палатка", "Спальник", "Каремат", "Котелок", "Нож"));
        hiking.put("Одежда", Arrays.asList("Треккинговая обувь", "Носки", "Флис", "Куртка", "Штаны", "Дождевик", "Головной убор"));
        hiking.put("Еда", Arrays.asList("Еда в пакетах", "Сухой паек", "Вода", "Термос", "Горелка"));
        hiking.put("Медицина", Arrays.asList("Аптечка", "Эластичный бинт", "Пластырь", "Средство от насекомых"));
        templates.put("hiking", hiking);

        // Круиз
        Map<String, List<String>> cruise = new LinkedHashMap<>();
        cruise.put("Документы", Arrays.asList("Паспорт", "Виза", "Билеты", "Страховка", "Медицинская справка"));
        cruise.put("Одежда", Arrays.asList("Праздничный наряд", "Пляжная одежда", "Купальник", "Сандалии", "Туфли", "Пижама"));
        cruise.put("Гигиена", Arrays.asList("Солнцезащитный крем", "Шампунь", "Зубная щетка", "Дезодорант", "Москитная сетка"));
        cruise.put("Техника", Arrays.asList("Телефон", "Зарядка", "Фотоаппарат", "Наушники"));
        cruise.put("Прочее", Arrays.asList("Книга", "Ласты", "Маска для плавания", "Бинокль"));
        templates.put("cruise", cruise);

        // Фестиваль
        Map<String, List<String>> festival = new LinkedHashMap<>();
        festival.put("Документы", Arrays.asList("Паспорт", "Билет на фестиваль", "Страховка"));
        festival.put("Одежда", Arrays.asList("Яркая одежда", "Дождевик", "Удобная обувь", "Головной убор", "Солнцезащитные очки"));
        festival.put("Еда", Arrays.asList("Вода", "Еда в пакетах", "Термос", "Пикник"));
        festival.put("Техника", Arrays.asList("Телефон", "Зарядка", "Наушники", "Повербанк"));
        festival.put("Прочее", Arrays.asList("Плед", "Стул", "Флаг", "Настольные игры"));
        templates.put("festival", festival);
    }

    private void load() {
        try {
            String json = new String(Files.readAllBytes(Paths.get(DATA_FILE)));
            ChecklistData loaded = GSON.fromJson(json, ChecklistData.class);
            data.name = loaded.name;
            data.items = loaded.items;
            data.nextId = loaded.nextId;
        } catch (Exception e) {
            data = new ChecklistData();
            data.nextId = 1;
        }
    }

    private void save(String filename) {
        try {
            Files.write(Paths.get(filename), GSON.toJson(data).getBytes());
            System.out.println("\u001B[32mЧек-лист сохранён в " + filename + "\u001B[0m");
        } catch (IOException e) {
            System.err.println("Ошибка сохранения: " + e.getMessage());
        }
    }

    private void loadTemplate(String templateName) {
        Map<String, List<String>> tmpl = templates.get(templateName);
        if (tmpl == null) return;
        data = new ChecklistData();
        data.name = templateName.equals("beach") ? "Пляжный отдых" :
                    templateName.equals("mountain") ? "Горный поход" :
                    templateName.equals("business") ? "Командировка" :
                    templateName.equals("hiking") ? "Пеший поход" :
                    templateName.equals("cruise") ? "Круиз" : "Фестиваль";
        data.nextId = 1;
        for (Map.Entry<String, List<String>> entry : tmpl.entrySet()) {
            for (String text : entry.getValue()) {
                data.items.add(new Item(data.nextId++, text, entry.getKey()));
            }
        }
    }

    private void addItem(String text, String category) {
        if (category == null || category.isEmpty()) category = "Прочее";
        data.items.add(new Item(data.nextId++, text, category));
        System.out.println("\u001B[32mПункт добавлен (ID: " + (data.nextId-1) + ")\u001B[0m");
    }

    private void removeItem(int id) {
        Iterator<Item> it = data.items.iterator();
        while (it.hasNext()) {
            Item item = it.next();
            if (item.id == id) {
                it.remove();
                System.out.println("\u001B[33mПункт #" + id + " удалён.\u001B[0m");
                return;
            }
        }
        System.out.println("\u001B[31mПункт #" + id + " не найден.\u001B[0m");
    }

    private void packItem(int id) {
        for (Item item : data.items) {
            if (item.id == id) {
                item.packed = true;
                System.out.println("\u001B[32mПункт #" + id + " отмечен как собранный.\u001B[0m");
                return;
            }
        }
        System.out.println("\u001B[31mПункт #" + id + " не найден.\u001B[0m");
    }

    private void unpackItem(int id) {
        for (Item item : data.items) {
            if (item.id == id) {
                item.packed = false;
                System.out.println("\u001B[33mОтметка с пункта #" + id + " снята.\u001B[0m");
                return;
            }
        }
        System.out.println("\u001B[31mПункт #" + id + " не найден.\u001B[0m");
    }

    private void display() {
        if (data.items.isEmpty()) {
            System.out.println("\u001B[33mЧек-лист пуст.\u001B[0m");
            return;
        }
        int total = data.items.size();
        int packed = 0;
        for (Item item : data.items) {
            if (item.packed) packed++;
        }
        int pct = total > 0 ? packed * 100 / total : 0;
        int barLen = 20;
        int filled = pct * barLen / 100;
        String bar = "█".repeat(filled) + "░".repeat(barLen - filled);
        System.out.println("\u001B[36m📋 Чек-лист: " + data.name + "\u001B[0m");
        System.out.println("\u001B[32mПрогресс: " + bar + " " + pct + "% (" + packed + "/" + total + ")\u001B[0m\n");

        Set<String> categories = new TreeSet<>();
        for (Item item : data.items) categories.add(item.category);
        for (String cat : categories) {
            System.out.println("\u001B[33mКатегория: " + cat + "\u001B[0m");
            for (Item item : data.items) {
                if (item.category.equals(cat)) {
                    String status = item.packed ? "\u001B[32m✅\u001B[0m" : "\u001B[31m❌\u001B[0m";
                    System.out.println("  " + item.id + ". " + status + " " + item.text);
                }
            }
        }
    }

    private void exportCsv(String filename) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.println("id,category,item,packed");
            for (Item item : data.items) {
                pw.printf("%d,%s,%s,%b%n", item.id, item.category, item.text, item.packed);
            }
        }
        System.out.println("\u001B[32mЭкспортировано в " + filename + " (CSV)\u001B[0m");
    }

    private void exportTxt(String filename) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.println("Чек-лист: " + data.name);
            pw.println("Дата: " + new Date());
            pw.println();
            Set<String> categories = new TreeSet<>();
            for (Item item : data.items) categories.add(item.category);
            for (String cat : categories) {
                pw.println("=== " + cat + " ===");
                for (Item item : data.items) {
                    if (item.category.equals(cat)) {
                        pw.println((item.packed ? "[x]" : "[ ]") + " " + item.text);
                    }
                }
                pw.println();
            }
        }
        System.out.println("\u001B[32mЭкспортировано в " + filename + " (TXT)\u001B[0m");
    }

    public void run() throws Exception {
        if (loadFile != null) {
            String json = new String(Files.readAllBytes(Paths.get(loadFile)));
            data = GSON.fromJson(json, ChecklistData.class);
        } else if (newFlag) {
            data = new ChecklistData();
            data.name = name != null ? name : "Новый список";
            data.nextId = 1;
        } else if (template != null) {
            loadTemplate(template);
            if (name != null) data.name = name;
        } else {
            // Попытка загрузить из файла по умолчанию
            try {
                String json = new String(Files.readAllBytes(Paths.get(DATA_FILE)));
                data = GSON.fromJson(json, ChecklistData.class);
            } catch (Exception e) {
                data = new ChecklistData();
                data.name = name != null ? name : "Мой список";
                data.nextId = 1;
            }
        }

        if (addText != null) {
            addItem(addText, addCategory);
        }
        if (packId != null) packItem(packId);
        if (unpackId != null) unpackItem(unpackId);
        if (removeId != null) removeItem(removeId);
        if (list) display();
        if (saveFile != null) save(saveFile);
        if (exportCsv != null) exportCsv(exportCsv);
        if (exportTxt != null) exportTxt(exportTxt);

        if (loadFile == null && !newFlag && template == null && saveFile == null &&
            (addText != null || packId != null || unpackId != null || removeId != null)) {
            save(DATA_FILE);
        }

        if (!list && addText == null && packId == null && unpackId == null && removeId == null &&
            saveFile == null && exportCsv == null && exportTxt == null && loadFile == null && !newFlag) {
            System.out.println("Используйте --help для справки.");
        }
    }

    public static void main(String[] args) throws Exception {
        TravelChecklist checklist = new TravelChecklist();
        JCommander.newBuilder().addObject(checklist).build().parse(args);
        checklist.run();
    }
}
