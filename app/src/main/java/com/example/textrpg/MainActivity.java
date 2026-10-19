package com.example.textrpg;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private View layoutMainMenu, layoutCharCreation, layoutGameMain;
    private EditText etCharName;
    private RadioButton rbWarrior, rbMage, rbRogue;
    private TextView tvHeroInfo, tvHeroLevel, tvGameLog;
    private LinearLayout layoutOptionsContainer;

    private StoryManager storyManager;

    // Характеристики персонажа
    private String heroName = "";
    private String heroClass = "";
    private int heroLevel = 1;
    private int currentExp = 0;
    private int requiredExp = 100;
    private int skillPoints = 3;

    private int statHealth = 100;
    private int currentHealth = 100;
    private int statStrength = 10;
    private int statIntelligence = 10;
    private int statStamina = 50;
    private int statDodge = 0;

    // Инвентарь зелий
    private int countSmallPotion = 0;
    private int countMediumPotion = 0;
    private int countLargePotion = 0;
    private int countDodgeElixir = 0;
    private int countArmorElixir = 0;

    // Баффы в бою
    private int buffDodgeTurns = 0;
    private int buffArmorTurns = 0;

    // Состояние боя
    private boolean isInCombat = false;
    private Monster activeMonster;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        storyManager = new StoryManager();

        layoutMainMenu = findViewById(R.id.layoutMainMenu);
        layoutCharCreation = findViewById(R.id.layoutCharCreation);
        layoutGameMain = findViewById(R.id.layoutGameMain);

        Button btnStartMenu = findViewById(R.id.btnStartMenu);
        etCharName = findViewById(R.id.etCharName);
        rbWarrior = findViewById(R.id.rbWarrior);
        rbMage = findViewById(R.id.rbMage);
        rbRogue = findViewById(R.id.rbRogue);
        Button btnCreateChar = findViewById(R.id.btnCreateChar);

        tvHeroInfo = findViewById(R.id.tvHeroInfo);
        tvHeroLevel = findViewById(R.id.tvHeroLevel);
        tvGameLog = findViewById(R.id.tvGameLog);
        layoutOptionsContainer = findViewById(R.id.layoutOptionsContainer);
        Button btnOpenTalents = findViewById(R.id.btnOpenTalents);

        btnStartMenu.setOnClickListener(v -> {
            layoutMainMenu.setVisibility(View.GONE);
            layoutCharCreation.setVisibility(View.VISIBLE);
        });

        btnCreateChar.setOnClickListener(v -> {
            String name = etCharName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Пожалуйста, введите имя героя!", Toast.LENGTH_SHORT).show();
                return;
            }
            heroName = name;
            initHeroStats();

            updateHeroUI();
            renderCurrentStoryNode();

            layoutCharCreation.setVisibility(View.GONE);
            layoutGameMain.setVisibility(View.VISIBLE);
        });

        btnOpenTalents.setOnClickListener(v -> showTalentsDialog());
    }

    // Первоначальная инициализация или полное сбрасывание параметров при смерти
    private void initHeroStats() {
        heroLevel = 1;
        currentExp = 0;
        requiredExp = 100;
        skillPoints = 3;

        statHealth = 100;
        currentHealth = 100;
        statStrength = 10;
        statIntelligence = 10;
        statStamina = 50;

        if (rbWarrior.isChecked()) {
            heroClass = "Воин";
            statDodge = 10;
        } else if (rbMage.isChecked()) {
            heroClass = "Маг";
            statDodge = 0;
        } else if (rbRogue.isChecked()) {
            heroClass = "Разбойник";
            statDodge = 25;
        }

        countSmallPotion = 0;
        countMediumPotion = 0;
        countLargePotion = 0;
        countDodgeElixir = 0;
        countArmorElixir = 0;
    }

    private void updateHeroUI() {
        tvHeroInfo.setText(heroName + " (" + heroClass + ")");
        tvHeroLevel.setText("Уровень: " + heroLevel + " [" + currentExp + "/" + requiredExp + " EXP]");
    }

    private void addExperience(int amount) {
        currentExp += amount;
        boolean leveledUp = false;
        while (currentExp >= requiredExp) {
            currentExp -= requiredExp;
            heroLevel++;
            skillPoints++;
            requiredExp = (int) (requiredExp * 1.60);
            leveledUp = true;
        }
        if (leveledUp) {
            Toast.makeText(this, "Поздравляем! Достигнут " + heroLevel + " уровень! Получено 1 очко таланта.", Toast.LENGTH_LONG).show();
        }
        updateHeroUI();
    }

    private double getEffectiveArmor() {
        double baseArmor = 100.0 + (heroLevel - 1) * 10.0;
        if (buffArmorTurns > 0) {
            baseArmor *= 1.20;
        }
        return baseArmor;
    }

    private void renderCurrentStoryNode() {
        StoryNode node = storyManager.getCurrentNode();

        if (node.getId().equals("corridor_forward") && !isInCombat) {
            startCombat();
            return;
        }

        tvGameLog.setText(node.getText());
        layoutOptionsContainer.removeAllViews();

        for (StoryNode.Option option : node.getOptions()) {
            Button btnOption = new Button(this);
            btnOption.setText(option.getText());
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 12);
            btnOption.setLayoutParams(params);

            btnOption.setOnClickListener(v -> {
                storyManager.selectOption(option.getTargetNodeId());
                renderCurrentStoryNode();
            });

            layoutOptionsContainer.addView(btnOption);
        }
    }

    private void startCombat() {
        isInCombat = true;
        buffDodgeTurns = 0;
        buffArmorTurns = 0;
        activeMonster = Monster.generateMonster(heroLevel);
        updateCombatScreen("На вашем пути встал противник!");
    }

    private void updateCombatScreen(String message) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== БОЙ С МОНСТРОМ ===\n");
        sb.append("Враг: ").append(activeMonster.getName()).append("\n");
        sb.append("HP Врага: ").append(activeMonster.getCurrentHp()).append("/").append(activeMonster.getMaxHp()).append("\n");
        sb.append("Ваше здоровье: ").append(currentHealth).append("/").append(statHealth).append("\n");
        
        int currentDodge = statDodge + (buffDodgeTurns > 0 ? 10 : 0);
        sb.append("Броня: ").append((int)getEffectiveArmor()).append(" | Уклонение: ").append(currentDodge).append("%\n\n");
        sb.append(message);

        tvGameLog.setText(sb.toString());
        layoutOptionsContainer.removeAllViews();

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 10);

        // 1. Атака
        Button btnAttack = new Button(this);
        btnAttack.setText("Атака (Сила: " + statStrength + ")");
        btnAttack.setLayoutParams(params);
        btnAttack.setOnClickListener(v -> performHeroAttack());
        layoutOptionsContainer.addView(btnAttack);

        // 2. Использование предметов (без завершения хода и атаки монстра!)
        if (countSmallPotion > 0) {
            Button btn = new Button(this);
            btn.setText("Малое зелье HP (" + countSmallPotion + " шт) [+5%]");
            btn.setLayoutParams(params);
            btn.setOnClickListener(v -> usePotionWithoutTurnLoss(0.05, 1, 0, 0, "Вы выпили малое зелье здоровья."));
            layoutOptionsContainer.addView(btn);
        }
        if (countMediumPotion > 0) {
            Button btn = new Button(this);
            btn.setText("Среднее зелье HP (" + countMediumPotion + " шт) [+10%]");
            btn.setLayoutParams(params);
            btn.setOnClickListener(v -> usePotionWithoutTurnLoss(0.10, 0, 1, 0, "Вы выпили среднее зелье здоровья."));
            layoutOptionsContainer.addView(btn);
        }
        if (countLargePotion > 0) {
            Button btn = new Button(this);
            btn.setText("Большое зелье HP (" + countLargePotion + " шт) [+20%]");
            btn.setLayoutParams(params);
            btn.setOnClickListener(v -> usePotionWithoutTurnLoss(0.20, 0, 0, 1, "Вы выпили большое зелье здоровья."));
            layoutOptionsContainer.addView(btn);
        }
        if (countDodgeElixir > 0 && buffDodgeTurns == 0) {
            Button btn = new Button(this);
            btn.setText("Эликсир уклонения (" + countDodgeElixir + " шт) [+10% Dodge]");
            btn.setLayoutParams(params);
            btn.setOnClickListener(v -> {
                countDodgeElixir--;
                buffDodgeTurns = 3;
                updateCombatScreen("Вы выпили эликсир уклонения (+10% Dodge на 3 хода)!\nВаш ход продолжается.");
            });
            layoutOptionsContainer.addView(btn);
        }
        if (countArmorElixir > 0 && buffArmorTurns == 0) {
            Button btn = new Button(this);
            btn.setText("Эликсир брони (" + countArmorElixir + " шт) [+20% Armor]");
            btn.setLayoutParams(params);
            btn.setOnClickListener(v -> {
                countArmorElixir--;
                buffArmorTurns = 3;
                updateCombatScreen("Вы выпили эликсир брони (+20% Брони на 3 хода)!\nВаш ход продолжается.");
            });
            layoutOptionsContainer.addView(btn);
        }

        // 3. Побег из боя
        boolean canFleeLevel = activeMonster.getLevel() >= heroLevel + 3;
        boolean canFleeHealth = ((double) currentHealth / statHealth) < 0.35;

        if (canFleeLevel || canFleeHealth) {
            Button btnFlee = new Button(this);
            btnFlee.setText("Сбежать из боя");
            btnFlee.setLayoutParams(params);
            btnFlee.setOnClickListener(v -> fleeCombat());
            layoutOptionsContainer.addView(btnFlee);
        }
    }

    // Выпиваем зелье HP, не отдавая ход монстру
    private void usePotionWithoutTurnLoss(double percent, int small, int medium, int large, String msg) {
        countSmallPotion -= small;
        countMediumPotion -= medium;
        countLargePotion -= large;

        int heal = (int) (statHealth * percent);
        currentHealth = Math.min(statHealth, currentHealth + heal);

        updateCombatScreen(msg + " Восстановлено +" + heal + " HP.\nВаш ход продолжается.");
    }

    private void performHeroAttack() {
        Random random = new Random();
        double baseDamage = statStrength;
        int levelDiff = activeMonster.getLevel() - heroLevel;

        if (activeMonster.getLevel() < heroLevel) {
            baseDamage *= 1.10;
        } else if (levelDiff > 0) {
            int pairs = levelDiff / 2;
            double reduction = pairs * 0.05;
            baseDamage *= Math.max(0.1, (1.0 - reduction));
        }

        double spread = 0.8 + (random.nextDouble() * 0.4);
        int heroDamage = (int) Math.max(1, baseDamage * spread);

        activeMonster.setCurrentHp(activeMonster.getCurrentHp() - heroDamage);

        if (activeMonster.getCurrentHp() <= 0) {
            isInCombat = false;
            int expReward = activeMonster.calculateExpReward(heroLevel);
            String lootMsg = generateLoot();

            StringBuilder victoryMsg = new StringBuilder();
            victoryMsg.append("Вы нанесли ").append(heroDamage).append(" урона.\n\n");
            victoryMsg.append("Вы победили монстра ").append(activeMonster.getName()).append("!\n");
            victoryMsg.append("Получено опыта: +").append(expReward).append(" EXP\n");
            if (!lootMsg.isEmpty()) {
                victoryMsg.append("Выпавшая добыча:\n").append(lootMsg);
            }

            addExperience(expReward);

            tvGameLog.setText(victoryMsg.toString());
            layoutOptionsContainer.removeAllViews();

            Button btnContinue = new Button(this);
            btnContinue.setText("Продолжить путь");
            btnContinue.setOnClickListener(v -> {
                storyManager.selectOption("start");
                renderCurrentStoryNode();
            });
            layoutOptionsContainer.addView(btnContinue);
            return;
        }

        performMonsterAttack(heroDamage, "Вы нанесли " + heroDamage + " урона.\n");
    }

    private String generateLoot() {
        Random random = new Random();
        StringBuilder loot = new StringBuilder();

        if (random.nextDouble() <= 0.70) {
            countSmallPotion++;
            loot.append("- Малое зелье HP\n");
        }
        if (random.nextDouble() <= 0.40) {
            countMediumPotion++;
            loot.append("- Среднее зелье HP\n");
        }
        if (random.nextDouble() <= 0.05) {
            countLargePotion++;
            loot.append("- Большое зелье HP\n");
        }
        if (random.nextDouble() <= 0.25) {
            countDodgeElixir++;
            loot.append("- Эликсир уклонения\n");
        }
        if (random.nextDouble() <= 0.25) {
            countArmorElixir++;
            loot.append("- Эликсир брони\n");
        }

        return loot.toString();
    }

    private void performMonsterAttack(int heroLastDamage, String prefixMessage) {
        Random random = new Random();

        if (buffDodgeTurns > 0) buffDodgeTurns--;
        if (buffArmorTurns > 0) buffArmorTurns--;

        int effectiveDodge = statDodge + (buffDodgeTurns > 0 ? 10 : 0);
        if (random.nextInt(100) < effectiveDodge) {
            updateCombatScreen(prefixMessage + activeMonster.getName() + " атакует, но вы успешно УКЛОНИЛИСЬ!");
            return;
        }

        double monsterBaseDmg = activeMonster.getAttackPower();
        int levelDiff = activeMonster.getLevel() - heroLevel;

        if (activeMonster.getLevel() > heroLevel) {
            monsterBaseDmg *= (1.0 + (levelDiff * 0.05));
        } else if (activeMonster.getLevel() < heroLevel) {
            monsterBaseDmg *= (1.0 - (Math.abs(levelDiff) * 0.05));
        }

        double spread = 0.8 + (random.nextDouble() * 0.4);
        double rawDamage = monsterBaseDmg * spread;

        double currentArmor = getEffectiveArmor();
        double efficiency = 1.0 / (1.0 + (heroLevel - 1) * 0.15);
        double armorAbsorption = (currentArmor / 100.0) * 0.15 * efficiency;
        armorAbsorption = Math.min(0.75, armorAbsorption);

        int finalMonsterDamage = (int) Math.max(1, rawDamage * (1.0 - armorAbsorption));

        currentHealth -= finalMonsterDamage;

        String resultMsg = prefixMessage + activeMonster.getName() + " атакует и наносит вам " + finalMonsterDamage + " урона (Поглощено броней: " + (int)(armorAbsorption * 100) + "%)!";

        if (currentHealth <= 0) {
            currentHealth = 0;
            isInCombat = false;
            tvGameLog.setText(resultMsg + "\n\nВЫ ПОГИБЛИ В БОЮ!\nВсе накопленные уровни, навыки и зелья сброшены.");
            layoutOptionsContainer.removeAllViews();

            Button btnRestart = new Button(this);
            btnRestart.setText("Начать заново с 1 уровня");
            btnRestart.setOnClickListener(v -> {
                initHeroStats(); // Сброс всех характеристик к 1 уровню
                updateHeroUI();
                storyManager.selectOption("start");
                renderCurrentStoryNode();
            });
            layoutOptionsContainer.addView(btnRestart);
            return;
        }

        updateCombatScreen(resultMsg);
    }

    private void fleeCombat() {
        isInCombat = false;
        tvGameLog.setText("Вы благополучно сбежали от опасного противника (" + activeMonster.getName() + ")!");
        layoutOptionsContainer.removeAllViews();

        Button btnContinue = new Button(this);
        btnContinue.setText("Вернуться к развилке");
        btnContinue.setOnClickListener(v -> {
            storyManager.selectOption("start");
            renderCurrentStoryNode();
        });
        layoutOptionsContainer.addView(btnContinue);
    }

    private void showTalentsDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_talents);
        dialog.getWindow().setLayout(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        );

        TextView tvAvailablePoints = dialog.findViewById(R.id.tvAvailablePoints);
        TextView tvHealthVal = dialog.findViewById(R.id.tvHealthVal);
        TextView tvStrengthVal = dialog.findViewById(R.id.tvStrengthVal);
        TextView tvIntVal = dialog.findViewById(R.id.tvIntVal);
        TextView tvStaminaVal = dialog.findViewById(R.id.tvStaminaVal);
        TextView tvDodgeVal = dialog.findViewById(R.id.tvDodgeVal);

        Button btnPlusHealth = dialog.findViewById(R.id.btnPlusHealth);
        Button btnPlusStrength = dialog.findViewById(R.id.btnPlusStrength);
        Button btnPlusInt = dialog.findViewById(R.id.btnPlusInt);
        Button btnPlusStamina = dialog.findViewById(R.id.btnPlusStamina);
        Button btnPlusDodge = dialog.findViewById(R.id.btnPlusDodge);
        Button btnCloseTalents = dialog.findViewById(R.id.btnCloseTalents);

        Runnable updateDialogUI = () -> {
            tvAvailablePoints.setText("Доступно очков: " + skillPoints);
            tvHealthVal.setText(String.valueOf(statHealth));
            tvStrengthVal.setText(String.valueOf(statStrength));
            tvIntVal.setText(String.valueOf(statIntelligence));
            tvStaminaVal.setText(String.valueOf(statStamina));
            tvDodgeVal.setText(statDodge + "%");
        };

        updateDialogUI.run();

        btnPlusHealth.setOnClickListener(v -> {
            if (skillPoints > 0) {
                skillPoints--;
                statHealth += 10;
                currentHealth += 10;
                updateDialogUI.run();
            } else {
                Toast.makeText(this, "Нет свободных очков навыков!", Toast.LENGTH_SHORT).show();
            }
        });

        btnPlusStrength.setOnClickListener(v -> {
            if (skillPoints > 0) {
                skillPoints--;
                statStrength += 2;
                updateDialogUI.run();
            } else {
                Toast.makeText(this, "Нет свободных очков навыков!", Toast.LENGTH_SHORT).show();
            }
        });

        btnPlusInt.setOnClickListener(v -> {
            if (skillPoints > 0) {
                skillPoints--;
                statIntelligence += 2;
                updateDialogUI.run();
            } else {
                Toast.makeText(this, "Нет свободных очков навыков!", Toast.LENGTH_SHORT).show();
            }
        });

        btnPlusStamina.setOnClickListener(v -> {
            if (skillPoints > 0) {
                skillPoints--;
                statStamina += 5;
                updateDialogUI.run();
            } else {
                Toast.makeText(this, "Нет свободных очков навыков!", Toast.LENGTH_SHORT).show();
            }
        });

        btnPlusDodge.setOnClickListener(v -> {
            if (skillPoints > 0) {
                skillPoints--;
                statDodge += 2;
                updateDialogUI.run();
            } else {
                Toast.makeText(this, "Нет свободных очков навыков!", Toast.LENGTH_SHORT).show();
            }
        });

        btnCloseTalents.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
