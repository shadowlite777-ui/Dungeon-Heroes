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

    private float animFrame = 0f;
    private boolean triggerHitAnimation = false;

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

    public void triggerHit() {
        this.triggerHitAnimation = true;
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

            animFrame += 1.0f; // Увеличение счетчика фазы анимации

            Canvas canvas = holder.lockCanvas();
            if (canvas != null) {
                renderGraphics(canvas);
                holder.unlockCanvasAndPost(canvas);
            }

            if (triggerHitAnimation) {
                triggerHitAnimation = false;
            }

            try {
                Thread.sleep(16); // ~60 FPS
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void renderGraphics(Canvas canvas) {
        float width = canvas.getWidth();
        float height = canvas.getHeight();

        // 1. Отрисовка 2.5D коридора от первого лица
        GraphicsRenderer.draw25DDungeon(canvas, width, height, animFrame);

        // 2. Отрисовка текущего анимированного объекта перед глазами героя
        if (currentEvent != null) {
            if (currentEvent.type == RouteManager.EventType.MONSTER) {
                GraphicsRenderer.draw25DMonster(canvas, width, height, currentEvent.monster.getLevel(), animFrame, triggerHitAnimation);
            } else if (currentEvent.type == RouteManager.EventType.CHEST) {
                GraphicsRenderer.draw25DChest(canvas, width, height, animFrame);
            } else if (currentEvent.type == RouteManager.EventType.SHRINE) {
                GraphicsRenderer.draw25DShrine(canvas, width, height, animFrame);
            }
        }

        // 3. Отрисовка оружия героя от 1-го лица
        GraphicsRenderer.drawHeroFirstPersonWeapon(canvas, width, height, currentHeroClass, animFrame);

        // 4. Текстовый заголовок состояния
        Paint textPaint = new Paint();
        textPaint.setColor(Color.YELLOW);
        textPaint.setTextSize(34f);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);

        if (isInCombatState) {
            textPaint.setColor(Color.RED);
            canvas.drawText("⚔️ БОЙ С МОНСТРОМ ⚔️", width / 2f, 50f, textPaint);
        } else {
            canvas.drawText("🧭 ВЫБЕРИТЕ НАПРАВЛЕНИЕ", width / 2f, 50f, textPaint);
        }
    }
}
