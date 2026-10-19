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
        int minLevel = Math.max(1, heroLevel - 1);
        int maxLevel = heroLevel + 3;
        int level = minLevel + random.nextInt(maxLevel - minLevel + 1);

        String[] names = {"Теневой гоблин", "Пещерный паук", "Скелет-воин", "Орк-разбойник", "Дикий ворг", "Болотная кикимора"};
        String name = names[random.nextInt(names.length)] + " [Ур. " + level + "]";

        int maxHp = 25 + level * 10;
        // Сила атаки монстра снижена ещё на 20% (было 5 + level*2)
        int attackPower = (int) Math.max(2, (4 + level * 1.6));

        return new Monster(name, level, maxHp, attackPower);
    }

    public int calculateExpReward(int heroLevel) {
        int baseExp = 35;
        int diff = this.level - heroLevel;
        double multiplier = 1.0 + (diff * 0.15);
        // Прирост опыта увеличен на 10%
        return (int) Math.max(10, baseExp * multiplier * 1.10);
    }
}
