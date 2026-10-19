package com.example.textrpg;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private View layoutMainMenu, layoutCharCreation, layoutGameMain, layoutAbilityContainer;
    private EditText etCharName;
    private RadioButton rbWarrior, rbMage, rbRogue;
    private TextView tvHeroInfo, tvHeroLevel, tvGameLog, tvHealthStatus, tvManaStatus;
    private ProgressBar pbHealth, pbMana;
    private Button btnClassAbility;
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
    private int currentMana = 30;
    private int statStrength = 10;
    private int statIntelligence = 10;
    private int statStamina = 50;
    private int statDodge = 0;

    // Инвентарь зелий
    private int countSmallPotion = 0;
    private int countMediumPotion = 0;
    private int countLargePotion = 0;
    private int countManaPotion = 0;    // +40% MP
    private int countDodgeElixir = 0;
    private int countArmorElixir = 0;

    // Эффекты и способности
    private int rageAttacksLeft = 0;     // У Воина (урон +70% на 2 атаки)
    private boolean abilityUsedThisCombat = false;
    private int mageCooldownCombats = 0; // Перезарядка мага (1 раз в 2 боя)

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
        layoutAbilityContainer = findViewById(R.id.layoutAbilityContainer);

        Button btnStartMenu = findViewById(R.id.btnStartMenu);
        etCharName = findViewById(R.id.etCharName);
        rbWarrior = findViewById(R.id.rbWarrior);
        rbMage = findViewById(R.id.rbMage);
        rbRogue = findViewById(R.id.rbRogue);
        Button btnCreateChar = findViewById(R.id.btnCreateChar);

        tvHeroInfo = findViewById(R.id.tvHeroInfo);
        tvHeroLevel = findViewById(R.id.tvHeroLevel);
        tvGameLog = findViewById(R.id.tvGameLog);
        tvHealthStatus = findViewById(R.id.tvHealthStatus);
        tvManaStatus = findViewById(R.id.tvManaStatus);
        pbHealth = findViewById(R.id.pbHealth);
        pbMana = findViewById(R.id.pbMana);
        btnClassAbility = findViewById(R.id.btnClassAbility);

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

        btnClassAbility.setOnClickListener(v -> useClassAbility());
        btnOpenTalents.setOnClickListener(v -> showTalentsDialog());
    }

    private int getMaxMana() {
        return statIntelligence * 3;
    }

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
        currentMana = getMaxMana();

        if (rbWarrior.isChecked()) {
            heroClass = "Воин";
            statDodge = 10;
            btnClassAbility.setText("Раж\n(4 MP)");
            btnClassAbility.setBackgroundColor(Color.parseColor("#8B7500")); // Тёмно-жёлтый
        } else if (rbMage.isChecked()) {
            heroClass = "Маг";
            statDodge = 0;
            btnClassAbility.setText("Гнев\nдревних\n(7 MP)");
            btnClassAbility.setBackgroundColor(Color.parseColor("#00BFFF")); // Голубой
        } else if (rbRogue.isChecked()) {
            heroClass = "Разбойник";
            statDodge = 25;
            btnClassAbility.setText("Удар\nсмерти\n(5 MP)");
            btnClassAbility.setBackgroundColor(Color.parseColor("#006400")); // Тёмно-зелёный
        }

        countSmallPotion = 0;
        countMediumPotion = 0;
        countLargePotion = 0;
        countManaPotion = 0;
        countDodgeElixir = 0;
        countArmorElixir = 0;
        mageCooldownCombats = 0;
    }

    private void updateHeroUI() {
        tvHeroInfo.setText(heroName + " (" + heroClass + ")");
        tvHeroLevel.setText("Уровень: " + heroLevel + " [" + currentExp + "/" + requiredExp + " EXP]");

        // Обновление полосок HP и MP
        pbHealth.setMax(statHealth);
        pbHealth.setProgress(currentHealth);
        tvHealthStatus.setText(currentHealth + "/" + statHealth + " HP");

        int maxMp = getMaxMana();
        pbMana.setMax(maxMp);
        pbMana.setProgress(currentMana);
        tvManaStatus.setText(currentMana + "/" + maxMp + " MP");
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

        layoutAbilityContainer.setVisibility(View.GONE);
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
        rageAttacksLeft = 0;
        abilityUsedThisCombat = false;

        // Восполнение маны на 20% в начале каждого боя
        int manaRestored = (int) (getMaxMana() * 0.20);
        currentMana = Math.min(getMaxMana(), currentMana + manaRestored);

        if (mageCooldownCombats > 0) {
            mageCooldownCombats--;
        }

        activeMonster = Monster.generateMonster(heroLevel);
        layoutAbilityContainer.setVisibility(View.VISIBLE);
        updateHeroUI();
        updateCombatScreen("На вашем пути встал противник! (+20% MP восполнено)");
    }

    private void updateCombatScreen(String message) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== БОЙ С МОНСТРОМ ===\n");
        sb.append("Враг: ").append(activeMonster.getName()).append("\n");
        sb.append("HP Врага: ").append(activeMonster.getCurrentHp()).append("/").append(activeMonster.getMaxHp()).append("\n");

        int currentDodge = statDodge + (buffDodgeTurns > 0 ? 10 : 0);
        sb.append("Броня: ").append((int) getEffectiveArmor()).append(" | Уклонение: ").append(currentDodge).append("%\n");
        if (rageAttacksLeft > 0) {
            sb.append(" [Эффект «Раж»: ").append(rageAttacksLeft).append(" атк.]\n");
        }
        sb.append("\n").append(message);

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

        // 2. Использование зелий (без завершения хода)
        if (countSmallPotion > 0) {
            Button btn = new Button(this);
            btn.setText("Малое зелье HP (" + countSmallPotion + " шт) [+5%]");
            btn.setLayoutParams(params);
            btn.setOnClickListener(v -> usePotion(0.05, 0, 1, 0, 0, 0, "Вы выпили малое зелье HP."));
            layoutOptionsContainer.addView(btn);
        }
        if (countMediumPotion > 0) {
            Button btn = new Button(this);
            btn.setText("Среднее зелье HP (" + countMediumPotion + " шт) [+10%]");
            btn.setLayoutParams(params);
            btn.setOnClickListener(v -> usePotion(0.10, 0, 0, 1, 0, 0, "Вы выпили среднее зелье HP."));
            layoutOptionsContainer.addView(btn);
        }
        if (countLargePotion > 0) {
            Button btn = new Button(this);
            btn.setText("Большое зелье HP (" + countLargePotion + " шт) [+20%]");
            btn.setLayoutParams(params);
            btn.setOnClickListener(v -> usePotion(0.20, 0, 0, 0, 1, 0, "Вы выпили большое зелье HP."));
            layoutOptionsContainer.addView(btn);
        }
        if (countManaPotion > 0) {
            Button btn = new Button(this);
            btn.setText("Зелье маны (" + countManaPotion + " шт) [+40% MP]");
            btn.setLayoutParams(params);
            btn.setOnClickListener(v -> usePotion(0, 0.40, 0, 0, 0, 1, "Вы выпили зелье маны."));
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

        // 3. Побег
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

    private void usePotion(double hpPct, double mpPct, int small, int medium, int large, int manaP, String msg) {
        countSmallPotion -= small;
        countMediumPotion -= medium;
        countLargePotion -= large;
        countManaPotion -= manaP;

        if (hpPct > 0) {
            int heal = (int) (statHealth * hpPct);
            currentHealth = Math.min(statHealth, currentHealth + heal);
        }
        if (mpPct > 0) {
            int manaHeal = (int) (getMaxMana() * mpPct);
            currentMana = Math.min(getMaxMana(), currentMana + manaHeal);
        }

        updateHeroUI();
        updateCombatScreen(msg + "\nВаш ход продолжается.");
    }

    // Использование уникальной способности класса
    private void useClassAbility() {
        if (!isInCombat) return;

        if (abilityUsedThisCombat) {
            Toast.makeText(this, "Способность уже использована в этом бою!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (heroClass.equals("Воин")) {
            if (currentMana < 4) {
                Toast.makeText(this, "Недостаточно маны! Требуется 4 MP.", Toast.LENGTH_SHORT).show();
                return;
            }
            currentMana -= 4;
            rageAttacksLeft = 2;
            abilityUsedThisCombat = true;
            updateHeroUI();
            updateCombatScreen("Активирована способность «Раж»! Урон следующих 2 атак увеличен на 70%.");

        } else if (heroClass.equals("Разбойник")) {
            if (currentMana < 5) {
                Toast.makeText(this, "Недостаточно маны! Требуется 5 MP.", Toast.LENGTH_SHORT).show();
                return;
            }
            currentMana -= 5;
            abilityUsedThisCombat = true;
            updateHeroUI();

            // «Удар смерти»: средний урон + 230% (итого х3.30)
            double baseDmg = statStrength * 3.30;
            int finalDmg = (int) Math.max(1, baseDmg);

            activeMonster.setCurrentHp(activeMonster.getCurrentHp() - finalDmg);

            if (activeMonster.getCurrentHp() <= 0) {
                processMonsterDefeat("Нанесен УДАР СМЕРТИ (" + finalDmg + " урона)!");
            } else {
                performMonsterAttack(finalDmg, "УДАР СМЕРТИ нанес " + finalDmg + " урона!\n");
            }

        } else if (heroClass.equals("Маг")) {
            if (mageCooldownCombats > 0) {
                Toast.makeText(this, "Способность Мага перезаряжается (доступно через 1 бой).", Toast.LENGTH_SHORT).show();
                return;
            }
            if (currentMana < 7) {
                Toast.makeText(this, "Недостаточно маны! Требуется 7 MP.", Toast.LENGTH_SHORT).show();
                return;
            }
            currentMana -= 7;
            abilityUsedThisCombat = true;
            mageCooldownCombats = 2; // Восстановится через 2 боя
            updateHeroUI();

            // «Гнев древних»: мгновенное убийство + гарантированный зелейный дроп
            activeMonster.setCurrentHp(0);
            processMonsterDefeatWithBonusPotion("ГНЕВ ДРЕВНИХ испепелил врага мгновенно!");
        }
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

        // Если включен «Раж» у воина
        if (rageAttacksLeft > 0) {
            baseDamage *= 1.70;
            rageAttacksLeft--;
        }

        double spread = 0.8 + (random.nextDouble() * 0.4);
        int heroDamage = (int) Math.max(1, baseDamage * spread);

        activeMonster.setCurrentHp(activeMonster.getCurrentHp() - heroDamage);

        if (activeMonster.getCurrentHp() <= 0) {
            processMonsterDefeat("Вы нанесли " + heroDamage + " урона.");
            return;
        }

        performMonsterAttack(heroDamage, "Вы нанесли " + heroDamage + " урона.\n");
    }

    private void processMonsterDefeat(String attackLog) {
        processMonsterDefeatInternal(attackLog, false);
    }

    private void processMonsterDefeatWithBonusPotion(String attackLog) {
        processMonsterDefeatInternal(attackLog, true);
    }

    private void processMonsterDefeatInternal(String attackLog, boolean forceBonusPotion) {
        isInCombat = false;
        layoutAbilityContainer.setVisibility(View.GONE);

        int expReward = activeMonster.calculateExpReward(heroLevel);
        String lootMsg = generateLoot(forceBonusPotion);

        StringBuilder victoryMsg = new StringBuilder();
        victoryMsg.append(attackLog).append("\n\n");
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
    }

    private String generateLoot(boolean forceBonusPotion) {
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
        // Шанс 15% на зелье маны
        if (random.nextDouble() <= 0.15) {
            countManaPotion++;
            loot.append("- Зелье маны (+40% MP)\n");
        }
        if (random.nextDouble() <= 0.25) {
            countDodgeElixir++;
            loot.append("- Эликсир уклонения\n");
        }
        if (random.nextDouble() <= 0.25) {
            countArmorElixir++;
            loot.append("- Эликсир брони\n");
        }

        // Бонусное гарантированное зелье за способность Мага
        if (forceBonusPotion) {
            countManaPotion++;
            loot.append("- [Древний манускрипт] Гарантированное зелье маны!\n");
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
        updateHeroUI();

        String resultMsg = prefixMessage + activeMonster.getName() + " атакует и наносит вам " + finalMonsterDamage + " урона (Поглощено броней: " + (int) (armorAbsorption * 100) + "%)!";

        if (currentHealth <= 0) {
            currentHealth = 0;
            updateHeroUI();
            isInCombat = false;
            layoutAbilityContainer.setVisibility(View.GONE);
            tvGameLog.setText(resultMsg + "\n\nВЫ ПОГИБЛИ В БОЮ!\nВсе накопленные уровни, навыки и зелья сброшены.");
            layoutOptionsContainer.removeAllViews();

            Button btnRestart = new Button(this);
            btnRestart.setText("Начать заново с 1 уровня");
            btnRestart.setOnClickListener(v -> {
                initHeroStats();
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
        layoutAbilityContainer.setVisibility(View.GONE);
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
            updateHeroUI();
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
                currentMana += 6; // С увеличением интеллекта растет макс. мана и текущая
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
