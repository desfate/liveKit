package github.com.desfate.livekit.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.Image;
import android.media.ImageReader;
import android.opengl.EGLContext;
import android.util.Log;
import android.view.TextureView;

import github.com.desfate.livekit.camera.interfaces.CameraErrorCallBack;
import github.com.desfate.livekit.camera.news.CameraClient;
import github.com.desfate.livekit.camera.news.CameraInfo;
import github.com.desfate.livekit.ui.FocusView;
import github.com.desfate.livekit.gl.RenderVideoFrame;
import github.com.desfate.livekit.gl.interfaces.IGLSurfaceTextureListener;
import github.com.desfate.livekit.gl.thread.GLThread;
import github.com.desfate.livekit.live.LiveCallBack;
import github.com.desfate.livekit.live.LivePushInterface;



/**
 * 通过GlThread textureId 进行腾讯推流
 */
public class CameraTextureControl implements LivePushInterface {

    private final static String TAG = "CameraControl";

    private boolean mIsSending;   //                    是否正在推流
    private GLThread mGLThread;   //                    GL线程
    private LiveCallBack callBack; //                   直播管理器 用于管理直播相关接口
    private TextureView textureView;
    private RenderVideoFrame mRender;//                      自定义渲染

    CameraClient mCameraClient; //          相机对象
    Context mContext;
    CameraInfo cameraInfo;

    boolean isPusher = false;  // 是否开始推流


    public CameraTextureControl(Context context
            , CameraInfo cameraInfo
            , TextureView textureView
            , SurfaceTexture surfaceTexture
            , LiveCallBack callBack
            , CameraErrorCallBack errorCallBack)  {
        this.mContext = context;
        this.cameraInfo = cameraInfo;
        this.callBack = callBack;
        this.textureView = textureView;
        mIsSending = false;
        mCameraClient = new CameraClient.CameraClientBuilder()
                .setCameraErrorCallBack(errorCallBack)
                .setSurfaceTexture(surfaceTexture)
                .setContext(context)
                .setmOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                    @Override
                    public void onImageAvailable(ImageReader imageReader) {
                        Image image = imageReader.acquireNextImage(); //这个必须要有  不然会导致卡死
                        image.close();
                    }
                })
                .setmFocusStateCallback(null)
                .build();
        mRender = new RenderVideoFrame(cameraInfo.getDefaultBufferSize(), cameraInfo.isFront());  //  绑定采集和渲染
    }


    IGLSurfaceTextureListener textureListener = new IGLSurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture) {
            Log.d(TAG, "onSurfaceTextureAvailable: " + Thread.currentThread().getName());
            mGLThread.setInputSize(cameraInfo.getDefaultBufferSize().getWidth(), cameraInfo.getDefaultBufferSize().getHeight());
            // 承载视频画面的“画板（SurfaceTexture）”已经准备好了，需要我们创建一个 MovieVideoFrameReader，并与之关联起来。
            mCameraClient.getCamera().addSurfaceTexture(surfaceTexture);
            // 开启相机  开始预览
            mCameraClient.getCamera().openCamera(cameraInfo);
        }

        @Override
        public int onTextureProcess(int textureId, EGLContext eglContext) {
            if (!mIsSending) return textureId;

            // TODO: 2020/10/13 将视频帧通过纹理方式塞给SDK fixme
            // 这里是提交给推流服务的流
            if(callBack != null && isPusher) {
                callBack.startPushByTextureId(textureId, cameraInfo.getImageBufferSize().getWidth(), cameraInfo.getImageBufferSize().getHeight());
            }

//            if (mRender.getRenderFrame() != null) {
//                mRender.getRenderFrame().onRenderVideoFrame(textureId, eglContext);  // 这是本地预览的绘制  暂时不用这边绘制  用本地相机直出的数据绘制
//            }
            return textureId;
        }

        @Override
        public void onSurfaceTextureDestroy(SurfaceTexture surfaceTexture) {
            Log.d(TAG, "onSurfaceTextureDestroy: " + Thread.currentThread().getName());
        }
    };


    /*********************************************************** 作为控制器  必须具备的功能  ***************************************************************/
    /**
     * 开始预览
     */
    @Override
    public void startPreview() {

    }

    /**
     * 开始推流  这个属于后台推流  推流数据不显示
     */
    @Override
    public void startPush() {
        isPusher = true;
        if (mIsSending) return;
        //启动一个 OpenGL 线程，该线程用于定时 sendCustomVideoData()
        mGLThread = new GLThread();
        mGLThread.setListener(textureListener);
        mGLThread.start();
        mIsSending = true;
        mRender.start(textureView);// 自定义渲染器与渲染的 View 绑定
    }

    /**
     * 切换相机
     * @param info 相机信息
     */
    @Override
    public void switchCamera(CameraInfo info) {
        mCameraClient.getCamera().switchCamera(info);
        if(mRender != null) mRender.changeCamera();
    }

    /**
     * 停止推流
     */
    @Override
    public void stopPush() {
        isPusher = false;
    }

    /**
     * 释放资源
     */
    @Override
    public void releaseRes() {
        mCameraClient.getCamera().releaseCamera();
    }

    /**
     * 自定义对焦
     * @return 对焦控制器
     */
    @Override
    public FocusControl customerFocus(FocusView focusView) {
        return null;
    }

    @Override
    public void focusViewChange(int width, int height) {

    }

    @Override
    public void startMFocus(float x, float y) {

    }


}
