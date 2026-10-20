package com.example.textrpg;

import java.util.Random;

public class RouteManager {
    
    public enum EventType {
        MONSTER,
        CHEST,
        SHRINE,
        TRAP
    }

    private int dungeonDepth = 0;

    public RouteManager() {
        dungeonDepth = 1;
    }

    public int getDungeonDepth() {
        return dungeonDepth;
    }

    public void advanceDepth() {
        dungeonDepth++;
    }

    // Генерация случайного события при выборе дороги
    public EventResult generateEvent(String direction, int heroLevel) {
        Random random = new Random();
        int roll = random.nextInt(100);

        EventResult result = new EventResult();
        result.direction = direction;

        if (roll < 60) {
            // 60% шанс на монстра
            result.type = EventType.MONSTER;
            result.monster = Monster.generateMonster(heroLevel);
            result.description = "🚶 Вы свернули **" + getDirectionText(direction) + "** и прошли вглубь туннеля (Глубина: " + dungeonDepth + ").\n\n⚠️ Из темноты на вас выскакивает враг!";
        } else if (roll < 80) {
            // 20% шанс на сундук
            result.type = EventType.CHEST;
            result.description = "🚶 Вы пошли **" + getDirectionText(direction) + "** (Глубина: " + dungeonDepth + ").\n\n chests ✨ Вы обнаружили старинный заброшенный сундук, полный припасов!";
        } else if (roll < 90) {
            // 10% шанс на святыню
            result.type = EventType.SHRINE;
            result.description = "🚶 Вы выбрали путь **" + getDirectionText(direction) + "** (Глубина: " + dungeonDepth + ").\n\n ⛪ Перед вами мерцает древняя магическая святыня, источающая целительную ауру.";
        } else {
            // 10% шанс на ловушку / пустой коридор
            result.type = EventType.TRAP;
            result.description = "🚶 Вы прошли **" + getDirectionText(direction) + "** (Глубина: " + dungeonDepth + ").\n\n 🕸️ Путь оказался пустым, но воздух здесь насквозь пропитан смертельным холодом.";
        }

        return result;
    }

    private String getDirectionText(String dir) {
        switch (dir) {
            case "LEFT": return "налево";
            case "RIGHT": return "направо";
            default: return "прямо";
        }
    }

    public static class EventResult {
        public String direction;
        public EventType type;
        public String description;
        public Monster monster;
    }
}
