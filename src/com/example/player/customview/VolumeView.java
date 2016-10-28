package com.example.player.customview;

import com.example.mediaplayerdemo.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * 声音、亮度调节环形进度条
 *
 * @author makai 2015年2月6日上午11:30:30
 */
public class VolumeView extends View {

    /**
     * 是否改变图标
     */
    private boolean tag = false;
    // 半径
    float r1 = 0;
    float r2 = 0;
    float r3 = 0;
    // 外圆宽度
    float w1 = 3;
    // 内圆宽度
    float w2 = 6;
    Paint paint;

    // 进度
    float progress = 0;
    // 最大值
    int max;
    Bitmap bitmap;
    // 用于定义的圆弧的形状和大小的界限
    RectF oval;
    /**
     * 显示文本
     */
    private String text = null;

    public VolumeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public VolumeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VolumeView(Context context) {
        super(context);
        init(context);
    }

    void init(Context context) {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Style.STROKE);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        float cx = getMeasuredWidth() / 3;
        float cy = getMeasuredHeight() / 3;
        r1 = cx - w1 / 2;
        r2 = cx - w1 / 2 - w2 / 2;
        r3 = cx - w1 / 2 - w2;

        // 绘制外圆
//		paint.setStrokeWidth(w1);
//		paint.setColor(Color.parseColor("#454547"));
//		canvas.drawCircle(cx, cy, r1, paint);

        // 绘制中间圆环
//		paint.setColor(Color.parseColor("#29b6fd"));
//		paint.setStrokeWidth(w2);
//		canvas.drawCircle(cx, cy, r2, paint);

        // 绘制中间的图片
        canvas.drawBitmap(bitmap, cx - bitmap.getWidth() / 2,
                (cx - bitmap.getHeight() / 2), paint);

        // 绘制背景
        paint.setColor(Color.parseColor("#464648"));
        paint.setStyle(Style.FILL);
        paint.setAlpha(80);
        canvas.drawCircle(cx, cy, r1, paint);


        // 绘制文本
//		paint.setColor(Color.WHITE);
//		paint.setStrokeWidth(0);
//		paint.setTextSize(14);
//		float textWidth = paint.measureText(text); // 测量字体宽度，我们需要根据字体的宽度设置在圆环中间

//		canvas.drawText(text, cx - textWidth / 2, cx + bitmap.getHeight() / 2
//				+ 14, paint);

        // 绘制进度
        paint.setStyle(Style.STROKE);
        paint.setStrokeWidth(w2);
        paint.setColor(Color.parseColor("#29b6fd"));
//		paint.setColor(Color.WHITE);
        if (oval == null) {
            oval = new RectF(cx - r2, cy - r2, cx + r2, cy + r2);
        }
        canvas.drawArc(oval, 270, 360 * progress / max, false, paint);

        super.onDraw(canvas);
    }

    /**
     * 设置进度(0-max)
     *
     * @param progress
     */
    public void setProgress(float progress) {
        //TODO bitmap 判断逻辑冗余，简化处理
        if (text.equals("音量")) {
            bitmap = BitmapFactory.decodeResource(getResources(),
                    R.drawable.ring_on);
        } else {
            bitmap = BitmapFactory.decodeResource(getResources(),
                    R.drawable.light);
        }
        this.progress = progress;
        if (this.progress >= max) {
            this.progress = max;
        }
        if (this.progress <= 0) {
            this.progress = 0;
        }
        if (this.progress == 0) {
            if (text.equals("音量")) {
                bitmap = BitmapFactory.decodeResource(getResources(),
                        R.drawable.ring_shut);
                tag = true;
            }
        } else {
            if (text.equals("音量") && tag) {
                bitmap = BitmapFactory.decodeResource(getResources(),
                        R.drawable.ring_on);
                tag = false;
            }
        }
        //刷新界面
        postInvalidate();
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setMax(int max) {
        this.max = max;
    }
}
