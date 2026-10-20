package com.example.textrpg;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;

public class GraphicsRenderer {

    // Отрисовка 2.5D коридора от первого лица
    public static void draw25DDungeon(Canvas canvas, float width, float height, float animFrame) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // 1. Потолок и Пол с градиентной глубиной
        Paint ceilingPaint = new Paint();
        ceilingPaint.setShader(new LinearGradient(0, 0, 0, height / 2, Color.parseColor("#08070A"), Color.parseColor("#1A1622"), Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, width, height / 2, ceilingPaint);

        Paint floorPaint = new Paint();
        floorPaint.setShader(new LinearGradient(0, height / 2, 0, height, Color.parseColor("#120F17"), Color.parseColor("#2A2233"), Shader.TileMode.CLAMP));
        canvas.drawRect(0, height / 2, width, height, floorPaint);

        // 2. Перспективные стены 2.5D (Трапеции коридора)
        float vanishingX = width / 2f;
        float vanishingY = height / 2f;
        float horizonSize = width * 0.18f;

        // Левая стена
        Path leftWall = new Path();
        leftWall.moveTo(0, 0);
        leftWall.lineTo(vanishingX - horizonSize, vanishingY - horizonSize * 0.6f);
        leftWall.lineTo(vanishingX - horizonSize, vanishingY + horizonSize * 0.6f);
        leftWall.lineTo(0, height);
        leftWall.close();

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#2B2336"));
        canvas.drawPath(leftWall, paint);

        // Правая стена
        Path rightWall = new Path();
        rightWall.moveTo(width, 0);
        rightWall.lineTo(vanishingX + horizonSize, vanishingY - horizonSize * 0.6f);
        rightWall.lineTo(vanishingX + horizonSize, vanishingY + horizonSize * 0.6f);
        rightWall.lineTo(width, height);
        rightWall.close();

        paint.setColor(Color.parseColor("#231C2C"));
        canvas.drawPath(rightWall, paint);

        // Линии швов каменных блоков на стенах (эффект объема)
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(Color.parseColor("#3D314A"));
        canvas.drawPath(leftWall, paint);
        canvas.drawPath(rightWall, paint);

        // Дальняя арка коридора (проход вперед)
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#0A080E"));
        canvas.drawRect(vanishingX - horizonSize, vanishingY - horizonSize * 0.6f, vanishingX + horizonSize, vanishingY + horizonSize * 0.6f, paint);
    }

    // Отрисовка Анимированного Монстра от 1-го лица
    public static void draw25DMonster(Canvas canvas, float width, float height, int monsterLevel, float animFrame, boolean isHit) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // Анимация «дыхания» и покачивания от 1-го лица
        float hoverY = (float) Math.sin(animFrame * 0.08) * 12f;
        float scalePulse = 1.0f + (float) Math.sin(animFrame * 0.05) * 0.03f;

        float cx = width / 2f;
        float cy = height / 2f + hoverY;
        float baseSize = (width * 0.28f) * scalePulse;

        // Вспышка при получении урона
        if (isHit) {
            paint.setColor(Color.WHITE);
            canvas.drawCircle(cx, cy, baseSize * 1.1f, paint);
        }

        // 1. Аура и тело
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#50FF0000"));
        canvas.drawCircle(cx, cy, baseSize * 0.85f, paint);

        paint.setColor(Color.parseColor("#7A0C0C"));
        canvas.drawCircle(cx, cy, baseSize * 0.6f, paint);

        // 2. Рога и детали монстра
        Path hornPath = new Path();
        hornPath.moveTo(cx - baseSize * 0.4f, cy - baseSize * 0.2f);
        hornPath.lineTo(cx - baseSize * 0.7f, cy - baseSize * 0.9f);
        hornPath.lineTo(cx - baseSize * 0.1f, cy - baseSize * 0.4f);
        hornPath.close();

        hornPath.moveTo(cx + baseSize * 0.4f, cy - baseSize * 0.2f);
        hornPath.lineTo(cx + baseSize * 0.7f, cy - baseSize * 0.9f);
        hornPath.lineTo(cx + baseSize * 0.1f, cy - baseSize * 0.4f);
        hornPath.close();

        paint.setColor(Color.parseColor("#D4AF37"));
        canvas.drawPath(hornPath, paint);

        // 3. Светящиеся глаза монстра
        paint.setColor(Color.YELLOW);
        canvas.drawCircle(cx - baseSize * 0.22f, cy - baseSize * 0.12f, baseSize * 0.12f, paint);
        canvas.drawCircle(cx + baseSize * 0.22f, cy - baseSize * 0.12f, baseSize * 0.12f, paint);

        paint.setColor(Color.RED);
        canvas.drawCircle(cx - baseSize * 0.22f, cy - baseSize * 0.12f, baseSize * 0.05f, paint);
        canvas.drawCircle(cx + baseSize * 0.22f, cy - baseSize * 0.12f, baseSize * 0.05f, paint);
    }

    // Отрисовка Сундука от 1-го лица
    public static void draw25DChest(Canvas canvas, float width, float height, float animFrame) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        float hoverY = (float) Math.sin(animFrame * 0.05) * 6f;
        float cx = width / 2f;
        float cy = height / 2f + 40f + hoverY;
        float w = width * 0.3f;
        float h = width * 0.2f;

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#7A3D09"));
        RectF rect = new RectF(cx - w / 2, cy - h / 2, cx + w / 2, cy + h / 2);
        canvas.drawRoundRect(rect, 12, 12, paint);

        paint.setColor(Color.parseColor("#FFD700"));
        canvas.drawRect(cx - w / 2, cy - 6, cx + w / 2, cy + 6, paint);
        canvas.drawCircle(cx, cy, 14, paint);
    }

    // Отрисовка Святыни от 1-го лица
    public static void draw25DShrine(Canvas canvas, float width, float height, float animFrame) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        float hoverY = (float) Math.sin(animFrame * 0.06) * 10f;
        float cx = width / 2f;
        float cy = height / 2f + hoverY;
        float size = width * 0.22f;

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#4000FFFF"));
        canvas.drawCircle(cx, cy, size * 1.2f, paint);

        Path path = new Path();
        path.moveTo(cx, cy - size);
        path.lineTo(cx + size * 0.6f, cy);
        path.lineTo(cx, cy + size);
        path.lineTo(cx - size * 0.6f, cy);
        path.close();

        paint.setColor(Color.parseColor("#00FFFF"));
        canvas.drawPath(path, paint);
    }

    // Отрисовка Оружия/Рук героя внизу экрана (вид от первого лица)
    public static void drawHeroFirstPersonWeapon(Canvas canvas, float width, float height, String heroClass, float animFrame) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        float bobbingX = (float) Math.sin(animFrame * 0.05) * 8f;
        float bobbingY = (float) Math.abs(Math.cos(animFrame * 0.05)) * 10f;

        if (heroClass.equals("Воин")) {
            // Щит Воина слева внизу
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.parseColor("#808080"));
            RectF shield = new RectF(20 + bobbingX, height - 220 + bobbingY, 180 + bobbingX, height + 40);
            canvas.drawRoundRect(shield, 20, 20, paint);

            paint.setColor(Color.parseColor("#FFD700"));
            canvas.drawCircle(100 + bobbingX, height - 110 + bobbingY, 30, paint);
        } else if (heroClass.equals("Маг")) {
            // Магический Посох справа внизу
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.parseColor("#5C4033"));
            canvas.drawRect(width - 120 + bobbingX, height - 300 + bobbingY, width - 80 + bobbingX, height, paint);

            // Набалдашник с анимированным голубым свечением
            paint.setColor(Color.parseColor("#00BFFF"));
            canvas.drawCircle(width - 100 + bobbingX, height - 320 + bobbingY, 28 + (float) Math.sin(animFrame * 0.1) * 4f, paint);
        } else {
            // Кинжалы Разбойника по бокам
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.parseColor("#C0C0C0"));

            Path blade = new Path();
            blade.moveTo(width - 140 + bobbingX, height);
            blade.lineTo(width - 80 + bobbingX, height - 180 + bobbingY);
            blade.lineTo(width - 40 + bobbingX, height);
            blade.close();

            canvas.drawPath(blade, paint);
        }
    }
}
