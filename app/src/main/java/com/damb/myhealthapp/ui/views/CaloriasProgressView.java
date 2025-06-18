package com.damb.myhealthapp.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

public class CaloriasProgressView extends View {
    private int calorias = 0;
    private int meta = 500;
    private Paint fondoPaint, progresoPaint, textoPaint;
    private RectF rectF;

    public CaloriasProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        fondoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fondoPaint.setColor(0xFFE0E0E0); // gris claro
        fondoPaint.setStyle(Paint.Style.STROKE);
        fondoPaint.setStrokeWidth(32f);

        progresoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progresoPaint.setStyle(Paint.Style.STROKE);
        progresoPaint.setStrokeWidth(32f);
        progresoPaint.setStrokeCap(Paint.Cap.ROUND);

        textoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textoPaint.setColor(0xFF222222);
        textoPaint.setTextSize(64f);
        textoPaint.setTextAlign(Paint.Align.CENTER);

        rectF = new RectF();
    }

    public void setCalorias(int calorias) {
        this.calorias = calorias;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth();
        float height = getHeight();
        float size = Math.min(width, height) * 0.8f;
        float left = (width - size) / 2;
        float top = (height - size) / 2;
        float right = left + size;
        float bottom = top + size;
        rectF.set(left, top, right, bottom);

        // Fondo
        canvas.drawArc(rectF, 0, 360, false, fondoPaint);

        // Color según porcentaje
        float percent = Math.min(1f, calorias / (float)meta);
        if (percent >= 0.8f) {
            progresoPaint.setColor(0xFF4CAF50); // verde
        } else if (percent >= 0.5f) {
            progresoPaint.setColor(0xFFFFA000); // naranja
        } else {
            progresoPaint.setColor(0xFFD32F2F); // rojo
        }

        // Progreso
        canvas.drawArc(rectF, -90, percent * 360, false, progresoPaint);

        // Texto de calorías
        String texto = calorias + " kcal";
        canvas.drawText(texto, width / 2, height / 2 + 24, textoPaint);
    }
} 