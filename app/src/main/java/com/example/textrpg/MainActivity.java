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

    private String heroName = "";
    private String heroClass = "";
    private int heroLevel = 1;
    private int skillPoints = 3;

    private int statHealth = 100;
    private int currentHealth = 100;
    private int statStrength = 10;
    private int statIntelligence = 10;
    private int statStamina = 50;

    // Переменные состояния боя
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

            if (rbWarrior.isChecked()) heroClass = "Воин";
            else if (rbMage.isChecked()) heroClass = "Маг";
            else if (rbRogue.isChecked()) heroClass = "Разбойник";

            currentHealth = statHealth;
            updateHeroUI();
            renderCurrentStoryNode();

            layoutCharCreation.setVisibility(View.GONE);
            layoutGameMain.setVisibility(View.VISIBLE);
        });

        btnOpenTalents.setOnClickListener(v -> showTalentsDialog());
    }

    private void updateHeroUI() {
        tvHeroInfo.setText(heroName + " (" + heroClass + ")");
        tvHeroLevel.setText("Уровень: " + heroLevel);
    }

    private void renderCurrentStoryNode() {
        StoryNode node = storyManager.getCurrentNode();

        // Если узел связан с боем — запускаем бой
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
            params.setMargins(0, 0, 0, 16);
            btnOption.setLayoutParams(params);

            btnOption.setOnClickListener(v -> {
                storyManager.selectOption(option.getTargetNodeId());
                renderCurrentStoryNode();
            });

            layoutOptionsContainer.addView(btnOption);
        }
    }

    // Инициализация боя
    private void startCombat() {
        isInCombat = true;
        activeMonster = Monster.generateMonster(heroLevel);
        updateCombatScreen("На вашего пути встал противник!");
    }

    // Обновление экрана боя и отрисовка кнопок действий
    private void updateCombatScreen(String message) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== БОЙ С МОНСТРОМ ===\n");
        sb.append("Враг: ").append(activeMonster.getName()).append("\n");
        sb.append("HP Врага: ").append(activeMonster.getCurrentHp()).append("/").append(activeMonster.getMaxHp()).append("\n");
        sb.append("Ваше здоровье: ").append(currentHealth).append("/").append(statHealth).append("\n\n");
        sb.append(message);

        tvGameLog.setText(sb.toString());
        layoutOptionsContainer.removeAllViews();

        // Кнопка атаки
        Button btnAttack = new Button(this);
        btnAttack.setText("Атака (Сила: " + statStrength + ")");
        btnAttack.setOnClickListener(v -> performHeroAttack());
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 16);
        btnAttack.setLayoutParams(params);
        layoutOptionsContainer.addView(btnAttack);

        // Условия для побега:
        // 1. Уровень монстра выше уровня героя на 3 и более
        // 2. Уровень здоровья ниже 35%
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

    // Логика атаки героя с учетом разницы уровней и разброса ±20%
    private void performHeroAttack() {
        Random random = new Random();
        double baseDamage = statStrength; // Базовый урон зависит от силы
        int levelDiff = activeMonster.getLevel() - heroLevel;

        if (activeMonster.getLevel() < heroLevel) {
            // Если уровень монстра ниже, урон повышен на 10%
            baseDamage *= 1.10;
        } else if (levelDiff > 0) {
            // Если уровень выше, урон снижается на 5% за каждые 2 уровня превышения
            int pairs = levelDiff / 2;
            double reduction = pairs * 0.05;
            baseDamage *= Math.max(0.1, (1.0 - reduction));
        }

        // Рандомный разброс ±20% (от 0.8 до 1.2)
        double spread = 0.8 + (random.nextDouble() * 0.4);
        int heroDamage = (int) Math.max(1, baseDamage * spread);

        activeMonster.setCurrentHp(activeMonster.getCurrentHp() - heroDamage);

        if (activeMonster.getCurrentHp() <= 0) {
            // Победа
            isInCombat = false;
            tvGameLog.setText("Вы нанесли " + heroDamage + " урона.\n\nВы победили монстра " + activeMonster.getName() + "!");
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

        // Ход монстра
        performMonsterAttack(heroDamage);
    }

    // Логика ответной атаки монстра
    private void performMonsterAttack(int heroLastDamage) {
        Random random = new Random();
        double monsterBaseDmg = activeMonster.getAttackPower();
        int levelDiff = activeMonster.getLevel() - heroLevel;

        if (activeMonster.getLevel() > heroLevel) {
            monsterBaseDmg *= (1.0 + (levelDiff * 0.05));
        } else if (activeMonster.getLevel() < heroLevel) {
            monsterBaseDmg *= (1.0 - (Math.abs(levelDiff) * 0.05));
        }

        double spread = 0.8 + (random.nextDouble() * 0.4);
        int monsterDamage = (int) Math.max(1, monsterBaseDmg * spread);

        currentHealth -= monsterDamage;

        String resultMsg = "Вы нанесли " + heroLastDamage + " урона.\n" +
                activeMonster.getName() + " атакует и наносит вам " + monsterDamage + " урона!";

        if (currentHealth <= 0) {
            currentHealth = 0;
            isInCombat = false;
            tvGameLog.setText(resultMsg + "\n\nВы погибли в бою...");
            layoutOptionsContainer.removeAllViews();

            Button btnRestart = new Button(this);
            btnRestart.setText("Начать заново");
            btnRestart.setOnClickListener(v -> {
                currentHealth = statHealth;
                storyManager.selectOption("start");
                renderCurrentStoryNode();
            });
            layoutOptionsContainer.addView(btnRestart);
            return;
        }

        updateCombatScreen(resultMsg);
    }

    // Побег из боя
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

    // Окно талантов
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

        Button btnPlusHealth = dialog.findViewById(R.id.btnPlusHealth);
        Button btnPlusStrength = dialog.findViewById(R.id.btnPlusStrength);
        Button btnPlusInt = dialog.findViewById(R.id.btnPlusInt);
        Button btnPlusStamina = dialog.findViewById(R.id.btnPlusStamina);
        Button btnCloseTalents = dialog.findViewById(R.id.btnCloseTalents);

        Runnable updateDialogUI = () -> {
            tvAvailablePoints.setText("Доступно очков: " + skillPoints);
            tvHealthVal.setText(String.valueOf(statHealth));
            tvStrengthVal.setText(String.valueOf(statStrength));
            tvIntVal.setText(String.valueOf(statIntelligence));
            tvStaminaVal.setText(String.valueOf(statStamina));
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

        btnCloseTalents.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
