package github.com.desfate.livekit.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Size;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.constraintlayout.widget.ConstraintLayout;

import javax.microedition.khronos.egl.EGLConfig;

import github.com.desfate.livekit.camera.interfaces.CameraErrorCallBack;
import github.com.desfate.livekit.camera.news.CameraInfo;
import github.com.desfate.livekit.live.LiveCallBack;
import github.com.desfate.livekit.live.LiveConfig;
import github.com.desfate.livekit.live.LivePushControl;
import github.com.desfate.livekit.reders.CameraDrawer;
import github.com.desfate.livekit.utils.LiveSupportUtils;
import github.com.desfate.livekit.utils.ScreenUtils;

/**
 * 基于 Data 模式上传的 GLSurfaceView
 */
public class DataLivePushView extends BaseLiveView {

    private CameraDrawer mDrawer; //      openGl渲染代码
    private LiveConfig liveConfig;//      直播配置数据
    private LivePushControl control;//    直播逻辑控制器
    private FocusView focusView;//        对焦视图

    public DataLivePushView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void init(final LiveConfig liveConfig, final ViewGroup parent, LiveCallBack liveCallBack) {
        this.liveConfig = liveConfig;
        focusView = new FocusView(getContext());
        focusView.setVisibility(View.GONE);
        control = new LivePushControl.LivePushControlBuilder()
                .setContext(getContext())
                .setLiveConfig(liveConfig)
                .setSurfaceTexture(getmSurfaceTexture())
                .setLiveCallBack(liveCallBack)
                .setFocusView(focusView)
                .setCameraErrorCallBack(new CameraErrorCallBack() {
                    @Override
                    public void onCameraOpenSuccess(CameraInfo info) {
                        post(new Runnable() {
                            @Override
                            public void run() {
                                addFocusView(liveConfig, parent);
                            }
                        });
                    }

                    @Override
                    public void onCameraOpenError(CameraInfo info, int error) {

                    }
                })
                .build();
        if(parent != null) { parent.addView(focusView); }
    }

    @Override
    public void surfaceCreated(EGLConfig config) {
        mDrawer = new CameraDrawer();
    }

    @Override
    public void onClick(float X, float Y) {
        if(control != null) control.focusClick(X, Y);
    }

    @Override
    public void onDrawFrame(int mSurfaceId) {
        mDrawer.draw(mSurfaceId, liveConfig.getPushCameraType() == 1, getWidth(), getHeight());
    }

    /**
     * 获取直播推流控制器
     * @return 控制器
     */
    public LivePushControl getControl() {
        return control;
    }

    /**
     * 添加对焦focus
     */
    private void addFocusView(LiveConfig liveConfig, final ViewGroup parent){
        boolean screenType = true;
        if(ScreenUtils.getScreenSize(getContext()).getWidth() > ScreenUtils.getScreenSize(getContext()).getHeight()){
            screenType = false;
        }

        Size size = LiveSupportUtils.getCameraBestSize(true, liveConfig.getLiveQuality());

        final int realWidth = ScreenUtils.getScreenSize(getContext()).getWidth();
        final int realHeight = realWidth * size.getWidth() / size.getHeight();

        final int landRealHeight = ScreenUtils.getScreenSize(getContext()).getHeight();
        final int landRealWidth = landRealHeight * size.getWidth() / size.getHeight();

        if(getLayoutParams() instanceof RelativeLayout.LayoutParams){  // 父布局是关联布局
            if(screenType) {
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(realWidth, realHeight);
                setLayoutParams(layoutParams);
                // 重新设定对焦区域大小
                focusView.initFocusArea(realWidth, realHeight);
                control.focusViewChange(realWidth, realHeight);
            }else{
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(landRealWidth, landRealHeight);
                setLayoutParams(layoutParams);
                // 重新设定对焦区域大小
                focusView.initFocusArea(landRealWidth, landRealHeight);
                control.focusViewChange(landRealWidth, landRealHeight);
            }
        }else if(getLayoutParams() instanceof ConstraintLayout.LayoutParams){  //  父布局是约束布局
            if(screenType) {
                ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(realWidth, realHeight);
                setLayoutParams(layoutParams);
                // 重新设定对焦区域大小
                focusView.initFocusArea(realWidth, realHeight);
                control.focusViewChange(realWidth, realHeight);
            }else{
                ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(landRealWidth, landRealHeight);
                setLayoutParams(layoutParams);
                // 重新设定对焦区域大小
                focusView.initFocusArea(landRealWidth, landRealHeight);
                control.focusViewChange(landRealWidth, landRealHeight);
            }
        }
    }

}
