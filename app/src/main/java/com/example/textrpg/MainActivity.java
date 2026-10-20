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
    private GameView gameView;
    private EditText etCharName;
    private RadioButton rbWarrior, rbMage, rbRogue;
    private TextView tvHeroInfo, tvHeroLevel, tvGameLog, tvHealthStatus, tvManaStatus;
    private ProgressBar pbHealth, pbMana;
    private Button btnClassAbility;
    private LinearLayout layoutOptionsContainer;

    private RouteManager routeManager;

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
    private double baseArmorValue = 100.0;

    private int countSmallPotion = 0;
    private int countMediumPotion = 0;
    private int countLargePotion = 0;
    private int countManaPotion = 0;
    private int countDodgeElixir = 0;
    private int countArmorElixir = 0;

    private int rageAttacksLeft = 0;
    private boolean abilityUsedThisCombat = false;
    private int mageCooldownCombats = 0;

    private int buffDodgeTurns = 0;
    private int buffArmorTurns = 0;

    private boolean isInCombat = false;
    private Monster activeMonster;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        routeManager = new RouteManager();

        layoutMainMenu = findViewById(R.id.layoutMainMenu);
        layoutCharCreation = findViewById(R.id.layoutCharCreation);
        layoutGameMain = findViewById(R.id.layoutGameMain);
        layoutAbilityContainer = findViewById(R.id.layoutAbilityContainer);
        gameView = findViewById(R.id.gameView);

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

            gameView.initGame(heroClass);
            updateHeroUI();
            showRouteChoices("🏰 Вы смотрите на графическую карту Бесконечного Лабиринта.");

            layoutCharCreation.setVisibility(View.GONE);
            layoutGameMain.setVisibility(View.VISIBLE);
        });

        btnClassAbility.setOnClickListener(v -> useClassAbility());
        btnOpenTalents.setOnClickListener(v -> showTalentsDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameView != null) gameView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameView != null) gameView.pause();
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
            baseArmorValue = 200.0;
            btnClassAbility.setText("Раж\n(4 MP)");
            btnClassAbility.setBackgroundColor(Color.parseColor("#8B7500"));
        } else if (rbMage.isChecked()) {
            heroClass = "Маг";
            statDodge = 0;
            baseArmorValue = 100.0;
            btnClassAbility.setText("Гнев\nдревних\n(" + getMageManaCost() + " MP)");
            btnClassAbility.setBackgroundColor(Color.parseColor("#00BFFF"));
        } else if (rbRogue.isChecked()) {
            heroClass = "Разбойник";
            statDodge = 25;
            baseArmorValue = 100.0;
            btnClassAbility.setText("Удар\nсмерти\n(5 MP)");
            btnClassAbility.setBackgroundColor(Color.parseColor("#006400"));
        }

        countSmallPotion = 0;
        countMediumPotion = 0;
        countLargePotion = 0;
        countManaPotion = 0;
        countDodgeElixir = 0;
        countArmorElixir = 0;
        mageCooldownCombats = 0;
    }

    private int getMageManaCost() {
        int reductions = heroLevel / 5;
        return Math.max(2, 7 - reductions);
    }

    private void updateHeroUI() {
        tvHeroInfo.setText(heroName + " (" + heroClass + ")");
        tvHeroLevel.setText("Уровень: " + heroLevel + " [" + currentExp + "/" + requiredExp + " EXP]");

        pbHealth.setMax(statHealth);
        pbHealth.setProgress(currentHealth);
        tvHealthStatus.setText(currentHealth + "/" + statHealth + " HP");

        int maxMp = getMaxMana();
        pbMana.setMax(maxMp);
        pbMana.setProgress(currentMana);
        tvManaStatus.setText(currentMana + "/" + maxMp + " MP");

        if (heroClass.equals("Маг")) {
            btnClassAbility.setText("Гнев\nдревних\n(" + getMageManaCost() + " MP)");
        }
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
            currentHealth = statHealth;
            currentMana = getMaxMana();
            Toast.makeText(this, "✨ УРОВЕНЬ ПОВЫШЕН до " + heroLevel + "! Здоровье и Мана восстановлены!", Toast.LENGTH_LONG).show();
        }
        updateHeroUI();
    }

    private double getEffectiveArmor() {
        double currentBase = baseArmorValue + (heroLevel - 1) * 10.0;
        if (buffArmorTurns > 0) {
            currentBase *= 1.20;
        }
        return currentBase;
    }

    private void showRouteChoices(String headerMessage) {
        isInCombat = false;
        layoutAbilityContainer.setVisibility(View.GONE);
        tvGameLog.setText(headerMessage);
        layoutOptionsContainer.removeAllViews();

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 8);

        Button btnLeft = new Button(this);
        btnLeft.setText("⬅️ Пойти НАЛЕВО");
        btnLeft.setBackgroundResource(R.drawable.btn_3d_primary);
        btnLeft.setTextColor(Color.parseColor("#FFD700"));
        btnLeft.setLayoutParams(params);
        btnLeft.setOnClickListener(v -> handleRouteSelection("LEFT"));
        layoutOptionsContainer.addView(btnLeft);

        Button btnStraight = new Button(this);
        btnStraight.setText("⬆️ Идти ПРЯМО");
        btnStraight.setBackgroundResource(R.drawable.btn_3d_primary);
        btnStraight.setTextColor(Color.parseColor("#FFD700"));
        btnStraight.setLayoutParams(params);
        btnStraight.setOnClickListener(v -> handleRouteSelection("STRAIGHT"));
        layoutOptionsContainer.addView(btnStraight);

        Button btnRight = new Button(this);
        btnRight.setText("➡️ Пойти НАПРАВО");
        btnRight.setBackgroundResource(R.drawable.btn_3d_primary);
        btnRight.setTextColor(Color.parseColor("#FFD700"));
        btnRight.setLayoutParams(params);
        btnRight.setOnClickListener(v -> handleRouteSelection("RIGHT"));
        layoutOptionsContainer.addView(btnRight);
    }

    private void handleRouteSelection(String direction) {
        RouteManager.EventResult event = routeManager.generateEvent(direction, heroLevel);
        routeManager.advanceDepth();

        gameView.setEventState(event, event.type == RouteManager.EventType.MONSTER);

        switch (event.type) {
            case MONSTER:
                startCombat(event.monster, event.description);
                break;
            case CHEST:
                handleChestEvent(event.description);
                break;
            case SHRINE:
                handleShrineEvent(event.description);
                break;
            case TRAP:
                handleTrapEvent(event.description);
                break;
        }
    }

    private void handleChestEvent(String desc) {
        Random random = new Random();
        StringBuilder chestLoot = new StringBuilder(desc + "\n📦 Содержимое:");

        if (random.nextDouble() <= 0.6) {
            countMediumPotion++;
            chestLoot.append(" Среднее зелье HP;");
        }
        if (random.nextDouble() <= 0.3) {
            countManaPotion++;
            chestLoot.append(" Зелье маны;");
        }
        countSmallPotion++;

        tvGameLog.setText(chestLoot.toString());
        layoutOptionsContainer.removeAllViews();

        Button btnContinue = new Button(this);
        btnContinue.setText("🗺️ Идти дальше");
        btnContinue.setBackgroundResource(R.drawable.btn_3d_primary);
        btnContinue.setTextColor(Color.parseColor("#FFD700"));
        btnContinue.setOnClickListener(v -> showRouteChoices("Сундук открыт. Вы продолжаете путь."));
        layoutOptionsContainer.addView(btnContinue);
    }

    private void handleShrineEvent(String desc) {
        int healAmount = (int) (statHealth * 0.25);
        int manaAmount = (int) (getMaxMana() * 0.25);
        currentHealth = Math.min(statHealth, currentHealth + healAmount);
        currentMana = Math.min(getMaxMana(), currentMana + manaAmount);
        updateHeroUI();

        tvGameLog.setText(desc + "\n✨ Восстановлено +" + healAmount + " HP и +" + manaAmount + " MP!");
        layoutOptionsContainer.removeAllViews();

        Button btnContinue = new Button(this);
        btnContinue.setText("🗺️ Идти дальше");
        btnContinue.setBackgroundResource(R.drawable.btn_3d_primary);
        btnContinue.setTextColor(Color.parseColor("#FFD700"));
        btnContinue.setOnClickListener(v -> showRouteChoices("Силы восстановлены, лабиринт ждет."));
        layoutOptionsContainer.addView(btnContinue);
    }

    private void handleTrapEvent(String desc) {
        int damage = 5 + heroLevel * 2;
        currentHealth = Math.max(1, currentHealth - damage);
        updateHeroUI();

        tvGameLog.setText(desc + "\n⚠️ Потеряно -" + damage + " HP!");
        layoutOptionsContainer.removeAllViews();

        Button btnContinue = new Button(this);
        btnContinue.setText("🗺️ Продолжить путь");
        btnContinue.setBackgroundResource(R.drawable.btn_3d_primary);
        btnContinue.setTextColor(Color.parseColor("#FFD700"));
        btnContinue.setOnClickListener(v -> showRouteChoices("Вы осторожно выходите на новую развилку."));
        layoutOptionsContainer.addView(btnContinue);
    }

    private void startCombat(Monster monster, String introMessage) {
        isInCombat = true;
        buffDodgeTurns = 0;
        buffArmorTurns = 0;
        rageAttacksLeft = 0;
        abilityUsedThisCombat = false;

        int manaRestored = (int) (getMaxMana() * 0.20);
        currentMana = Math.min(getMaxMana(), currentMana + manaRestored);

        if (mageCooldownCombats > 0) mageCooldownCombats--;

        activeMonster = monster;
        layoutAbilityContainer.setVisibility(View.VISIBLE);
        updateHeroUI();
        updateCombatScreen(introMessage + "\n⚔️ Начинается бой! (+20% MP восполнено)");
    }

    private void updateCombatScreen(String message) {
        StringBuilder sb = new StringBuilder();
        sb.append("🔥 Враг: ").append(activeMonster.getName()).append(" [HP: ").append(activeMonster.getCurrentHp()).append("/").append(activeMonster.getMaxHp()).append("]\n");
        sb.append(message);

        tvGameLog.setText(sb.toString());
        layoutOptionsContainer.removeAllViews();

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 8);

        Button btnAttack = new Button(this);
        btnAttack.setText("⚔️ Атака (Сила: " + statStrength + ")");
        btnAttack.setBackgroundResource(R.drawable.btn_3d_primary);
        btnAttack.setTextColor(Color.parseColor("#FFD700"));
        btnAttack.setLayoutParams(params);
        btnAttack.setOnClickListener(v -> performHeroAttack());
        layoutOptionsContainer.addView(btnAttack);

        if (countSmallPotion > 0) {
            Button btn = new Button(this);
            btn.setText("🧪 Малое зелье HP (" + countSmallPotion + " шт)");
            btn.setBackgroundResource(R.drawable.btn_3d_primary);
            btn.setTextColor(Color.WHITE);
            btn.setLayoutParams(params);
            btn.setOnClickListener(v -> usePotion(0.05, 0, 1, 0, 0, 0, "Вы выпили малое зелье HP."));
            layoutOptionsContainer.addView(btn);
        }
        if (countManaPotion > 0) {
            Button btn = new Button(this);
            btn.setText("💧 Зелье маны (" + countManaPotion + " шт)");
            btn.setBackgroundResource(R.drawable.btn_3d_primary);
            btn.setTextColor(Color.parseColor("#66B2FF"));
            btn.setLayoutParams(params);
            btn.setOnClickListener(v -> usePotion(0, 0.40, 0, 0, 0, 1, "Вы выпили зелье маны."));
            layoutOptionsContainer.addView(btn);
        }

        boolean canFleeLevel = activeMonster.getLevel() >= heroLevel + 3;
        boolean canFleeHealth = ((double) currentHealth / statHealth) < 0.35;

        if (canFleeLevel || canFleeHealth) {
            Button btnFlee = new Button(this);
            btnFlee.setText("🏃 Сбежать из боя");
            btnFlee.setBackgroundResource(R.drawable.btn_3d_primary);
            btnFlee.setTextColor(Color.LTGRAY);
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

        if (hpPct > 0) currentHealth = Math.min(statHealth, currentHealth + (int)(statHealth * hpPct));
        if (mpPct > 0) currentMana = Math.min(getMaxMana(), currentMana + (int)(getMaxMana() * mpPct));

        updateHeroUI();
        updateCombatScreen(msg + "\nВаш ход продолжается.");
    }

    private void useClassAbility() {
        if (!isInCombat || abilityUsedThisCombat) return;

        int boostMultiplier = (heroLevel / 5);

        if (heroClass.equals("Воин")) {
            if (currentMana < 4) return;
            currentMana -= 4;
            rageAttacksLeft = 2;
            abilityUsedThisCombat = true;
            updateHeroUI();
            updateCombatScreen("💥 «Раж» активирован! Следующие 2 атаки мощнее на " + (70 + boostMultiplier * 10) + "%.");

        } else if (heroClass.equals("Разбойник")) {
            if (currentMana < 5) return;
            currentMana -= 5;
            abilityUsedThisCombat = true;
            updateHeroUI();

            double baseDmg = statStrength * (3.30 + boostMultiplier * 0.10);
            int finalDmg = (int) Math.max(1, baseDmg);

            activeMonster.setCurrentHp(activeMonster.getCurrentHp() - finalDmg);

            if (activeMonster.getCurrentHp() <= 0) {
                processMonsterDefeat("🗡️ УДАР СМЕРТИ нанес " + finalDmg + " урона!");
            } else {
                performMonsterAttack(finalDmg, "🗡️ УДАР СМЕРТИ нанес " + finalDmg + " урона!\n");
            }

        } else if (heroClass.equals("Маг")) {
            int cost = getMageManaCost();
            if (mageCooldownCombats > 0 || currentMana < cost) return;
            currentMana -= cost;
            abilityUsedThisCombat = true;
            mageCooldownCombats = 2;
            updateHeroUI();

            activeMonster.setCurrentHp(0);
            processMonsterDefeat("🔮 ГНЕВ ДРЕВНИХ испепелил врага мгновенно!");
        }
    }

    private void performHeroAttack() {
        if (gameView != null) {
            gameView.triggerHit();
        }

        Random random = new Random();
        double baseDamage = statStrength;

        if (rageAttacksLeft > 0) {
            baseDamage *= (1.70 + (heroLevel / 5) * 0.10);
            rageAttacksLeft--;
        }

        int heroDamage = (int) Math.max(1, baseDamage * (0.8 + random.nextDouble() * 0.4));
        activeMonster.setCurrentHp(activeMonster.getCurrentHp() - heroDamage);

        if (activeMonster.getCurrentHp() <= 0) {
            processMonsterDefeat("Вы нанесли " + heroDamage + " урона.");
            return;
        }

        performMonsterAttack(heroDamage, "Вы нанесли " + heroDamage + " урона.\n");
    }

    private void processMonsterDefeat(String attackLog) {
        isInCombat = false;
        layoutAbilityContainer.setVisibility(View.GONE);

        int expReward = activeMonster.calculateExpReward(heroLevel);
        addExperience(expReward);

        gameView.setEventState(null, false);

        tvGameLog.setText(attackLog + "\n🎉 ПОБЕДА НАД " + activeMonster.getName() + "! Получено +" + expReward + " EXP.");
        layoutOptionsContainer.removeAllViews();

        Button btnContinue = new Button(this);
        btnContinue.setText("🗺️ Выбрать новый путь");
        btnContinue.setBackgroundResource(R.drawable.btn_3d_primary);
        btnContinue.setTextColor(Color.parseColor("#FFD700"));
        btnContinue.setOnClickListener(v -> showRouteChoices("Вы осматриваетесь после боя."));
        layoutOptionsContainer.addView(btnContinue);
    }

    private void performMonsterAttack(int heroLastDamage, String prefixMessage) {
        Random random = new Random();

        if (random.nextInt(100) < statDodge) {
            updateCombatScreen(prefixMessage + activeMonster.getName() + " атакует, но вы УКЛОНИЛИСЬ!");
            return;
        }

        double currentArmor = getEffectiveArmor();
        double armorAbsorption = Math.min(0.75, (currentArmor / 100.0) * 0.15 / (1.0 + (heroLevel - 1) * 0.15));
        int finalDamage = (int) Math.max(1, activeMonster.getAttackPower() * (0.8 + random.nextDouble() * 0.4) * (1.0 - armorAbsorption));

        currentHealth -= finalDamage;
        updateHeroUI();

        if (currentHealth <= 0) {
            currentHealth = 0;
            updateHeroUI();
            isInCombat = false;
            layoutAbilityContainer.setVisibility(View.GONE);
            tvGameLog.setText("💀 ВЫ ПОГИБЛИ В БОЮ!\nВсе уровни сброшены.");
            layoutOptionsContainer.removeAllViews();

            Button btnRestart = new Button(this);
            btnRestart.setText("🔄 Начать заново с 1 уровня");
            btnRestart.setBackgroundResource(R.drawable.btn_3d_primary);
            btnRestart.setTextColor(Color.RED);
            btnRestart.setOnClickListener(v -> {
                initHeroStats();
                updateHeroUI();
                showRouteChoices("🏰 Вы у начала Бесконечного Лабиринта.");
            });
            layoutOptionsContainer.addView(btnRestart);
            return;
        }

        updateCombatScreen(prefixMessage + activeMonster.getName() + " наносит " + finalDamage + " урона!");
    }

    private void fleeCombat() {
        isInCombat = false;
        layoutAbilityContainer.setVisibility(View.GONE);
        gameView.setEventState(null, false);
        showRouteChoices("🏃 Вы успешно сбежали от " + activeMonster.getName() + "!");
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
            }
        });

        btnPlusStrength.setOnClickListener(v -> {
            if (skillPoints > 0) {
                skillPoints--;
                statStrength += 2;
                updateDialogUI.run();
            }
        });

        btnPlusInt.setOnClickListener(v -> {
            if (skillPoints > 0) {
                skillPoints--;
                statIntelligence += 2;
                currentMana += 6;
                updateDialogUI.run();
            }
        });

        btnPlusStamina.setOnClickListener(v -> {
            if (skillPoints > 0) {
                skillPoints--;
                statStamina += 5;
                updateDialogUI.run();
            }
        });

        btnPlusDodge.setOnClickListener(v -> {
            if (skillPoints > 0) {
                skillPoints--;
                statDodge += 2;
                updateDialogUI.run();
            }
        });

        btnCloseTalents.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}
