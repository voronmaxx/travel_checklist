// travel_checklist.go
package main

import (
	"encoding/csv"
	"encoding/json"
	"flag"
	"fmt"
	"os"
	"sort"
	"strconv"
	"strings"
	"time"
)

const dataFile = "checklist.json"

type Item struct {
	ID       int    `json:"id"`
	Text     string `json:"text"`
	Category string `json:"category"`
	Packed   bool   `json:"packed"`
}

type Checklist struct {
	Name    string `json:"name"`
	Items   []Item `json:"items"`
	NextID  int    `json:"next_id"`
}

var templates = map[string]map[string][]string{
	"beach": {
		"Документы": {"Паспорт", "Билеты", "Страховка", "Виза"},
		"Одежда":    {"Купальник", "Шорты", "Футболка", "Панама", "Сандалии", "Пляжное полотенце"},
		"Гигиена":   {"Солнцезащитный крем", "Очки", "Шампунь", "Зубная щетка", "Гель для душа"},
		"Техника":   {"Телефон", "Зарядка", "Наушники", "Повербанк"},
		"Медицина":  {"Аптечка", "Средство от укусов", "Пластырь"},
	},
	"mountain": {
		"Документы":   {"Паспорт", "Страховка", "Карта", "Компас"},
		"Одежда":      {"Термобелье", "Флис", "Куртка", "Штаны", "Трекинговая обувь", "Носки", "Шапка", "Перчатки"},
		"Снаряжение":  {"Рюкзак", "Палатка", "Спальник", "Каремат", "Фонарик", "Трекинговые палки"},
		"Еда":         {"Горелка", "Топливо", "Посуда", "Еда в пакетах", "Вода", "Термос"},
		"Техника":     {"Телефон", "Зарядка", "Навигатор", "Радиостанция"},
	},
	"business": {
		"Документы": {"Паспорт", "Билеты", "Командировочное удостоверение", "Виза", "Страховка"},
		"Одежда":    {"Костюм", "Рубашки", "Галстук", "Туфли", "Носки", "Ремень"},
		"Техника":   {"Ноутбук", "Зарядка", "Презентация", "Флешка", "Телефон", "Адаптер"},
		"Гигиена":   {"Дезодорант", "Зубная щетка", "Паста", "Расческа", "Шампунь"},
		"Прочее":    {"Визитки", "Блокнот", "Ручка", "Зонт"},
	},
	"hiking": {
		"Документы":   {"Паспорт", "Карта", "Разрешение", "Страховка"},
		"Снаряжение":  {"Рюкзак", "Палатка", "Спальник", "Каремат", "Котелок", "Нож"},
		"Одежда":      {"Треккинговая обувь", "Носки", "Флис", "Куртка", "Штаны", "Дождевик", "Головной убор"},
		"Еда":         {"Еда в пакетах", "Сухой паек", "Вода", "Термос", "Горелка"},
		"Медицина":    {"Аптечка", "Эластичный бинт", "Пластырь", "Средство от насекомых"},
	},
	"cruise": {
		"Документы": {"Паспорт", "Виза", "Билеты", "Страховка", "Медицинская справка"},
		"Одежда":    {"Праздничный наряд", "Пляжная одежда", "Купальник", "Сандалии", "Туфли", "Пижама"},
		"Гигиена":   {"Солнцезащитный крем", "Шампунь", "Зубная щетка", "Дезодорант", "Москитная сетка"},
		"Техника":   {"Телефон", "Зарядка", "Фотоаппарат", "Наушники"},
		"Прочее":    {"Книга", "Ласты", "Маска для плавания", "Бинокль"},
	},
	"festival": {
		"Документы": {"Паспорт", "Билет на фестиваль", "Страховка"},
		"Одежда":    {"Яркая одежда", "Дождевик", "Удобная обувь", "Головной убор", "Солнцезащитные очки"},
		"Еда":       {"Вода", "Еда в пакетах", "Термос", "Пикник"},
		"Техника":   {"Телефон", "Зарядка", "Наушники", "Повербанк"},
		"Прочее":    {"Плед", "Стул", "Флаг", "Настольные игры"},
	},
}

func (c *Checklist) loadTemplate(name string) {
	tmpl, ok := templates[name]
	if !ok {
		return
	}
	c.Name = name
	c.Items = []Item{}
	c.NextID = 1
	for cat, items := range tmpl {
		for _, text := range items {
			c.Items = append(c.Items, Item{ID: c.NextID, Text: text, Category: cat, Packed: false})
			c.NextID++
		}
	}
}

func (c *Checklist) addItem(text, category string) {
	if category == "" {
		category = "Прочее"
	}
	item := Item{ID: c.NextID, Text: text, Category: category, Packed: false}
	c.Items = append(c.Items, item)
	c.NextID++
	fmt.Printf("\033[32mПункт добавлен (ID: %d)\033[0m\n", item.ID)
}

func (c *Checklist) removeItem(id int) {
	for i, item := range c.Items {
		if item.ID == id {
			c.Items = append(c.Items[:i], c.Items[i+1:]...)
			fmt.Printf("\033[33mПункт #%d удалён.\033[0m\n", id)
			return
		}
	}
	fmt.Printf("\033[31mПункт #%d не найден.\033[0m\n", id)
}

func (c *Checklist) packItem(id int) {
	for i := range c.Items {
		if c.Items[i].ID == id {
			c.Items[i].Packed = true
			fmt.Printf("\033[32mПункт #%d отмечен как собранный.\033[0m\n", id)
			return
		}
	}
	fmt.Printf("\033[31mПункт #%d не найден.\033[0m\n", id)
}

func (c *Checklist) unpackItem(id int) {
	for i := range c.Items {
		if c.Items[i].ID == id {
			c.Items[i].Packed = false
			fmt.Printf("\033[33mОтметка с пункта #%d снята.\033[0m\n", id)
			return
		}
	}
	fmt.Printf("\033[31mПункт #%d не найден.\033[0m\n", id)
}

func (c *Checklist) display() {
	if len(c.Items) == 0 {
		fmt.Println("\033[33mЧек-лист пуст.\033[0m")
		return
	}
	packed := 0
	for _, item := range c.Items {
		if item.Packed {
			packed++
		}
	}
	total := len(c.Items)
	pct := packed * 100 / total
	barLen := 20
	filled := pct * barLen / 100
	bar := strings.Repeat("█", filled) + strings.Repeat("░", barLen-filled)
	fmt.Printf("\033[36m📋 Чек-лист: %s\033[0m\n", c.Name)
	fmt.Printf("\033[32mПрогресс: %s %d%% (%d/%d)\033[0m\n\n", bar, pct, packed, total)

	categories := []string{}
	catMap := make(map[string]bool)
	for _, item := range c.Items {
		if !catMap[item.Category] {
			catMap[item.Category] = true
			categories = append(categories, item.Category)
		}
	}
	sort.Strings(categories)

	for _, cat := range categories {
		fmt.Printf("\033[33mКатегория: %s\033[0m\n", cat)
		for _, item := range c.Items {
			if item.Category == cat {
				status := "\033[32m✅\033[0m"
				if !item.Packed {
					status = "\033[31m❌\033[0m"
				}
				fmt.Printf("  %d. %s %s\n", item.ID, status, item.Text)
			}
		}
	}
}

func (c *Checklist) save(filename string) {
	data, _ := json.MarshalIndent(c, "", "  ")
	os.WriteFile(filename, data, 0644)
	fmt.Printf("\033[32mЧек-лист сохранён в %s\033[0m\n", filename)
}

func loadChecklist(filename string) *Checklist {
	data, err := os.ReadFile(filename)
	if err != nil {
		fmt.Printf("\033[31mОшибка загрузки: %v\033[0m\n", err)
		return nil
	}
	var c Checklist
	if err := json.Unmarshal(data, &c); err != nil {
		fmt.Printf("\033[31mОшибка парсинга: %v\033[0m\n", err)
		return nil
	}
	return &c
}

func (c *Checklist) exportCSV(filename string) {
	f, _ := os.Create(filename)
	defer f.Close()
	w := csv.NewWriter(f)
	defer w.Flush()
	w.Write([]string{"id", "category", "item", "packed"})
	for _, item := range c.Items {
		w.Write([]string{
			strconv.Itoa(item.ID),
			item.Category,
			item.Text,
			strconv.FormatBool(item.Packed),
		})
	}
	fmt.Printf("\033[32mЭкспортировано в %s (CSV)\033[0m\n", filename)
}

func (c *Checklist) exportTXT(filename string) {
	f, _ := os.Create(filename)
	defer f.Close()
	fmt.Fprintf(f, "Чек-лист: %s\n", c.Name)
	fmt.Fprintf(f, "Дата: %s\n\n", time.Now().Format("2006-01-02 15:04:05"))
	categories := []string{}
	catMap := make(map[string]bool)
	for _, item := range c.Items {
		if !catMap[item.Category] {
			catMap[item.Category] = true
			categories = append(categories, item.Category)
		}
	}
	sort.Strings(categories)
	for _, cat := range categories {
		fmt.Fprintf(f, "=== %s ===\n", cat)
		for _, item := range c.Items {
			if item.Category == cat {
				status := "[ ]"
				if item.Packed {
					status = "[x]"
				}
				fmt.Fprintf(f, "%s %s\n", status, item.Text)
			}
		}
		fmt.Fprintln(f)
	}
	fmt.Printf("\033[32mЭкспортировано в %s (TXT)\033[0m\n", filename)
}

func main() {
	var (
		template    string
		name        string
		add         string
		addCategory string
		pack        int
		unpack      int
		remove      int
		list        bool
		saveFile    string
		loadFile    string
		newFlag     bool
		exportCSV   string
		exportTXT   string
	)
	flag.StringVar(&template, "template", "", "Тип путешествия")
	flag.StringVar(&name, "name", "", "Название чек-листа")
	flag.StringVar(&add, "add", "", "Добавить пункт")
	flag.StringVar(&addCategory, "add-category", "", "Категория для добавляемого пункта")
	flag.IntVar(&pack, "pack", 0, "Отметить пункт как собранный")
	flag.IntVar(&unpack, "unpack", 0, "Снять отметку с пункта")
	flag.IntVar(&remove, "remove", 0, "Удалить пункт")
	flag.BoolVar(&list, "list", false, "Показать чек-лист")
	flag.StringVar(&saveFile, "save", "", "Сохранить чек-лист")
	flag.StringVar(&loadFile, "load", "", "Загрузить чек-лист")
	flag.BoolVar(&newFlag, "new", false, "Создать новый пустой чек-лист")
	flag.StringVar(&exportCSV, "export-csv", "", "Экспорт в CSV")
	flag.StringVar(&exportTXT, "export-txt", "", "Экспорт в TXT")
	flag.Parse()

	var c *Checklist
	if loadFile != "" {
		c = loadChecklist(loadFile)
		if c == nil {
			os.Exit(1)
		}
	} else if newFlag {
		c = &Checklist{Name: name, Items: []Item{}, NextID: 1}
	} else if template != "" {
		c = &Checklist{Name: name}
		c.loadTemplate(template)
	} else {
		// Попытка загрузить из файла по умолчанию
		if _, err := os.Stat(dataFile); err == nil {
			c = loadChecklist(dataFile)
		}
		if c == nil {
			c = &Checklist{Name: name, Items: []Item{}, NextID: 1}
		}
	}

	if add != "" {
		c.addItem(add, addCategory)
	}
	if pack != 0 {
		c.packItem(pack)
	}
	if unpack != 0 {
		c.unpackItem(unpack)
	}
	if remove != 0 {
		c.removeItem(remove)
	}
	if list {
		c.display()
	}
	if saveFile != "" {
		c.save(saveFile)
	}
	if exportCSV != "" {
		c.exportCSV(exportCSV)
	}
	if exportTXT != "" {
		c.exportTXT(exportTXT)
	}

	if loadFile == "" && !newFlag && template == "" && saveFile == "" && (add != "" || pack != 0 || unpack != 0 || remove != 0) {
		c.save(dataFile)
	}

	if flag.NArg() == 0 && len(os.Args) == 1 {
		fmt.Println("Используйте --help для справки.")
	}
}
