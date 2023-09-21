package com.michaelaskew.avocadotimer.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class CircleChartView extends View {
    private double fractionElapsed;
    private Paint paint;

    public CircleChartView(Context context) {
        super(context);
        init(null);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        int radius = Math.min(width, height) / 2;

        Log.d("CircleChartView", "Avocado fraction " + fractionElapsed);
        if (fractionElapsed > 0.25) {
            paint.setColor(Color.YELLOW);
        } else if (fractionElapsed < -0.25) {
            paint.setColor(Color.rgb(139,69,19)); // Brown color
        } else {
            paint.setColor(Color.rgb(154,205,50)); // Avocado green color
        }

        canvas.drawCircle(width / 2, height / 2, radius, paint);

        paint.setColor(Color.WHITE);
        canvas.drawCircle(width / 3 + width / 6, height / 3 + height / 6, radius / 2, paint);

    }

    public void setFractionElapsed(double fractionElapsed) {
        this.fractionElapsed = fractionElapsed;
        invalidate();
    }

    // Constructor used when view is inflated from XML
    public CircleChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    // Optional constructor if you want default styling
    public CircleChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    // Optional constructor if you want default styling with API 21+
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CircleChartView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    // Common initialization method for all constructors
    private void init(AttributeSet attrs) {
        paint = new Paint();
        paint.setAntiAlias(true);
    }

}
