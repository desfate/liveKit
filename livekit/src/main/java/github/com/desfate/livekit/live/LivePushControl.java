package github.com.desfate.livekit.live;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.view.TextureView;

import github.com.desfate.livekit.CameraAdapter;
import github.com.desfate.livekit.LiveConfig;
import github.com.desfate.livekit.LiveConstant;
import github.com.desfate.livekit.camera.CameraDataControl;
import github.com.desfate.livekit.camera.CameraTextureControl;
import github.com.desfate.livekit.camera.FocusControl;
import github.com.desfate.livekit.camera.interfaces.CameraErrorCallBack;
import github.com.desfate.livekit.camera.news.CameraInfo;
import github.com.desfate.livekit.ui.FocusView;

/**
 * 直播控制器 用于分离 相机核心
 *
 * 现阶段有两套推送方案
 * 1： 通过相机中的ImageReader 返回的数据 进行数据推送
 * 2： 通过GlThread 的方式 向腾讯云直接推送textureId 可以完成推送
 *
 * 控制器需要具备的一些功能
 * 1： 绑定本地预览
 * 2： 设置直播类型
 * 3： 开启直播上传
 * 4： 切换直播摄像头
 * 5： 停止直播，释放资源
 * 6： 绑定上传回调
 *
 * 这里使用静态代理  PushDataAgent  数据类型代理
 *                PushTextureIdAgent textureId 类型代理
 */
public class LivePushControl implements MLiveControl{
    private final static String TAG = "LivePushControl";

    Context context;//                   上下文
    LiveConfig liveConfig;//             直播配置
    CameraInfo cameraInfo;//             通过直播配置 找到的合适的相机设置

    LivePushInterface mControl;//        直播推送控制器（控制直播逻辑 & 行为）
    FocusControl focusControl;//         对焦控制器（控制对焦逻辑 & 行为）

    private LivePushControl(LivePushControlBuilder builder) {
        this.context = builder.context;
        this.liveConfig = builder.liveConfig;
        if (liveConfig != null) {
            cameraInfo = CameraAdapter.liveConfigToCameraInfo(builder.liveConfig);
            if (liveConfig.getLivePushType() == LiveConstant.LivePushType.DATA) { //                          通过data 模式进行直播推流
                mControl = new CameraDataControl(builder.context, builder.surfaceTexture, cameraInfo, builder.liveCallBack, builder.cameraErrorCallBack);
                focusControl = mControl.customerFocus(builder.focusView);  //                                             开启自定义对焦
            } else {
                // 通过texture模式推流
                mControl = new CameraTextureControl(builder.context, cameraInfo, builder.textureView, builder.surfaceTexture, builder.liveCallBack, builder.cameraErrorCallBack);
            }
        }
    }

    // Camera method
    public void startPreview(){
//        mControl.startPreview();  // 重新整理架构的话  开启预览是preview模块的任务
    }

    public void startPush() {
        mControl.startPush();
    }

    public void switchCamera() {
        liveConfig.switchCamera();
        mControl.switchCamera(CameraAdapter.liveConfigToCameraInfo(liveConfig));
    }

    /**
     * 返回当前状态
     * @return true 前置 false 后置
     */
    public boolean isFront(){
        if(liveConfig != null){
            return liveConfig.isFront();
        }
        return false;
    }

//    public boolean getCameraFront

    public void stopPush() {
        mControl.stopPush();
    }

    public void releaseRes() {
        mControl.releaseRes();
    }

    // Focus method
    public void focusClick(float X, float Y){ if(mControl != null) mControl.startMFocus(X, Y); }

    public void focusViewChange(int width, int height){ if(mControl != null) mControl.focusViewChange(width, height);}



    public static class LivePushControlBuilder {
        Context context;
        LiveConfig liveConfig;
        SurfaceTexture surfaceTexture;
        TextureView textureView;
        LiveCallBack liveCallBack;
        CameraErrorCallBack cameraErrorCallBack;
        FocusView focusView;

        public LivePushControlBuilder setContext(Context context) {
            this.context = context;
            return this;
        }

        public LivePushControlBuilder setLiveConfig(LiveConfig liveConfig) {
            this.liveConfig = liveConfig;
            return this;
        }

        public LivePushControlBuilder setSurfaceTexture(SurfaceTexture surfaceTexture) {
            this.surfaceTexture = surfaceTexture;
            return this;
        }

        public LivePushControlBuilder setTextureView(TextureView textureView) {
            this.textureView = textureView;
            return this;
        }

        public LivePushControlBuilder setLiveCallBack(LiveCallBack liveCallBack) {
            this.liveCallBack = liveCallBack;
            return this;
        }

        public LivePushControlBuilder setFocusView(FocusView focusView) {
            this.focusView = focusView;
            return this;
        }

        public LivePushControlBuilder setCameraErrorCallBack(CameraErrorCallBack cameraErrorCallBack) {
            this.cameraErrorCallBack = cameraErrorCallBack;
            return this;
        }

        public LivePushControl build(){
            return new LivePushControl(this);
        }
    }


}
