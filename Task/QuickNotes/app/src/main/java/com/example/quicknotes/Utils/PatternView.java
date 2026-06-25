package com.example.quicknotes.Utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.quicknotes.R;

import java.util.ArrayList;
import java.util.List;

public class PatternView extends View {

    private static final int GRID_SIZE = 3;
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<Dot> dots = new ArrayList<>();
    private final List<Dot> selectedDots = new ArrayList<>();
    private final Path linePath = new Path();

    private float lastX, lastY;
    private boolean isDrawing = false;
    private boolean isError = false;
    private OnPatternListener listener;

    public interface OnPatternListener {
        void onPatternComplete(List<Integer> pattern);
    }

    public PatternView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        dotPaint.setStyle(Paint.Style.FILL);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        linePaint.setStrokeWidth(12f);
        resetColors();
    }

    private void resetColors() {
        dotPaint.setColor(ContextCompat.getColor(getContext(), R.color.gray_version));
        linePaint.setColor(ContextCompat.getColor(getContext(), R.color.primary_blue));
    }

    public void setNormal() {
        isError = false;
        linePaint.setColor(ContextCompat.getColor(getContext(), R.color.primary_blue));
        invalidate();
    }

    public void setError() {
        isError = true;
        linePaint.setColor(ContextCompat.getColor(getContext(), R.color.badge_untitled_red_text));
        invalidate();
    }

    public void clearPattern() {
        selectedDots.clear();
        isDrawing = false;
        isError = false;
        resetColors();
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        dots.clear();
        float cellWidth = (float) w / GRID_SIZE;
        float cellHeight = (float) h / GRID_SIZE;
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                dots.add(new Dot(j * cellWidth + cellWidth / 2, i * cellHeight + cellHeight / 2, i * GRID_SIZE + j));
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw lines
        if (!selectedDots.isEmpty()) {
            linePath.reset();
            linePath.moveTo(selectedDots.get(0).x, selectedDots.get(0).y);
            for (int i = 1; i < selectedDots.size(); i++) {
                linePath.lineTo(selectedDots.get(i).x, selectedDots.get(i).y);
            }
            if (isDrawing && !isError) {
                linePath.lineTo(lastX, lastY);
            }
            canvas.drawPath(linePath, linePaint);
        }

        // Draw dots
        for (Dot dot : dots) {
            boolean isSelected = selectedDots.contains(dot);
            if (isSelected) {
                if (isError) {
                    dotPaint.setColor(ContextCompat.getColor(getContext(), R.color.badge_untitled_red_text));
                } else {
                    dotPaint.setColor(ContextCompat.getColor(getContext(), R.color.primary_blue));
                }
            } else {
                dotPaint.setColor(ColorUtils.setAlphaComponent(ContextCompat.getColor(getContext(), R.color.gray_version), 100));
            }
            canvas.drawCircle(dot.x, dot.y, isSelected ? 24 : 20, dotPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) return false;

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                clearPattern();
                checkDot(x, y);
                isDrawing = true;
                break;
            case MotionEvent.ACTION_MOVE:
                if (isDrawing) {
                    checkDot(x, y);
                    lastX = x;
                    lastY = y;
                }
                break;
            case MotionEvent.ACTION_UP:
                isDrawing = false;
                if (listener != null && !selectedDots.isEmpty()) {
                    List<Integer> pattern = new ArrayList<>();
                    for (Dot dot : selectedDots) pattern.add(dot.id);
                    listener.onPatternComplete(pattern);
                }
                break;
        }
        invalidate();
        return true;
    }

    private void checkDot(float x, float y) {
        for (Dot dot : dots) {
            if (Math.hypot(dot.x - x, dot.y - y) < 80) {
                if (!selectedDots.contains(dot)) {
                    selectedDots.add(dot);
                }
                break;
            }
        }
    }

    public void setOnPatternListener(OnPatternListener listener) {
        this.listener = listener;
    }

    private static class Dot {
        float x, y;
        int id;
        Dot(float x, float y, int id) { this.x = x; this.y = y; this.id = id; }
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Dot) return ((Dot) obj).id == id;
            return false;
        }
    }
}

class ColorUtils {
    public static int setAlphaComponent(int color, int alpha) {
        if (alpha < 0 || alpha > 255) throw new IllegalArgumentException("alpha must be between 0 and 255.");
        return (color & 0x00ffffff) | (alpha << 24);
    }
}
