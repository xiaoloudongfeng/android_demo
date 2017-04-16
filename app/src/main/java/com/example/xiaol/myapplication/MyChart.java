package com.example.xiaol.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.method.Touch;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by xiaolou on 2017/2/28.
 */

public class MyChart extends View {
    private final int XRemain = 60;             // X轴空白
    private final int YRemain = 50;            // Y轴空白

    private String Xstr = "min";
    private String Ystr = "℃";
    private int XLimit = 30;                    // 30 * 1min = 1h
    private int YLimit = 30;                    // 5℃ - 35℃
    private float YScale;                       // Y轴刻度，计算方法为：YLength / YLimit
    private float XScale;                       // X轴刻度，计算方法为：XLength / XLimit
    private int XLength;                        // 实际X轴长度，计算方法为：(w - 2 * XRemain)
    private int YLength;                        // 实际Y轴长度，计算方法为：(h - 2 * YRemain)
    private int YInterval = 5;

    private float XTouch;
    private float YTouch;
    private String XTouchStr;
    private boolean TouchFlag;

    private List<Float> tempData = new ArrayList<Float>();

    public MyChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = getDefaultSize(0, widthMeasureSpec);
        int h = getDefaultSize(0, heightMeasureSpec);

        XLength = w - 2 * XRemain;
        YLength = h - 2 * YRemain;

        XScale = XLength / (float)XLimit;
        YScale = YLength / (float)YLimit;

        Log.e("MyChart", "XLength: " + XLength + "YLength: " + YLength);
        Log.e("MyChart", "XScale: " + XScale);
        Log.e("MyChart", "YScale: " + YScale);

        setMeasuredDimension(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setColor(0x26b4b4b4);
        paint.setStrokeWidth(3);
        paint.setTextSize(25);

        Paint paint2 = new Paint();
        paint2.setStyle(Paint.Style.FILL);
        paint2.setAntiAlias(true);
        paint2.setColor(0x89696969);
        paint2.setStrokeWidth(3);
        paint2.setTextSize(30);

        // 添加温度刻度和文字
        canvas.drawText(Ystr, XRemain, YRemain, paint2);
        for (int i = 0; i * YScale <= YLength; i++) {
            if (i % YInterval == 0) {
                canvas.drawText(String.valueOf(i + 5), XRemain * 0.2f, YLength - i * YScale + 5 + YRemain, paint2);
                canvas.drawLine(XRemain, YLength - i * YScale + YRemain, XLength + XRemain, YLength - i * YScale + YRemain, paint);
            }
        }

        // 添加时间刻度和文字
        canvas.drawText(Xstr, (XRemain + XLength) * 0.98f, (YRemain + YLength) * 1.03f, paint2);
        for (int i = 0; i * XScale <= XLength; i++) {
            if (i % 5 == 0) {
                if (i !=0 && i * XScale != XLength) {
                    canvas.drawText(String.valueOf(i), XRemain * 0.7f + i * XScale, (YLength + YRemain) * 1.04f, paint2);
                }
                canvas.drawLine(XRemain + i * XScale, YLength + YRemain, XRemain + i * XScale, YRemain, paint);
            }
        }

        // 绘制折线图
        paint.setColor(0x92C92121);
        paint.setStrokeWidth(3);
        paint.setTextSize(25);
        if (tempData.size() > 1) {
            for (int i = 1; i < tempData.size(); i++) {
                canvas.drawLine((i - 1) * XScale + XRemain, YRemain + YLength - (tempData.get(i - 1) - 5f) * YScale,
                        i * XScale + XRemain, YRemain + YLength - (tempData.get(i) - 5f) * YScale, paint);
                canvas.drawCircle(i * XScale + XRemain, YRemain + YLength - (tempData.get(i) - 5f) * YScale, 3, paint);
            }
        }

        if (TouchFlag) {
            TouchFlag = !TouchFlag;

            int strlen = XTouchStr.length() * 20;
            float x = XTouch + strlen > XLength ? XTouch - strlen : XTouch + 10;
            canvas.drawText(XTouchStr, x, YTouch - 10, paint2);

            paint2.setColor(0x47C92121);
            canvas.drawCircle(XTouch, YTouch, 25, paint2);

            paint2.setColor(0xFFFFFFFF);
            canvas.drawCircle(XTouch, YTouch, 7, paint2);

            paint2.setColor(0xFF7CB345);
            canvas.drawCircle(XTouch, YTouch, 4, paint2);

            paint2.setColor(0x3C1C7BB6);
            paint2.setStrokeWidth(5);
            canvas.drawLine(XTouch, YLength + YRemain, XTouch, YRemain, paint2);
        }
    }

    public void onTempRefresh(float temp) {
        tempData.add(temp);

        // 画折线图，需要XLimit + 1个数据
        if (tempData.size() > XLimit + 1) {
            tempData.remove(0);
        }

        Log.e("onRefresh", String.valueOf(temp));

        this.invalidate();
    }

    public boolean onTouchEvent(MotionEvent event) {
        float rawX = event.getRawX();
        float rawY = event.getRawY();

        Log.e("onTouchEvent", "rawX: " + rawX + "rawY: " + rawY);

        if (rawX > XRemain && rawX <= XLength + XRemain) {
            int num = (int)Math.ceil((double)((rawX - XRemain) / XScale));

            Log.e("onTouchEvent", "num: " + num);

            if (tempData.size() > num) {
                XTouchStr = String.format(Locale.CHINA, "%.1f%s", tempData.get(num), Ystr);
                XTouch = num * XScale + XRemain;
                YTouch = YRemain + YLength - (tempData.get(num) - 5f) * YScale;
                TouchFlag = true;

            } else {
                XTouchStr = null;
                TouchFlag = false;
            }
        } else {
            TouchFlag = false;
        }

        this.invalidate();

        return true;
    }

    public void setYLimit(int ylimit) {
        YLimit = ylimit;
        YScale = YLength / (float)YLimit;

        this.invalidate();
    }

    public void setYStr(String ystr) {
        Ystr = ystr;

        this.invalidate();
    }

    public void setYInterval(int yInterval) {
        YInterval = yInterval;

        this.invalidate();
    }
}
