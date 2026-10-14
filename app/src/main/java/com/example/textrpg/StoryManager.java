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
        // Начальная сцена
        StoryNode start = new StoryNode("start", 
            "Вы приходите в себя в полуразрушенной каменной келье. Воздух пахнет сыростью и старыми фолиантами. Перед вами две массивные двери: дубовая, закованная в железо, и старая деревянная, из-под которой пробивается слабый свет.");
        start.addOption("Открыть тяжелую дубовую дверь", "oak_door");
        start.addOption("Осмотреть старую деревянную дверь", "wooden_door");
        start.addOption("Обыскать келью", "search_cell");
        nodes.put(start.getId(), start);

        // Дубовая дверь
        StoryNode oakDoor = new StoryNode("oak_door", 
            "С трудом толкнув тяжелую дверь, вы оказываетесь в мрачном коридоре. Вдали слышен звон капель и глухое рычание. Вы видите следы крови на полу.");
        oakDoor.addOption("Осторожно продвигаться вперед на звук", "corridor_forward");
        oakDoor.addOption("Вернуться в келью", "start");
        nodes.put(oakDoor.getId(), oakDoor);

        // Деревянная дверь
        StoryNode woodenDoor = new StoryNode("wooden_door", 
            "Толкнув заклинившую дверь, вы попадаете в заброшенную библиотеку. Шкафы обрушились, но на одном из столов лежит таинственный свитoк, светящийся синеватым светом.");
        woodenDoor.addOption("Взять светящийся свиток", "take_scroll");
        woodenDoor.addOption("Игнорировать свиток и пройти в соседний зал", "library_hall");
        woodenDoor.addOption("Вернуться в келью", "start");
        nodes.put(woodenDoor.getId(), woodenDoor);

        // Обыск кельи
        StoryNode searchCell = new StoryNode("search_cell", 
            "В углу под грудой соломы вы находите старый потертый кинжал и флягу с водой. Взяв находки, вы вновь поворачиваетесь к дверям.");
        searchCell.addOption("Подойти к дубовой двери", "oak_door");
        searchCell.addOption("Подойти к деревянной двери", "wooden_door");
        nodes.put(searchCell.getId(), searchCell);

        // Развилки дальше
        StoryNode corridorForward = new StoryNode("corridor_forward", 
            "Продвигаясь по коридору, вы сталкиваетесь с теневым гоблином! Он зарычал и занес над головой ржавый топор. Начинается первый бой! (Продолжение следует...)");
        corridorForward.addOption("Начать заново", "start");
        nodes.put(corridorForward.getId(), corridorForward);

        StoryNode takeScroll = new StoryNode("take_scroll", 
            "Как только ваши пальцы касаются свитка, магическая энергия наполняет ваше тело! Вы чувствуете прилив сил. Перед вами открывается секретный проход.");
        takeScroll.addOption("Войти в секретный проход", "start");
        nodes.put(takeScroll.getId(), takeScroll);

        StoryNode libraryHall = new StoryNode("library_hall", 
            "Вы выходите в просторный зал библиотеки и видите запертый сундук с древними рунами.");
        libraryHall.addOption("Вернуться к свитку", "wooden_door");
        libraryHall.addOption("Вернуться в келью", "start");
        nodes.put(libraryHall.getId(), libraryHall);

        // Устанавливаем стартовый узел
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
