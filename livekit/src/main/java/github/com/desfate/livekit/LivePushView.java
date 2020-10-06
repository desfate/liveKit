package github.com.desfate.livekit;

import android.content.Context;
import android.hardware.camera2.params.MeteringRectangle;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import javax.microedition.khronos.egl.EGLConfig;

import github.com.desfate.livekit.camera.CameraControl;
import github.com.desfate.livekit.camera.view.FocusView;
import github.com.desfate.livekit.live.LiveManager;
import github.com.desfate.livekit.reders.CameraDrawer;
import github.com.desfate.livekit.utils.JobExecutor;
import github.com.desfate.livekit.utils.ScreenUtils;

/**
 * 基于GLSurfaceView的主播预览页面
 */
public class LivePushView extends BaseLiveView{
    CameraControl cameraControl; //       相机管理
    FocusView mFocusView; //              对焦View  这个view是要加入LivePlayView的父布局
    private CameraDrawer mDrawer; //      opengl渲染代码
    private JobExecutor mJobExecutor;//   线程池


    public LivePushView(Context context) {
        super(context);
    }


    public LivePushView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mJobExecutor = new JobExecutor();
        mFocusView = new FocusView(context);
        mFocusView.setVisibility(View.GONE);
        cameraControl = new CameraControl(context, this, mFocusView);
    }


    @Override
    public void surfaceCreated(EGLConfig config) {
        mDrawer = new CameraDrawer();
        cameraControl.openCamera((front, size) -> {
            int realWidth = ScreenUtils.getScreenSize(getContext()).getWidth();;
            int realHeight = realWidth * size.getWidth() / size.getHeight();;
            mJobExecutor.execute(new JobExecutor.Task<Void>() {
                @Override
                public void onMainThread(Void result) {
                    super.onMainThread(result);
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(realWidth, realHeight);
                    LivePushView.this.setLayoutParams(layoutParams);
                    // 重新设定对焦区域大小
                    mFocusView.initFocusArea(realWidth, realHeight);
                    cameraControl.focusChanged(realWidth,realHeight);
                }
            });
        });
    }


    @Override
    public void onClick(float X, float Y) {
        // 点击页面
        // 这里应该处理对焦逻辑
        cameraControl.startFocus(X, Y);
        MeteringRectangle focusRect = cameraControl.getFocusArea(X, Y, true);
        MeteringRectangle meterRect = cameraControl.getFocusArea(X, Y, false);
        cameraControl.cameraAFSetting(focusRect, meterRect);
    }


    @Override
    public void onDrawFrame(int surfaceTexture) {
        mDrawer.draw(surfaceTexture, cameraControl.getCameraStata());
    }


    public void switchCamera(){
        cameraControl.switchCamera();
    }

    /**
     * 把对焦动画的view 加入父布局
     * @param viewGroup
     */
    public void setParentLayout(ViewGroup viewGroup){
        viewGroup.addView(mFocusView);
    }

    /**
     * 设置推流回调
     * @param mLiveManager
     */
    public void setLivePushListener(LiveManager mLiveManager){
        cameraControl.setmLiveManager(mLiveManager);
    }


    public void release(){
        cameraControl.release();
        mJobExecutor = null;
    }

    public void startPush(){
        cameraControl.startPush();
    }

}
