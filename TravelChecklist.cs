// TravelChecklist.cs
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text.Json;
using System.Text.Json.Serialization;

namespace TravelChecklist
{
    class Program
    {
        static void Main(string[] args)
        {
            var opts = ParseArgs(args);
            var checklist = new TravelChecklist();
            if (opts.Load != null)
            {
                checklist.Load(opts.Load);
            }
            else if (opts.New)
            {
                checklist.Data.Name = opts.Name ?? "Новый список";
                checklist.Data.Items.Clear();
                checklist.Data.NextId = 1;
            }
            else if (opts.Template != null)
            {
                checklist.LoadTemplate(opts.Template);
                if (opts.Name != null) checklist.Data.Name = opts.Name;
            }
            else
            {
                // Попытка загрузить из файла по умолчанию
                if (File.Exists("checklist.json"))
                {
                    checklist.Load("checklist.json");
                }
                else
                {
                    checklist.Data.Name = opts.Name ?? "Мой список";
                }
            }

            if (opts.Add != null)
            {
                checklist.AddItem(opts.Add, opts.AddCategory);
            }
            if (opts.Pack.HasValue) checklist.PackItem(opts.Pack.Value);
            if (opts.Unpack.HasValue) checklist.UnpackItem(opts.Unpack.Value);
            if (opts.Remove.HasValue) checklist.RemoveItem(opts.Remove.Value);
            if (opts.List) checklist.Display();
            if (opts.Save != null) checklist.Save(opts.Save);
            if (opts.ExportCsv != null) checklist.ExportCsv(opts.ExportCsv);
            if (opts.ExportTxt != null) checklist.ExportTxt(opts.ExportTxt);

            if (opts.Load == null && !opts.New && opts.Template == null && opts.Save == null &&
                (opts.Add != null || opts.Pack.HasValue || opts.Unpack.HasValue || opts.Remove.HasValue))
            {
                checklist.Save("checklist.json");
            }

            if (!opts.List && opts.Add == null && !opts.Pack.HasValue && !opts.Unpack.HasValue && !opts.Remove.HasValue &&
                opts.Save == null && opts.ExportCsv == null && opts.ExportTxt == null && opts.Load == null && !opts.New)
            {
                Console.WriteLine("Используйте --help для справки.");
            }
        }

        static Options ParseArgs(string[] args)
        {
            var opts = new Options();
            for (int i = 0; i < args.Length; i++)
            {
                switch (args[i])
                {
                    case "--template": opts.Template = args[++i]; break;
                    case "--name": opts.Name = args[++i]; break;
                    case "--add": opts.Add = args[++i]; break;
                    case "--add-category": opts.AddCategory = args[++i]; break;
                    case "--pack": opts.Pack = int.Parse(args[++i]); break;
                    case "--unpack": opts.Unpack = int.Parse(args[++i]); break;
                    case "--remove": opts.Remove = int.Parse(args[++i]); break;
                    case "--list": opts.List = true; break;
                    case "--save": opts.Save = args[++i]; break;
                    case "--load": opts.Load = args[++i]; break;
                    case "--new": opts.New = true; break;
                    case "--export-csv": opts.ExportCsv = args[++i]; break;
                    case "--export-txt": opts.ExportTxt = args[++i]; break;
                }
            }
            return opts;
        }

        class Options
        {
            public string Template { get; set; }
            public string Name { get; set; }
            public string Add { get; set; }
            public string AddCategory { get; set; }
            public int? Pack { get; set; }
            public int? Unpack { get; set; }
            public int? Remove { get; set; }
            public bool List { get; set; }
            public string Save { get; set; }
            public string Load { get; set; }
            public bool New { get; set; }
            public string ExportCsv { get; set; }
            public string ExportTxt { get; set; }
        }

        class Item
        {
            public int Id { get; set; }
            public string Text { get; set; }
            public string Category { get; set; }
            public bool Packed { get; set; }
        }

        class ChecklistData
        {
            public string Name { get; set; } = "Путешествие";
            public List<Item> Items { get; set; } = new List<Item>();
            public int NextId { get; set; } = 1;
        }

        class TravelChecklist
        {
            public ChecklistData Data { get; private set; } = new ChecklistData();

            private readonly Dictionary<string, Dictionary<string, List<string>>> templates = new();

            public TravelChecklist()
            {
                InitTemplates();
            }

            private void InitTemplates()
            {
                // Пляж
                templates["beach"] = new()
                {
                    ["Документы"] = new() { "Паспорт", "Билеты", "Страховка", "Виза" },
                    ["Одежда"] = new() { "Купальник", "Шорты", "Футболка", "Панама", "Сандалии", "Пляжное полотенце" },
                    ["Гигиена"] = new() { "Солнцезащитный крем", "Очки", "Шампунь", "Зубная щетка", "Гель для душа" },
                    ["Техника"] = new() { "Телефон", "Зарядка", "Наушники", "Повербанк" },
                    ["Медицина"] = new() { "Аптечка", "Средство от укусов", "Пластырь" }
                };
                // ... остальные шаблоны аналогично
            }

            public void LoadTemplate(string templateName)
            {
                if (!templates.ContainsKey(templateName)) return;
                var tmpl = templates[templateName];
                Data = new ChecklistData();
                Data.Name = templateName switch
                {
                    "beach" => "Пляжный отдых",
                    "mountain" => "Горный поход",
                    "business" => "Командировка",
                    "hiking" => "Пеший поход",
                    "cruise" => "Круиз",
                    "festival" => "Фестиваль",
                    _ => "Путешествие"
                };
                Data.NextId = 1;
                foreach (var kv in tmpl)
                {
                    foreach (var text in kv.Value)
                    {
                        Data.Items.Add(new Item { Id = Data.NextId++, Text = text, Category = kv.Key });
                    }
                }
            }

            public void Load(string filename)
            {
                try
                {
                    string json = File.ReadAllText(filename);
                    Data = JsonSerializer.Deserialize<ChecklistData>(json) ?? new ChecklistData();
                }
                catch
                {
                    Data = new ChecklistData();
                }
            }

            public void Save(string filename)
            {
                string json = JsonSerializer.Serialize(Data, new JsonSerializerOptions { WriteIndented = true });
                File.WriteAllText(filename, json);
                Console.ForegroundColor = ConsoleColor.Green;
                Console.WriteLine($"Чек-лист сохранён в {filename}");
                Console.ResetColor();
            }

            public void AddItem(string text, string category)
            {
                if (string.IsNullOrEmpty(category)) category = "Прочее";
                Data.Items.Add(new Item { Id = Data.NextId++, Text = text, Category = category });
                Console.ForegroundColor = ConsoleColor.Green;
                Console.WriteLine($"Пункт добавлен (ID: {Data.NextId - 1})");
                Console.ResetColor();
            }

            public void RemoveItem(int id)
            {
                int idx = Data.Items.FindIndex(i => i.Id == id);
                if (idx == -1)
                {
                    Console.ForegroundColor = ConsoleColor.Red;
                    Console.WriteLine($"Пункт #{id} не найден.");
                    Console.ResetColor();
                    return;
                }
                Data.Items.RemoveAt(idx);
                Console.ForegroundColor = ConsoleColor.Yellow;
                Console.WriteLine($"Пункт #{id} удалён.");
                Console.ResetColor();
            }

            public void PackItem(int id)
            {
                var item = Data.Items.Find(i => i.Id == id);
                if (item == null)
                {
                    Console.ForegroundColor = ConsoleColor.Red;
                    Console.WriteLine($"Пункт #{id} не найден.");
                    Console.ResetColor();
                    return;
                }
                item.Packed = true;
                Console.ForegroundColor = ConsoleColor.Green;
                Console.WriteLine($"Пункт #{id} отмечен как собранный.");
                Console.ResetColor();
            }

            public void UnpackItem(int id)
            {
                var item = Data.Items.Find(i => i.Id == id);
                if (item == null)
                {
                    Console.ForegroundColor = ConsoleColor.Red;
                    Console.WriteLine($"Пункт #{id} не найден.");
                    Console.ResetColor();
                    return;
                }
                item.Packed = false;
                Console.ForegroundColor = ConsoleColor.Yellow;
                Console.WriteLine($"Отметка с пункта #{id} снята.");
                Console.ResetColor();
            }

            public void Display()
            {
                if (Data.Items.Count == 0)
                {
                    Console.ForegroundColor = ConsoleColor.Yellow;
                    Console.WriteLine("Чек-лист пуст.");
                    Console.ResetColor();
                    return;
                }
                int total = Data.Items.Count;
                int packed = Data.Items.Count(i => i.Packed);
                int pct = total > 0 ? packed * 100 / total : 0;
                int barLen = 20;
                int filled = pct * barLen / 100;
                string bar = new string('█', filled) + new string('░', barLen - filled);
                Console.ForegroundColor = ConsoleColor.Cyan;
                Console.WriteLine($"📋 Чек-лист: {Data.Name}");
                Console.ForegroundColor = ConsoleColor.Green;
                Console.WriteLine($"Прогресс: {bar} {pct}% ({packed}/{total})");
                Console.ResetColor();
                Console.WriteLine();

                var categories = Data.Items.Select(i => i.Category).Distinct().OrderBy(c => c);
                foreach (var cat in categories)
                {
                    Console.ForegroundColor = ConsoleColor.Yellow;
                    Console.WriteLine($"Категория: {cat}");
                    Console.ResetColor();
                    foreach (var item in Data.Items.Where(i => i.Category == cat))
                    {
                        string status = item.Packed ? "\u001B[32m✅\u001B[0m" : "\u001B[31m❌\u001B[0m";
                        Console.WriteLine($"  {item.Id}. {status} {item.Text}");
                    }
                }
            }

            public void ExportCsv(string filename)
            {
                using var sw = new StreamWriter(filename);
                sw.WriteLine("id,category,item,packed");
                foreach (var item in Data.Items)
                    sw.WriteLine($"{item.Id},{item.Category},{item.Text},{item.Packed}");
                Console.ForegroundColor = ConsoleColor.Green;
                Console.WriteLine($"Экспортировано в {filename} (CSV)");
                Console.ResetColor();
            }

            public void ExportTxt(string filename)
            {
                using var sw = new StreamWriter(filename);
                sw.WriteLine($"Чек-лист: {Data.Name}");
                sw.WriteLine($"Дата: {DateTime.Now}");
                sw.WriteLine();
                var categories = Data.Items.Select(i => i.Category).Distinct().OrderBy(c => c);
                foreach (var cat in categories)
                {
                    sw.WriteLine($"=== {cat} ===");
                    foreach (var item in Data.Items.Where(i => i.Category == cat))
                    {
                        sw.WriteLine($"{(item.Packed ? "[x]" : "[ ]")} {item.Text}");
                    }
                    sw.WriteLine();
                }
                Console.ForegroundColor = ConsoleColor.Green;
                Console.WriteLine($"Экспортировано в {filename} (TXT)");
                Console.ResetColor();
            }
        }
    }
}
