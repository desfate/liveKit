package github.com.desfate.livekit;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Size;
import android.widget.RelativeLayout;

import javax.microedition.khronos.egl.EGLConfig;

import github.com.desfate.livekit.camera.interfaces.CameraChangeCallback;
import github.com.desfate.livekit.reders.CameraDrawer;
import github.com.desfate.livekit.utils.JobExecutor;
import github.com.desfate.livekit.utils.ScreenUtils;


/**
 * 基于GLSurfaceView的观众观看页面
 */
public class LivePlayView extends BaseLiveView {

    private boolean isFront = true; // true: 前置  false: 后置

    private CameraDrawer mDrawer; //                    opengl渲染代码
    private CameraChangeCallback callBack;//            数据返回
    private JobExecutor mJobExecutor;//   线程池

    public LivePlayView(Context context) {
        super(context);
    }

    public LivePlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mJobExecutor = new JobExecutor();
        callBack = new CameraChangeCallback() {
            @Override
            public void viewChanged(boolean front, Size size) {
                getmSurfaceTexture().setDefaultBufferSize(size.getWidth(), size.getHeight());
                final int realWidth = ScreenUtils.getScreenSize(getContext()).getWidth();
                final int realHeight = realWidth * size.getWidth() / size.getHeight();
                mJobExecutor.execute(new JobExecutor.Task<Void>() {
                    @Override
                    public void onMainThread(Void result) {
                        super.onMainThread(result);
                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(realWidth, realHeight);
                        LivePlayView.this.setLayoutParams(layoutParams);
                    }
                });
            }
        };
    }

    @Override
    public void surfaceCreated(EGLConfig config) {
        mDrawer = new CameraDrawer();
    }

    @Override
    public void onClick(float X, float Y) {

    }

    /**
     * 当视频大小有改变是 view的大小也要跟着变化
     * @return
     */
    public CameraChangeCallback getVideoChange(){
        return callBack;
    }

    @Override
    public void onDrawFrame(int mSurfaceId) {
        mDrawer.draw(mSurfaceId, isFront, getWidth(), getHeight());
    }

    /**
     * 主播摄像头切换 本地进行变换
      * @param change
     */
    public void setFrontChange(boolean change){
        this.isFront = change;
    }
}
