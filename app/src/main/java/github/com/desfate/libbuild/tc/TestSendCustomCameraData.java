package github.com.desfate.libbuild.tc;


/*****************************************************************
 *
 *                 测试自定义采集功能 TestSendCustomVideoData
 *
 ******************************************************************/

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.EGLContext;
import android.util.Log;

import com.tencent.rtmp.TXLivePusher;

public class TestSendCustomCameraData implements GLThread.IGLSurfaceTextureListener {
    private static String TAG = "TestSendCustomVideoData";
    private Context mContext;
    private boolean mIsSending;
    private GLThread mGLThread;
    private SurfaceTexture mSurfaceTexture;
    private TXLivePusher mLivePusher;
    private VideoRenderListener renderListener;

    public TestSendCustomCameraData(Context context) {
        mContext = context;
        mIsSending = false;
    }

    public void setLivePusher(TXLivePusher mLivePusher) {
        this.mLivePusher = mLivePusher;
    }

    public void setRenderListener(VideoRenderListener renderListener) {
        this.renderListener = renderListener;
    }

    public synchronized void start() {
        if (mIsSending) return;
        //启动一个 OpenGL 线程，该线程用于定时 sendCustomVideoData()
        mGLThread = new GLThread();
        mGLThread.setListener(this);
        mGLThread.start();
        mIsSending = true;
    }

    private final Object mCameraLock = new Object();
    private Camera mCamera;
    private int mCurrentCameraType = Camera.CameraInfo.CAMERA_FACING_FRONT;

    /**
     * [[1920, 1440], [1920, 1080], [1600, 1200], [1440, 1080], [1280, 960], [1560, 720], [1440, 720], [1280, 720], [800, 600], [720, 480], [640, 480], [640, 360], [352, 288], [320, 240], [176, 144], ]
     */
    public static int mCameraWidth = 720; //1280;
    public static int mCameraHeight = 1280; // 720;

    private int mCameraOrientation;

    @SuppressWarnings("deprecation")
    public void openCamera(final int cameraType) {
        try {
            synchronized (mCameraLock) {
                Camera.CameraInfo info = new Camera.CameraInfo();
                int cameraId = 0;
                int numCameras = Camera.getNumberOfCameras();
                for (int i = 0; i < numCameras; i++) {
                    Camera.getCameraInfo(i, info);
                    Camera.getCameraInfo(i, info);
                    if (info.facing == cameraType) {
                        cameraId = i;
                        mCamera = Camera.open(i);
                        mCurrentCameraType = cameraType;
                        break;
                    }
                }
                if (mCamera == null) {
                    cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
                    Camera.getCameraInfo(cameraId, info);
                    mCamera = Camera.open(cameraId);
                    mCurrentCameraType = cameraId;
                }
                if (mCamera == null) {
                    throw new RuntimeException("No cameras");
                }
                mCameraOrientation = CameraUtils.getCameraOrientation(cameraId);
                CameraUtils.setFocusModes(mCamera.getParameters());
                CameraUtils.chooseFrameRate(mCamera.getParameters());
                float exposureCompensation = CameraUtils.getExposureCompensation(mCamera);
                Log.d(TAG, "openCamera: exposureCompensation = " + exposureCompensation);
                CameraUtils.setCameraDisplayOrientation((Activity) mContext, cameraId, mCamera);
                Camera.Parameters parameters = mCamera.getParameters();
                int[] size = CameraUtils.choosePreviewSize(parameters, mCameraWidth, mCameraHeight);
                mCameraWidth = size[0];
                mCameraHeight = size[1];
                CameraUtils.setExposureCompensation(mCamera, 0.5f);
                //设置相机参数
                mCamera.setParameters(parameters);
            }
            cameraStartPreview();
        } catch (Exception e) {
            Log.e(TAG, "openCamera: ", e);
            releaseCamera();
        }
    }


    private void cameraStartPreview() {
        try {
            if (mCamera == null) {
                return;
            }
            synchronized (mCameraLock) {
                mCamera.setPreviewTexture(mSurfaceTexture);
                mCamera.startPreview();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void releaseCamera() {
        try {
            synchronized (mCameraLock) {
                if (mCamera != null) {
                    mCamera.stopPreview();
                    mCamera.release();
                    mCamera = null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void changeCamera() {
        mIsSending = false;
        releaseCamera();
        openCamera(mCurrentCameraType == Camera.CameraInfo.CAMERA_FACING_FRONT ? Camera.CameraInfo.CAMERA_FACING_BACK : Camera.CameraInfo.CAMERA_FACING_FRONT);
        mIsSending = true;
    }

    public synchronized void stop() {
        if (!mIsSending) return;
        mIsSending = false;

        releaseCamera();

        if (mGLThread != null) mGLThread.stop();

    }

    /**
     * 承载视频画面的“画板（SurfaceTexture）”已经准备好了，需要我们创建一个 MovieVideoFrameReader，并与之关联起来。
     */
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture) {
        Log.d(TAG, "onSurfaceTextureAvailable: " + Thread.currentThread().getName());
        mGLThread.setInputSize(mCameraWidth, mCameraHeight);
        mSurfaceTexture = surfaceTexture;
        openCamera(mCurrentCameraType);
        // mFURenderer.loadItems(UserBeautyUtil.getBeautyFilePath());
    }

    /**
     * 当 GLThread 线程关联的“画板”内容发生变更时，也就是有新的一帧视频渲染上来时，
     * GLThread 就会触发该回调。此时，我们就可以向 TRTC SDK 中 sendCustomVideoData()了。
     */
    @Override
    public int onTextureProcess(int textureId, EGLContext eglContext) {
        if (!mIsSending) return textureId;
        // TODO: 2020/10/13 将视频帧通过纹理方式塞给SDK fixme
        mLivePusher.sendCustomVideoTexture(textureId, mCameraHeight, mCameraWidth);

        if (renderListener != null) {
            renderListener.onRenderVideoFrame(textureId, eglContext);
        }
        return textureId;
    }

    @Override
    public void onSurfaceTextureDestroy(SurfaceTexture surfaceTexture) {
        Log.d(TAG, "onSurfaceTextureDestroy: " + Thread.currentThread().getName());
        //   mFURenderer.destroyItems();
    }

    /**
     * 渲染回调，通知渲染使用
     */
    public interface VideoRenderListener {
        void onRenderVideoFrame(int textureId, EGLContext eglContext);
    }

}
