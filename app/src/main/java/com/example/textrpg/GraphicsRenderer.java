package com.example.textrpg;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

public class GraphicsRenderer {

    // Отрисовка пола и стен лабиринта
    public static void drawDungeonTile(Canvas canvas, float x, float y, float size, boolean isWall) {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);

        if (isWall) {
            // Каменная стена с 3D-объемом
            paint.setColor(Color.parseColor("#2B2625"));
            canvas.drawRect(x, y, x + size, y + size, paint);

            paint.setColor(Color.parseColor("#4A4240"));
            canvas.drawRect(x + 4, y + 4, x + size - 4, y + size - 4, paint);

            paint.setColor(Color.parseColor("#1C1918"));
            canvas.drawRect(x + 8, y + 8, x + size - 8, y + size - 8, paint);
        } else {
            // Каменный пол
            paint.setColor(Color.parseColor("#1A181C"));
            canvas.drawRect(x, y, x + size, y + size, paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);
            paint.setColor(Color.parseColor("#2C2830"));
            canvas.drawRect(x, y, x + size, y + size, paint);
        }
    }

    // Векторная отрисовка Героя через код
    public static void drawHero(Canvas canvas, float cx, float cy, float radius, String heroClass) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // Тень героя
        paint.setColor(Color.parseColor("#80000000"));
        canvas.drawOval(cx - radius, cy + radius * 0.5f, cx + radius, cy + radius * 0.9f, paint);

        // Тело персонажа
        paint.setStyle(Paint.Style.FILL);
        if (heroClass.equals("Воин")) {
            paint.setColor(Color.parseColor("#C0C0C0")); // Серебряные доспехи
        } else if (heroClass.equals("Маг")) {
            paint.setColor(Color.parseColor("#4169E1")); // Синяя мантия
        } else {
            paint.setColor(Color.parseColor("#2E8B57")); // Зеленый плащ разбойника
        }
        canvas.drawCircle(cx, cy, radius, paint);

        // Шлем / Оперение / Детали
        paint.setColor(Color.parseColor("#FFD700")); // Золотые украшения
        canvas.drawCircle(cx, cy - radius * 0.3f, radius * 0.4f, paint);

        // Глаза
        paint.setColor(Color.WHITE);
        canvas.drawCircle(cx - radius * 0.3f, cy - radius * 0.1f, radius * 0.2f, paint);
        canvas.drawCircle(cx + radius * 0.3f, cy - radius * 0.1f, radius * 0.2f, paint);

        paint.setColor(Color.BLACK);
        canvas.drawCircle(cx - radius * 0.3f, cy - radius * 0.1f, radius * 0.08f, paint);
        canvas.drawCircle(cx + radius * 0.3f, cy - radius * 0.1f, radius * 0.08f, paint);
    }

    // Процедурный вывод Графического Монстра
    public static void drawMonster(Canvas canvas, float cx, float cy, float size, int monsterLevel) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // Аура силы монстра в зависимости от уровня
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#40FF0000"));
        canvas.drawCircle(cx, cy, size * 0.7f, paint);

        // Тело монстра (Красный/Фиолетовый)
        paint.setColor(Color.parseColor("#8B0000"));
        canvas.drawCircle(cx, cy, size * 0.5f, paint);

        // Рога (Треугольные пути)
        Path path = new Path();
        path.moveTo(cx - size * 0.4f, cy - size * 0.2f);
        path.lineTo(cx - size * 0.6f, cy - size * 0.7f);
        path.lineTo(cx - size * 0.1f, cy - size * 0.4f);
        path.close();

        path.moveTo(cx + size * 0.4f, cy - size * 0.2f);
        path.lineTo(cx + size * 0.6f, cy - size * 0.7f);
        path.lineTo(cx + size * 0.1f, cy - size * 0.4f);
        path.close();

        paint.setColor(Color.parseColor("#DAA520"));
        canvas.drawPath(path, paint);

        // Светящиеся зловещие глаза
        paint.setColor(Color.YELLOW);
        canvas.drawCircle(cx - size * 0.2f, cy - size * 0.1f, size * 0.12f, paint);
        canvas.drawCircle(cx + size * 0.2f, cy - size * 0.1f, size * 0.12f, paint);

        paint.setColor(Color.RED);
        canvas.drawCircle(cx - size * 0.2f, cy - size * 0.1f, size * 0.05f, paint);
        canvas.drawCircle(cx + size * 0.2f, cy - size * 0.1f, size * 0.05f, paint);
    }

    // Отрисовка Золотого Сундука
    public static void drawChest(Canvas canvas, float cx, float cy, float size) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // Корпус сундука (Дерево)
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#8B4513"));
        RectF rect = new RectF(cx - size * 0.4f, cy - size * 0.3f, cx + size * 0.4f, cy + size * 0.3f);
        canvas.drawRoundRect(rect, 8, 8, paint);

        // Золотые полосы и замок
        paint.setColor(Color.parseColor("#FFD700"));
        canvas.drawRect(cx - size * 0.4f, cy - size * 0.05f, cx + size * 0.4f, cy + size * 0.05f, paint);
        canvas.drawCircle(cx, cy, size * 0.08f, paint);
    }

    // Отрисовка Магической Святыни
    public static void drawShrine(Canvas canvas, float cx, float cy, float size) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // Голубое магическое свечение
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#3000FFFF"));
        canvas.drawCircle(cx, cy, size * 0.8f, paint);

        // Кристалл
        Path path = new Path();
        path.moveTo(cx, cy - size * 0.5f);
        path.lineTo(cx + size * 0.3f, cy);
        path.lineTo(cx, cy + size * 0.5f);
        path.lineTo(cx - size * 0.3f, cy);
        path.close();

        paint.setColor(Color.parseColor("#00FFFF"));
        canvas.drawPath(path, paint);
    }
}
