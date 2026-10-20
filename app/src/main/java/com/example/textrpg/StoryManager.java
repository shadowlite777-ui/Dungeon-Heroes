package com.example.textrpg;

import java.util.HashMap;
import java.util.Map;

public class StoryManager {
    private Map<String, StoryNode> nodes = new HashMap<>();
    private String currentNodeId;

    public StoryManager() {
        initStory();
    }

    private void initStory() {
        // Локация 1: Старт
        StoryNode start = new StoryNode("start", 
            "⚔️ **ПРОБУЖДЕНИЕ В КЕЛЬЕ**\n\nВы приходите в себя в древней каменной келье. Воздух пропитан магией и сыростью. Вы видите две тяжелые двери и заваленный алтарь.");
        start.addOption("🚪 Войти в массивную дубовую дверь", "oak_door");
        start.addOption("🚪 Осмотреть хлипкую деревянную дверь", "wooden_door");
        start.addOption("🔍 Обыскать разрушенный алтарь", "search_cell");
        nodes.put(start.getId(), start);

        // Локация 2: Заброшенный алтарь
        StoryNode searchCell = new StoryNode("search_cell", 
            "✨ **ДРЕВНИЙ АЛТАРЬ**\n\nСреди обломков вы находите горящие угли и затерянный сундучок. Здесь можно перевести дух или исследовать тайник.");
        searchCell.addOption("⚔️ Исследовать мрачный коридор за алтарём", "corridor_forward");
        searchCell.addOption("🚪 Вернуться к главным дверям", "start");
        nodes.put(searchCell.getId(), searchCell);

        // Локация 3: Коридор испытаний (Бой)
        StoryNode oakDoor = new StoryNode("oak_door", 
            "🕯️ **МРАЧНЫЙ КОРИДОР**\n\nТяжелая дверь со скрипом поддается. Впереди слышится рычание и эхо шагов. Пути назад нет — впереди враг!");
        oakDoor.addOption("⚔️ Сделать шаг вперед и вступить в бой", "corridor_forward");
        nodes.put(oakDoor.getId(), oakDoor);

        StoryNode woodenDoor = new StoryNode("wooden_door", 
            "📜 **ЗАБРОШЕННАЯ БИБЛИОТЕКА**\n\nВы оказываетесь среди старинных стеллажей. На полу видна свежая цепочка следов монстра.");
        woodenDoor.addOption("⚔️ Идти по следам чудовища", "corridor_forward");
        woodenDoor.addOption("🏰 Спуститься в катакомбы", "catacombs");
        nodes.put(woodenDoor.getId(), woodenDoor);

        // Локация 4: Сюжетное продвижение
        StoryNode corridorForward = new StoryNode("corridor_forward", "Боевое столкновение!");
        nodes.put(corridorForward.getId(), corridorForward);

        StoryNode catacombs = new StoryNode("catacombs", 
            "💀 **ПОДЗЕМНЫЕ КАТАКОМБЫ**\n\nВы спускаетесь всё глубже под землю. Воздух становится холоднее, а стены покрыты светящимся мхом. Впереди виден вход в Тронный Зал.");
        catacombs.addOption("⚔️ Исследовать тёмный туннель", "corridor_forward");
        catacombs.addOption("👑 Шагнуть к Тронному Залу", "throne_room");
        nodes.put(catacombs.getId(), catacombs);

        StoryNode throne_room = new StoryNode("throne_room", 
            "👑 **ТРОННЫЙ ЗАЛ ДРЕВНИХ**\n\nВы выходите в гигантский зал с высокой колоннадой. На трон восседает тень древнего стража. Приготовьтесь к серьезному испытанию!");
        throne_room.addOption("⚔️ Принять вызов Стража Трона", "corridor_forward");
        throne_room.addOption("🚪 Вернуться в Катакомбы", "catacombs");
        nodes.put(throne_room.getId(), throne_room);

        currentNodeId = "start";
    }

    public StoryNode getCurrentNode() {
        return nodes.get(currentNodeId);
    }

    public void selectOption(String targetNodeId) {
        if (nodes.containsKey(targetNodeId)) {
            currentNodeId = targetNodeId;
        }
    }
}
