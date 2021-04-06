package github.com.desfate.livekit.ui;

import android.content.Context;
import android.view.MotionEvent;

import github.com.desfate.livekit.utils.ScreenUtils;

/**
 * 我负责处理View的touch事件
 */
public class TouchControl {

    private final static long CLICK_TIME = 200;  // 200ms 如果这个值设置的特别大 则是长按

    private float mDownX;//                 手指按下的X轴
    private float mDownY;//                 手指按下的Y轴
    private long mTouchTime;//              抬起时的time
    private long mDownTime;//               按下时的time
    private final float mClickDistance;//        用于区分移动距离， 当前动作时点击还是滑动
    private final onClick onClickInterface;

    public TouchControl(Context context, onClick onClick) {
        mClickDistance = (float) ScreenUtils.getScreenSize(context).getWidth() / 20;
        onClickInterface = onClick;
    }

    public void onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownTime = System.currentTimeMillis();
                mDownX = event.getX();
                mDownY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                mTouchTime = System.currentTimeMillis() - mDownTime;
                detectGesture(mDownX, event.getX(), mDownY, event.getY());
                break;
        }
    }

    private void detectGesture(float downX, float upX, float downY, float upY) {
        float distanceX = upX - downX;
        float distanceY = upY - downY;
        if (Math.abs(distanceX) < mClickDistance
                && Math.abs(distanceY) < mClickDistance
                && mTouchTime < CLICK_TIME) {
            if (onClickInterface != null) {
                onClickInterface.onClicked(upX, upY);
            }
        }
    }

    public interface onClick {
        void onClicked(float upX, float upY);
    }

}
