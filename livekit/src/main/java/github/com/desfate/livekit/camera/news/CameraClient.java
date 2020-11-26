package github.com.desfate.livekit.camera.news;


import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.Image;
import android.media.ImageReader;
import android.view.TextureView;

import github.com.desfate.livekit.camera.interfaces.CameraErrorCallBack;
import github.com.desfate.livekit.camera.interfaces.FocusStateCallback;

/**
 * 这里封装一下 Camera2
 *
 */
public class CameraClient {

    private CameraInterface mCamera;//                                                                    相机应该具有的功能

    public CameraClient(CameraClientBuilder builder){
        mCamera = new CameraEngine.CameraEngineBuilder(builder.context)
                .setmFocusStateCallback(builder.mFocusStateCallback)
                .setmOnImageAvailableListener(builder.mOnImageAvailableListener)
                .setmSurfaceTexture(builder.surfaceTexture)
                .setCameraCallBack(builder.cameraErrorCallBack)
                .build();
    }

    public CameraInterface getCamera(){
        return mCamera;
    }


    public static class CameraClientBuilder{
        private Context context;
        private FocusStateCallback mFocusStateCallback;//                                                     对焦模式变化接口
        private ImageReader.OnImageAvailableListener mOnImageAvailableListener; //                            相机预览数据的回调接口
        private SurfaceTexture surfaceTexture;
        private CameraErrorCallBack cameraErrorCallBack;

        public CameraClientBuilder setmFocusStateCallback(FocusStateCallback mFocusStateCallback) {
            this.mFocusStateCallback = mFocusStateCallback;
            return this;
        }

        public CameraClientBuilder setmOnImageAvailableListener(ImageReader.OnImageAvailableListener mOnImageAvailableListener) {
            this.mOnImageAvailableListener = mOnImageAvailableListener;
            return this;
        }

        public CameraClientBuilder setContext(Context context) {
            this.context = context;
            return this;
        }

        public CameraClientBuilder setSurfaceTexture(SurfaceTexture surfaceTexture) {
            this.surfaceTexture = surfaceTexture;
            return this;
        }

        public CameraClientBuilder setCameraErrorCallBack(CameraErrorCallBack cameraErrorCallBack) {
            this.cameraErrorCallBack = cameraErrorCallBack;
            return this;
        }

        public CameraClient build(){
            return new CameraClient(this);
        }
    }

}
