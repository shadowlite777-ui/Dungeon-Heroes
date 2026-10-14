package com.example.textrpg;

import java.util.Random;

public class Monster {
    private String name;
    private int level;
    private int maxHp;
    private int currentHp;
    private int attackPower;

    public Monster(String name, int level, int maxHp, int attackPower) {
        this.name = name;
        this.level = level;
        this.maxHp = maxHp;
        this.currentHp = maxHp;
        this.attackPower = attackPower;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public void setCurrentHp(int currentHp) {
        this.currentHp = Math.max(0, currentHp);
    }

    public int getAttackPower() {
        return attackPower;
    }

    public static Monster generateMonster(int heroLevel) {
        Random random = new Random();
        // Уровень монстра: от -1 до +7 от уровня героя (минимум 1-й уровень)
        int minLevel = Math.max(1, heroLevel - 1);
        int maxLevel = heroLevel + 7;
        int level = minLevel + random.nextInt(maxLevel - minLevel + 1);

        String[] names = {"Теневой гоблин", "Пещерный паук", "Скелет-воин", "Орк-разбойник", "Дикий ворг", "Болотная кикимора"};
        String name = names[random.nextInt(names.length)] + " [Ур. " + level + "]";

        int maxHp = 40 + level * 15;
        int attackPower = 8 + level * 3;

        return new Monster(name, level, maxHp, attackPower);
    }
}
