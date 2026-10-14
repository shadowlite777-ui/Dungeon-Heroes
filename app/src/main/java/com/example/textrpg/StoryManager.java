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
        StoryNode start = new StoryNode("start", 
            "Вы приходите в себя в полуразрушенной каменной келье. Воздух пахнет сыростью и старыми фолиантами. Перед вами две массивные двери: дубовая, закованная в железо, и старая деревянная, из-под которой пробивается слабый свет.");
        start.addOption("Открыть тяжелую дубовую дверь", "oak_door");
        start.addOption("Осмотреть старую деревянную дверь", "wooden_door");
        start.addOption("Обыскать келью", "search_cell");
        nodes.put(start.getId(), start);

        StoryNode oakDoor = new StoryNode("oak_door", 
            "С трудом толкнув тяжелую дверь, вы оказываетесь в мрачном коридоре. Вдали слышен звон капель и глухое рычание. Вы видите следы крови на полу.");
        oakDoor.addOption("Осторожно продвигаться вперед на звук", "corridor_forward");
        oakDoor.addOption("Вернуться в келью", "start");
        nodes.put(oakDoor.getId(), oakDoor);

        StoryNode woodenDoor = new StoryNode("wooden_door", 
            "Толкнув заклинившую дверь, вы попадаете в заброшенную библиотеку. Шкафы обрушились, но на одном из столов лежит таинственный свиток, светящийся синеватым светом.");
        woodenDoor.addOption("Взять светящийся свиток", "take_scroll");
        woodenDoor.addOption("Игнорировать свиток и пройти в соседний зал", "library_hall");
        woodenDoor.addOption("Вернуться в келью", "start");
        nodes.put(woodenDoor.getId(), woodenDoor);

        StoryNode searchCell = new StoryNode("search_cell", 
            "В углу под грудой соломы вы находите старый потертый кинжал и флягу с водой. Взяв находки, вы вновь поворачиваетесь к дверям.");
        searchCell.addOption("Подойти к дубовой двери", "oak_door");
        searchCell.addOption("Подойти к деревянной двери", "wooden_door");
        nodes.put(searchCell.getId(), searchCell);

        // Этот узел запускает боевую систему в MainActivity
        StoryNode corridorForward = new StoryNode("corridor_forward", 
            "Продвигаясь по коридору, вы сталкиваетесь с монстром!");
        corridorForward.addOption("Начать заново", "start");
        nodes.put(corridorForward.getId(), corridorForward);

        StoryNode takeScroll = new StoryNode("take_scroll", 
            "Как только ваши пальцы касаются свитка, магическая энергия наполняет ваше тело! Вы чувствуете прилив сил.");
        takeScroll.addOption("Вернуться к дверям", "start");
        nodes.put(takeScroll.getId(), takeScroll);

        StoryNode libraryHall = new StoryNode("library_hall", 
            "Вы выходите в просторный зал библиотеки и видите запертый сундук с древними рунами.");
        libraryHall.addOption("Вернуться в келью", "start");
        nodes.put(libraryHall.getId(), libraryHall);

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
