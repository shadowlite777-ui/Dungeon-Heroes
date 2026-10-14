package com.example.textrpg; // Замените на пакет вашего проекта, если он отличается

import java.io.Serializable;

public class Player implements Serializable {
    private String name;
    private String characterClass;
    private int level = 1;
    private int talentPoints = 1; // Дадим 1 очко сразу для теста

    // 4 основных навыка
    private int health = 100;
    private int strength = 10;
    private int intelligence = 10;
    private int stamina = 10;

    public Player(String name, String characterClass) {
        this.name = name;
        this.characterClass = characterClass;
    }

    // Геттеры и сеттеры
    public String getName() { return name; }
    public String getCharacterClass() { return characterClass; }
    
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    
    public int getTalentPoints() { return talentPoints; }
    public void setTalentPoints(int talentPoints) { this.talentPoints = talentPoints; }

    public int getHealth() { return health; }
    public void setHealth(int health) { this.health = health; }

    public int getStrength() { return strength; }
    public void setStrength(int strength) { this.strength = strength; }

    public int getIntelligence() { return intelligence; }
    public void setIntelligence(int intelligence) { this.intelligence = intelligence; }

    public int getStamina() { return stamina; }
    public void setStamina(int stamina) { this.stamina = stamina; }
}
