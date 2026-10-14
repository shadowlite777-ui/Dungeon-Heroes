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

public class MainActivity extends AppCompatActivity {

    // Экраны (контейнеры)
    private View layoutMainMenu, layoutCharCreation, layoutGameMain;

    // Элементы создания персонажа
    private EditText etCharName;
    private RadioButton rbWarrior, rbMage, rbRogue;

    // Элементы игрового экрана
    private TextView tvHeroInfo, tvHeroLevel, tvGameLog;
    private LinearLayout layoutOptionsContainer;

    // Менеджер сюжета
    private StoryManager storyManager;

    // Характеристики персонажа
    private String heroName = "";
    private String heroClass = "";
    private int heroLevel = 1;
    private int skillPoints = 3;

    private int statHealth = 100;
    private int statStrength = 10;
    private int statIntelligence = 10;
    private int statStamina = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        storyManager = new StoryManager();

        // Инициализация виджетов
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

        // Кнопка Старт в главном меню
        btnStartMenu.setOnClickListener(v -> {
            layoutMainMenu.setVisibility(View.GONE);
            layoutCharCreation.setVisibility(View.VISIBLE);
        });

        // Кнопка создания персонажа
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

            updateHeroUI();
            renderCurrentStoryNode();

            // Переход на игровой экран
            layoutCharCreation.setVisibility(View.GONE);
            layoutGameMain.setVisibility(View.VISIBLE);
        });

        // Кнопка открытия окна талантов
        btnOpenTalents.setOnClickListener(v -> showTalentsDialog());
    }

    private void updateHeroUI() {
        tvHeroInfo.setText(heroName + " (" + heroClass + ")");
        tvHeroLevel.setText("Уровень: " + heroLevel);
    }

    // Отображение текущего текстового узла сюжета и сгенерированных кнопок выбора
    private void renderCurrentStoryNode() {
        StoryNode node = storyManager.getCurrentNode();
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

    // Окно талантов на полэкрана
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
