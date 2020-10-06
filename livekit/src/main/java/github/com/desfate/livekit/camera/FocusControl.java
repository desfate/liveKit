package github.com.desfate.livekit.camera;

import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.params.MeteringRectangle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;
import java.util.Optional;

import github.com.desfate.livekit.camera.interfaces.FocusCallback;
import github.com.desfate.livekit.camera.view.FocusView;
import github.com.desfate.livekit.utils.CoordinateTransformer;


/**
 * 用于管理相机手动对焦的类
 */
public class FocusControl {

    private static final int HIDE_FOCUS_DELAY = 1600;
    private static final int DELAY_MILLIS = 800;
    private static final int MSG_HIDE_FOCUS = 0x10;

    private FocusView mFocusView; //                对焦动画
    private Looper mLooper; //                      主线程Looper
    private Handler mHandler;//                     主线程回调
    private Rect mFocusRect;//                      点击区域矩阵
    private Rect mPreviewRect;//                    默认区域
    private float currentX, currentY;//             点击的对焦点

    private CoordinateTransformer mTransformer;//   坐标变换
    private FocusCallback mFocusCallback;//         对焦动画结束回调

    public FocusControl(FocusView view, Looper looper, FocusCallback mFocusCallback){
        this.mFocusView = view;
        this.mLooper = looper;
        this.mFocusCallback = mFocusCallback;
        mHandler = new MainHandler(this, mLooper);
        mFocusRect = new Rect();
    }

    public void startFocus(float x, float y) {
        currentX = x;
        currentY = y;
        mHandler.removeMessages(MSG_HIDE_FOCUS);
        mFocusView.moveToPosition(x, y);
        mHandler.sendEmptyMessageDelayed(MSG_HIDE_FOCUS, HIDE_FOCUS_DELAY);
    }

    public void startFocus() {
        mHandler.removeMessages(MSG_HIDE_FOCUS);
        mFocusView.startFocus();
        mHandler.sendEmptyMessageDelayed(MSG_HIDE_FOCUS, HIDE_FOCUS_DELAY);
    }

    public void focusFailed(){
        mFocusView.focusFailed();
    }

    public void focusSuccess(){
        mFocusView.focusSuccess();
    }

    public void hideFocusView(){
        mFocusView.hideFocusUI();
    }

    /**
     * 自动对焦
     */
    public void autoFocus() {
        mHandler.removeMessages(MSG_HIDE_FOCUS);
        mFocusView.resetToDefaultPosition();
        mFocusView.startFocus();
        mHandler.sendEmptyMessageDelayed(MSG_HIDE_FOCUS, DELAY_MILLIS);
    }


    public void onPreviewChanged(int width, int height, CameraCharacteristics c) {
        mPreviewRect = new Rect(0, 0, width, height);
        mTransformer = new CoordinateTransformer(c, new RectF(mPreviewRect));
    }


    public MeteringRectangle getFocusArea(float x, float y, boolean isFocusArea) {
        currentX = x;
        currentY = y;
        if (isFocusArea) {
            return calcTapAreaForCamera2(mPreviewRect.width() / 5, DELAY_MILLIS);
        } else {
            return calcTapAreaForCamera2(mPreviewRect.width() / 4, DELAY_MILLIS);
        }
    }


    private MeteringRectangle calcTapAreaForCamera2(int areaSize, int weight) {
        int left = clamp((int) currentX - areaSize / 2,
                mPreviewRect.left, mPreviewRect.right - areaSize);
        int top = clamp((int) currentY - areaSize / 2,
                mPreviewRect.top, mPreviewRect.bottom - areaSize);
        RectF rectF = new RectF(left, top, left + areaSize, top + areaSize);
        toFocusRect(mTransformer.toCameraSpace(rectF));
        return new MeteringRectangle(mFocusRect, weight);
    }

    private int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    private void toFocusRect(RectF rectF) {
        mFocusRect.left = Math.round(rectF.left);
        mFocusRect.top = Math.round(rectF.top);
        mFocusRect.right = Math.round(rectF.right);
        mFocusRect.bottom = Math.round(rectF.bottom);
    }


    private static class MainHandler extends Handler {
        final WeakReference<FocusControl> mControl;

        MainHandler(FocusControl manager, Looper looper) {
            super(looper);
            mControl = new WeakReference<>(manager);
        }


        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mControl.get() == null) {
                return;
            }
            switch (msg.what) {
                case MSG_HIDE_FOCUS:
                    mControl.get().mFocusView.resetToDefaultPosition();
                    mControl.get().mFocusView.hideFocusUI();
                    if(mControl.get().mFocusCallback != null){
                        mControl.get().mFocusCallback.focusFinish();
                    }
//                    Optional.ofNullable(mControl.get().mFocusCallback).ifPresent(focusInterface -> focusInterface.focusFinish());
                    break;
            }
        }
    }
}
