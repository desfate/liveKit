package github.com.desfate.livekit.dual;

import android.content.Context;
import android.graphics.SurfaceTexture;

import github.com.desfate.livekit.camera.CameraPreviewControl;
import github.com.desfate.livekit.camera.interfaces.CameraErrorCallBack;
import github.com.desfate.livekit.camera.interfaces.DualPreviewInterface;
import github.com.desfate.livekit.camera.news.CameraInfo;

/**
 * 负责预览的协调工作
 *
 * 暂时只做 基于OpenGl的双摄预览
 * 预留可能同时支持多种模式的预览空间
 */
public class PreviewControl {

    private final static String TAG = "PreviewControl";

    Context context;//                   上下文
    DualPreviewInterface mControl;
    CameraInfo cameraInfo;

    public PreviewControl(PreviewControl.PreviewControlBuilder builder){
        this.context = builder.context;

        // 通过预览配置 生成 相机信息
        cameraInfo = PreviewUtils.dualToCameraInfo(context, builder.previewConfig);
        mControl = new CameraPreviewControl.PreviewControlBuilder()
                .setContext(builder.context)
                .setSurfaceTexture(builder.surfaceTexture)
                .setCameraInfo(cameraInfo)
                .setErrorCallBack(builder.errorCallBack)
                .build();
    }

    public void startPreview(){
        mControl.startPreview();
    }


    public static class PreviewControlBuilder{
        PreviewConfig previewConfig;
        CameraErrorCallBack errorCallBack;
        SurfaceTexture surfaceTexture;
        Context context;

        public PreviewControl.PreviewControlBuilder setPreviewConfig(PreviewConfig previewConfig) {
            this.previewConfig = previewConfig;
            return this;
        }

        public PreviewControl.PreviewControlBuilder setErrorCallBack(CameraErrorCallBack errorCallBack) {
            this.errorCallBack = errorCallBack;
            return this;
        }

        public PreviewControl.PreviewControlBuilder setSurfaceTexture(SurfaceTexture surfaceTexture) {
            this.surfaceTexture = surfaceTexture;
            return this;
        }

        public PreviewControl.PreviewControlBuilder setContext(Context context) {
            this.context = context;
            return this;
        }

        public PreviewConfig getPreviewConfig() {
            return previewConfig;
        }

        public CameraErrorCallBack getErrorCallBack() {
            return errorCallBack;
        }

        public SurfaceTexture getSurfaceTexture() {
            return surfaceTexture;
        }

        public Context getContext() {
            return context;
        }

        public PreviewControl build(){
            return new PreviewControl(this);
        }
    }


}
