package github.com.desfate.livekit.ui;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.Nullable;

import github.com.desfate.livekit.CameraConstant;
import github.com.desfate.livekit.R;
import github.com.desfate.livekit.utils.ScreenUtils;


/**
 * 自定义对焦动画View
 */
public class FocusView extends View {

    private int radiusOuter, radiusInner, strokeWidth;  //                  外圈半径， 内圈半径， 圈本体的宽度
    private int colorSuccess, colorFailed, colorNormal, colorCurrent;  //   对焦成功， 对焦失败， 对焦中的颜色
    private RectF outerRectF, innerRectF; //                                外圈矩形和内圈矩形
    private ObjectAnimator animator; //                                     属性动画
    private Paint paint; //                                                 画笔

    private int previewWidth;
    private int previewHeight;

    public FocusView(Context context) {
        this(context, null, 0);
        initAnimation();
    }

    public FocusView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FocusView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Resources resources = context.getResources();
        radiusOuter = ScreenUtils.dpToPx(context, CameraConstant.radiusOuterSize);
        radiusInner = ScreenUtils.dpToPx(context, CameraConstant.radiusInnerSize);
        strokeWidth = ScreenUtils.dpToPx(context, CameraConstant.strokeWidthSize);

        colorFailed = Color.parseColor("#00CCFF");
        colorSuccess = Color.parseColor("#00CCFF");
        colorNormal = Color.parseColor("#00CCFF");
        colorCurrent = colorNormal;

        outerRectF = new RectF(strokeWidth, strokeWidth, radiusOuter * 2 - strokeWidth, radiusOuter * 2 - strokeWidth);
        innerRectF = new RectF(radiusOuter - radiusInner, radiusOuter - radiusInner, radiusOuter + radiusInner, radiusOuter + radiusInner);

        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);  //          设置样式 描边
        paint.setAntiAlias(true); //                     防锯齿
        paint.setStrokeWidth(strokeWidth);//             设置画笔宽度

        initAnimation();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(radiusOuter * 2, radiusOuter * 2);  // 设定这个View的大小 大小就是最外圈的直径
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setColor(colorCurrent);
        for (int i = 0; i < 4; i++) { // 画四段圆弧
            canvas.drawArc(outerRectF, 90 * i + 5, 80, false, paint);
            canvas.drawArc(innerRectF, 90 * i + 50, 80, false, paint);
        }
    }

    public void startFocus() {
        //this.setRotation(0);
        this.setVisibility(VISIBLE);
        colorCurrent = colorNormal;
        invalidate();
        setAnimator(0, 90, 500).start();
    }

    public void focusSuccess() {
        colorCurrent = colorSuccess;
        invalidate();
        setAnimator(90, 0, 200).start();
    }

    public void focusFailed() {
        colorCurrent = colorFailed;
        invalidate();
        setAnimator(180, 0, 200).start();
    }

    public ObjectAnimator setAnimator(float from, float to, long duration) {
        animator.cancel();
        animator.setFloatValues(from, to);
        animator.setDuration(duration);
        return animator;
    }

    private void initAnimation() {
        animator = new ObjectAnimator();
        animator.setTarget(this);
        animator.setPropertyName("rotation");
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
    }

    public void moveToPosition(float x, float y) {
        x -= radiusOuter;
        y -= radiusOuter;
        this.setTranslationX(x);
        this.setTranslationY(y);
        this.setVisibility(VISIBLE);
        colorCurrent = colorNormal;
        invalidate();
    }

    public void resetToDefaultPosition() {
        int x = previewWidth / 2 - radiusOuter;
        int y = previewHeight / 2 - radiusOuter;
        this.setTranslationX(x);
        this.setTranslationY(y);
    }

    public void hideFocusUI() {
        this.setVisibility(GONE);
    }

    public void initFocusArea(int width, int height) {
        previewWidth = width;
        previewHeight = height;
        resetToDefaultPosition();
    }
}
