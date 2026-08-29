// TravelChecklist.kt
import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TravelChecklist {
    @Parameter(names = ["--template"])
    private var template: String? = null

    @Parameter(names = ["--name"])
    private var name: String? = null

    @Parameter(names = ["--add"])
    private var addText: String? = null

    @Parameter(names = ["--add-category"])
    private var addCategory: String? = null

    @Parameter(names = ["--pack"])
    private var packId: Int? = null

    @Parameter(names = ["--unpack"])
    private var unpackId: Int? = null

    @Parameter(names = ["--remove"])
    private var removeId: Int? = null

    @Parameter(names = ["--list"])
    private var list: Boolean = false

    @Parameter(names = ["--save"])
    private var saveFile: String? = null

    @Parameter(names = ["--load"])
    private var loadFile: String? = null

    @Parameter(names = ["--new"])
    private var newFlag: Boolean = false

    @Parameter(names = ["--export-csv"])
    private var exportCsv: String? = null

    @Parameter(names = ["--export-txt"])
    private var exportTxt: String? = null

    data class Item(val id: Int, val text: String, val category: String, val packed: Boolean = false)
    data class ChecklistData(var name: String = "Путешествие", val items: MutableList<Item> = mutableListOf(), var nextId: Int = 1)

    private val dataFile = "checklist.json"
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val type = object : TypeToken<ChecklistData>() {}.type
    private var data = ChecklistData()
    private val templates = mutableMapOf<String, MutableMap<String, MutableList<String>>>()

    init {
        initTemplates()
    }

    private fun initTemplates() {
        // Пляж
        templates["beach"] = mutableMapOf(
            "Документы" to mutableListOf("Паспорт", "Билеты", "Страховка", "Виза"),
            "Одежда" to mutableListOf("Купальник", "Шорты", "Футболка", "Панама", "Сандалии", "Пляжное полотенце"),
            "Гигиена" to mutableListOf("Солнцезащитный крем", "Очки", "Шампунь", "Зубная щетка", "Гель для душа"),
            "Техника" to mutableListOf("Телефон", "Зарядка", "Наушники", "Повербанк"),
            "Медицина" to mutableListOf("Аптечка", "Средство от укусов", "Пластырь")
        )
        // Горы
        templates["mountain"] = mutableMapOf(
            "Документы" to mutableListOf("Паспорт", "Страховка", "Карта", "Компас"),
            "Одежда" to mutableListOf("Термобелье", "Флис", "Куртка", "Штаны", "Трекинговая обувь", "Носки", "Шапка", "Перчатки"),
            "Снаряжение" to mutableListOf("Рюкзак", "Палатка", "Спальник", "Каремат", "Фонарик", "Трекинговые палки"),
            "Еда" to mutableListOf("Горелка", "Топливо", "Посуда", "Еда в пакетах", "Вода", "Термос"),
            "Техника" to mutableListOf("Телефон", "Зарядка", "Навигатор", "Радиостанция")
        )
        // Командировка
        templates["business"] = mutableMapOf(
            "Документы" to mutableListOf("Паспорт", "Билеты", "Командировочное удостоверение", "Виза", "Страховка"),
            "Одежда" to mutableListOf("Костюм", "Рубашки", "Галстук", "Туфли", "Носки", "Ремень"),
            "Техника" to mutableListOf("Ноутбук", "Зарядка", "Презентация", "Флешка", "Телефон", "Адаптер"),
            "Гигиена" to mutableListOf("Дезодорант", "Зубная щетка", "Паста", "Расческа", "Шампунь"),
            "Прочее" to mutableListOf("Визитки", "Блокнот", "Ручка", "Зонт")
        )
        // Поход
        templates["hiking"] = mutableMapOf(
            "Документы" to mutableListOf("Паспорт", "Карта", "Разрешение", "Страховка"),
            "Снаряжение" to mutableListOf("Рюкзак", "Палатка", "Спальник", "Каремат", "Котелок", "Нож"),
            "Одежда" to mutableListOf("Треккинговая обувь", "Носки", "Флис", "Куртка", "Штаны", "Дождевик", "Головной убор"),
            "Еда" to mutableListOf("Еда в пакетах", "Сухой паек", "Вода", "Термос", "Горелка"),
            "Медицина" to mutableListOf("Аптечка", "Эластичный бинт", "Пластырь", "Средство от насекомых")
        )
        // Круиз
        templates["cruise"] = mutableMapOf(
            "Документы" to mutableListOf("Паспорт", "Виза", "Билеты", "Страховка", "Медицинская справка"),
            "Одежда" to mutableListOf("Праздничный наряд", "Пляжная одежда", "Купальник", "Сандалии", "Туфли", "Пижама"),
            "Гигиена" to mutableListOf("Солнцезащитный крем", "Шампунь", "Зубная щетка", "Дезодорант", "Москитная сетка"),
            "Техника" to mutableListOf("Телефон", "Зарядка", "Фотоаппарат", "Наушники"),
            "Прочее" to mutableListOf("Книга", "Ласты", "Маска для плавания", "Бинокль")
        )
        // Фестиваль
        templates["festival"] = mutableMapOf(
            "Документы" to mutableListOf("Паспорт", "Билет на фестиваль", "Страховка"),
            "Одежда" to mutableListOf("Яркая одежда", "Дождевик", "Удобная обувь", "Головной убор", "Солнцезащитные очки"),
            "Еда" to mutableListOf("Вода", "Еда в пакетах", "Термос", "Пикник"),
            "Техника" to mutableListOf("Телефон", "Зарядка", "Наушники", "Повербанк"),
            "Прочее" to mutableListOf("Плед", "Стул", "Флаг", "Настольные игры")
        )
    }

    private fun load(filename: String) {
        try {
            val json = File(filename).readText()
            data = gson.fromJson(json, type) ?: ChecklistData()
        } catch (e: Exception) {
            data = ChecklistData()
        }
    }

    private fun save(filename: String) {
        val json = gson.toJson(data)
        File(filename).writeText(json)
        println("\u001B[32mЧек-лист сохранён в $filename\u001B[0m")
    }

    private fun loadTemplate(templateName: String) {
        val tmpl = templates[templateName] ?: return
        data = ChecklistData()
        data.name = when (templateName) {
            "beach" -> "Пляжный отдых"
            "mountain" -> "Горный поход"
            "business" -> "Командировка"
            "hiking" -> "Пеший поход"
            "cruise" -> "Круиз"
            "festival" -> "Фестиваль"
            else -> "Путешествие"
        }
        data.nextId = 1
        for ((category, items) in tmpl) {
            for (text in items) {
                data.items.add(Item(data.nextId++, text, category))
            }
        }
    }

    private fun addItem(text: String, category: String?) {
        val cat = category ?: "Прочее"
        data.items.add(Item(data.nextId++, text, cat))
        println("\u001B[32mПункт добавлен (ID: ${data.nextId - 1})\u001B[0m")
    }

    private fun removeItem(id: Int) {
        val idx = data.items.indexOfFirst { it.id == id }
        if (idx == -1) {
            println("\u001B[31mПункт #$id не найден.\u001B[0m")
            return
        }
        data.items.removeAt(idx)
        println("\u001B[33mПункт #$id удалён.\u001B[0m")
    }

    private fun packItem(id: Int) {
        val idx = data.items.indexOfFirst { it.id == id }
        if (idx == -1) {
            println("\u001B[31mПункт #$id не найден.\u001B[0m")
            return
        }
        data.items[idx] = data.items[idx].copy(packed = true)
        println("\u001B[32mПункт #$id отмечен как собранный.\u001B[0m")
    }

    private fun unpackItem(id: Int) {
        val idx = data.items.indexOfFirst { it.id == id }
        if (idx == -1) {
            println("\u001B[31mПункт #$id не найден.\u001B[0m")
            return
        }
        data.items[idx] = data.items[idx].copy(packed = false)
        println("\u001B[33mОтметка с пункта #$id снята.\u001B[0m")
    }

    private fun display() {
        if (data.items.isEmpty()) {
            println("\u001B[33mЧек-лист пуст.\u001B[0m")
            return
        }
        val total = data.items.size
        val packed = data.items.count { it.packed }
        val pct = if (total > 0) packed * 100 / total else 0
        val barLen = 20
        val filled = pct * barLen / 100
        val bar = "█".repeat(filled) + "░".repeat(barLen - filled)
        println("\u001B[36m📋 Чек-лист: ${data.name}\u001B[0m")
        println("\u001B[32mПрогресс: $bar $pct% ($packed/$total)\u001B[0m")
        println()
        val categories = data.items.map { it.category }.distinct().sorted()
        for (cat in categories) {
            println("\u001B[33mКатегория: $cat\u001B[0m")
            for (item in data.items.filter { it.category == cat }) {
                val status = if (item.packed) "\u001B[32m✅\u001B[0m" else "\u001B[31m❌\u001B[0m"
                println("  ${item.id}. $status ${item.text}")
            }
        }
    }

    private fun exportCsv(filename: String) {
        File(filename).printWriter().use { pw ->
            pw.println("id,category,item,packed")
            data.items.forEach { pw.println("${it.id},${it.category},${it.text},${it.packed}") }
        }
        println("\u001B[32mЭкспортировано в $filename (CSV)\u001B[0m")
    }

    private fun exportTxt(filename: String) {
        File(filename).printWriter().use { pw ->
            pw.println("Чек-лист: ${data.name}")
            pw.println("Дата: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")
            pw.println()
            val categories = data.items.map { it.category }.distinct().sorted()
            for (cat in categories) {
                pw.println("=== $cat ===")
                data.items.filter { it.category == cat }.forEach {
                    pw.println("${if (it.packed) "[x]" else "[ ]"} ${it.text}")
                }
                pw.println()
            }
        }
        println("\u001B[32mЭкспортировано в $filename (TXT)\u001B[0m")
    }

    fun run() {
        when {
            loadFile != null -> load(loadFile!!)
            newFlag -> {
                data = ChecklistData()
                data.name = name ?: "Новый список"
                data.nextId = 1
            }
            template != null -> {
                loadTemplate(template!!)
                if (name != null) data.name = name!!
            }
            else -> {
                if (File(dataFile).exists()) {
                    load(dataFile)
                } else {
                    data = ChecklistData()
                    data.name = name ?: "Мой список"
                }
            }
        }

        addText?.let { addItem(it, addCategory) }
        packId?.let { packItem(it) }
        unpackId?.let { unpackItem(it) }
        removeId?.let { removeItem(it) }
        if (list) display()
        saveFile?.let { save(it) }
        exportCsv?.let { exportCsv(it) }
        exportTxt?.let { exportTxt(it) }

        if (loadFile == null && !newFlag && template == null && saveFile == null &&
            (addText != null || packId != null || unpackId != null || removeId != null)) {
            save(dataFile)
        }

        if (!list && addText == null && packId == null && unpackId == null && removeId == null &&
            saveFile == null && exportCsv == null && exportTxt == null && loadFile == null && !newFlag) {
            println("Используйте --help для справки.")
        }
    }
}

fun main(args: Array<String>) {
    val checklist = TravelChecklist()
    JCommander.newBuilder().addObject(checklist).build().parse(*args)
    checklist.run()
}
