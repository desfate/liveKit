package github.com.desfate.livekit.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.Image;
import android.media.ImageReader;

import github.com.desfate.livekit.camera.interfaces.CameraErrorCallBack;
import github.com.desfate.livekit.camera.interfaces.DualPreviewInterface;
import github.com.desfate.livekit.camera.news.CameraClient;
import github.com.desfate.livekit.camera.news.CameraInfo;

/**
 * 只负责预览的控制器
 */
public class CameraPreviewControl implements DualPreviewInterface {

    CameraClient mCameraClient; //          相机对象
    PreviewControlBuilder builder;

    public CameraPreviewControl(PreviewControlBuilder builder){
        this.builder = builder;
        mCameraClient = new CameraClient.CameraClientBuilder()
                .setCameraErrorCallBack(builder.getErrorCallBack())
                .setSurfaceTexture(builder.getSurfaceTexture())
                .setContext(builder.getContext())
                .setmOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                    @Override
                    public void onImageAvailable(ImageReader imageReader) {
                        Image image = imageReader.acquireNextImage(); //这个必须要有  不然会导致卡死
                        image.close();
                    }
                })
                .setmFocusStateCallback(null)
                .build();


    }

    @Override
    public void startPreview() {
        mCameraClient.getCamera().openCamera(this.builder.getCameraInfo());
    }

    public static class PreviewControlBuilder{
        CameraInfo cameraInfo;
        CameraErrorCallBack errorCallBack;
        SurfaceTexture surfaceTexture;
        Context context;

        public PreviewControlBuilder setCameraInfo(CameraInfo cameraInfo) {
            this.cameraInfo = cameraInfo;
            return this;
        }

        public PreviewControlBuilder setErrorCallBack(CameraErrorCallBack errorCallBack) {
            this.errorCallBack = errorCallBack;
            return this;
        }

        public PreviewControlBuilder setSurfaceTexture(SurfaceTexture surfaceTexture) {
            this.surfaceTexture = surfaceTexture;
            return this;
        }

        public PreviewControlBuilder setContext(Context context) {
            this.context = context;
            return this;
        }

        public CameraInfo getCameraInfo() {
            return cameraInfo;
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

        public CameraPreviewControl build(){
            return new CameraPreviewControl(this);
        }
    }


}
