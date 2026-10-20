package com.example.textrpg;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements Runnable {

    private Thread gameThread;
    private boolean isPlaying;
    private SurfaceHolder holder;

    private String currentHeroClass = "Воин";
    private RouteManager.EventResult currentEvent;
    private boolean isInCombatState = false;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        holder = getHolder();
    }

    public void initGame(String heroClass) {
        this.currentHeroClass = heroClass;
    }

    public void setEventState(RouteManager.EventResult event, boolean inCombat) {
        this.currentEvent = event;
        this.isInCombatState = inCombat;
    }

    public void resume() {
        isPlaying = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void pause() {
        isPlaying = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (isPlaying) {
            if (!holder.getSurface().isValid()) continue;

            Canvas canvas = holder.lockCanvas();
            if (canvas != null) {
                renderGraphics(canvas);
                holder.unlockCanvasAndPost(canvas);
            }

            try {
                Thread.sleep(16); // ~60 FPS
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Главный метод рисовки игрового мира
    private void renderGraphics(Canvas canvas) {
        canvas.drawColor(Color.parseColor("#0D0B0E")); // Тёмный фон лабиринта

        float width = canvas.getWidth();
        float height = canvas.getHeight();
        float tileSize = width / 5f;

        // 1. Отрисовка развилки лабиринта (Тайлы пола и стен)
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
                boolean isWall = (col == 0 || col == 4 || (row == 0 && col != 2));
                GraphicsRenderer.drawDungeonTile(canvas, col * tileSize, row * tileSize, tileSize, isWall);
            }
        }

        // 2. Отрисовка игрока на стартовой позиции внизу
        float heroX = width / 2f;
        float heroY = tileSize * 3.5f;
        GraphicsRenderer.drawHero(canvas, heroX, heroY, tileSize * 0.35f, currentHeroClass);

        // 3. Отрисовка объекта развилки (Монстр / Сундук / Святыня) в центре
        if (currentEvent != null) {
            float objX = width / 2f;
            float objY = tileSize * 1.5f;

            if (currentEvent.type == RouteManager.EventType.MONSTER) {
                GraphicsRenderer.drawMonster(canvas, objX, objY, tileSize, currentEvent.monster.getLevel());
            } else if (currentEvent.type == RouteManager.EventType.CHEST) {
                GraphicsRenderer.drawChest(canvas, objX, objY, tileSize);
            } else if (currentEvent.type == RouteManager.EventType.SHRINE) {
                GraphicsRenderer.drawShrine(canvas, objX, objY, tileSize);
            }
        }

        // 4. Отрисовка статуса поверх графики
        Paint textPaint = new Paint();
        textPaint.setColor(Color.YELLOW);
        textPaint.setTextSize(36f);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);

        if (isInCombatState) {
            textPaint.setColor(Color.RED);
            canvas.drawText("⚔️ РЕЖИМ БОЯ ⚔️", width / 2f, 60f, textPaint);
        } else {
            canvas.drawText("🧭 ВЫБЕРИТЕ НАПРАВЛЕНИЕ ПУТИ", width / 2f, 60f, textPaint);
        }
    }
}
