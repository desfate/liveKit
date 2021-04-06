package github.com.desfate.livekit.controls;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.view.TextureView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import github.com.desfate.livekit.LiveConfig;
import github.com.desfate.livekit.LiveConstant;
import github.com.desfate.livekit.camera.interfaces.CameraErrorCallBack;
import github.com.desfate.livekit.dual.M3dPreviewControl;
import github.com.desfate.livekit.live.LiveCallBack;
import github.com.desfate.livekit.live.LivePushControl;
import github.com.desfate.livekit.ui.BaseLiveView;

/**
 * 我是核心控制器  我负责 整合直播 和 预览绘制两个控制器
 *  LivePushControl + M3dPreviewControl
 * 也只有我是对外开放的 使用的功能均来自我
 *
 */
public class MCameraControl implements MControl{

    private final LivePushControl liveControl;//        直播逻辑控制器
    private final M3dPreviewControl previewControl;//   3d预览控制器
    private final MCameraControlBuilder builder;

    public MCameraControl(MCameraControlBuilder builder){
        this.builder = builder;
        previewControl = new M3dPreviewControl(builder.baseLiveView, builder.liveConfig);  //       初始化3d控制器
        if(builder.liveConfig.getLivePushType() == LiveConstant.LivePushType.DATA) {  // 数据推送
            liveControl = new LivePushControl.LivePushControlBuilder()
                    .setContext(builder.context)
                    .setLiveConfig(builder.liveConfig)
                    .setSurfaceTexture(builder.baseLiveView.getSurfaceTexture())
                    .setLiveCallBack(builder.liveCallBack)
                    .setFocusView(null)
                    .setCameraErrorCallBack(builder.callBack)
                    .build();
        }else{ // texture 推送
            //   背屏渲染
            TextureView textureView = new TextureView(builder.context);
            liveControl = new LivePushControl.LivePushControlBuilder()
                    .setContext(builder.context)
                    .setLiveConfig(builder.liveConfig)
                    .setSurfaceTexture(builder.baseLiveView.getSurfaceTexture())// 这个SurfaceTexture是作为预览的
                    .setTextureView(textureView)            // 这个textureView并不显示   这个是作为离屏渲染模块
                    .setLiveCallBack(builder.liveCallBack)
                    .setCameraErrorCallBack(builder.callBack)
                    .build();
        }
    }


    @Override
    public boolean isCameraFront() {
        return liveControl.isFront();
    }

    // 外部可以调用的代码
    @Override
    public void switchCamera() {
        if(liveControl != null) liveControl.switchCamera();
        if(previewControl != null) previewControl.switchCamera();
    }

    @Override
    public void startPush() {
        if(liveControl != null) liveControl.startPush();
    }

    @Override
    public void startPreview() {
        if(liveControl != null) liveControl.startPreview();
    }

    @Override
    public void release() {
        if(liveControl != null) liveControl.releaseRes();
    }

    // 自己内部使用的方法
    public void surfaceCreated(GL10 gl , EGLConfig config){
        previewControl.getDrawControl().setTextureId(builder.getBaseLiveView().getSurfaceId());
        previewControl.getDrawControl().onCreated(gl, config);
    }

    public void onDrawFrame(GL10 gl ,int mSurfaceId) {
        previewControl.getDrawControl().onDrawFrame(gl);
    }

    public void onChanged(GL10 gl, int width, int height) {
        previewControl.getDrawControl().onSurfaceChanged(gl, width, height);
    }

    public void onFrame(SurfaceTexture surfaceTexture) {
        previewControl.getDrawControl().canDrawerFrame(); // 设置可以开始绘制
    }

    public static class MCameraControlBuilder{
        LiveConfig liveConfig;
        LiveCallBack liveCallBack;
        BaseLiveView baseLiveView;
        Context context;
        CameraErrorCallBack callBack;

        public LiveConfig getLiveConfig() {
            return liveConfig;
        }

        public MCameraControlBuilder setLiveConfig(LiveConfig liveConfig) {
            this.liveConfig = liveConfig;
            return this;
        }

        public LiveCallBack getLiveCallBack() {
            return liveCallBack;
        }

        public MCameraControlBuilder setLiveCallBack(LiveCallBack liveCallBack) {
            this.liveCallBack = liveCallBack;
            return this;
        }

        public BaseLiveView getBaseLiveView() {
            return baseLiveView;
        }

        public MCameraControlBuilder setBaseLiveView(BaseLiveView baseLiveView) {
            this.baseLiveView = baseLiveView;
            return this;
        }

        public Context getContext() {
            return context;
        }

        public MCameraControlBuilder setContext(Context context) {
            this.context = context;
            return this;
        }

        public CameraErrorCallBack getCallBack() {
            return callBack;
        }

        public MCameraControlBuilder setCallBack(CameraErrorCallBack callBack) {
            this.callBack = callBack;
            return this;
        }

        public MCameraControl build(){
            return new MCameraControl(MCameraControlBuilder.this);
        }
    }



}
