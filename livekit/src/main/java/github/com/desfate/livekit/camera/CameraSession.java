package github.com.desfate.livekit.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.MeteringRectangle;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.SessionConfiguration;
import android.media.ImageReader;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import github.com.desfate.livekit.camera.interfaces.CameraChangeCallback;
import github.com.desfate.livekit.camera.interfaces.FocusStateCallback;
import github.com.desfate.livekit.live.LiveConfig;
import github.com.desfate.livekit.utils.JobExecutor;
import github.com.desfate.livekit.utils.LiveSupportUtils;
import github.com.desfate.livekit.reders.OpenGLUtils;


/**
 * 管理相机会话事件
 */

public class CameraSession {

    private final static String TAG = "CameraSession";

    private LiveConfig mLiveConfig;//                               直播的配置
    private CameraConfig mCameraConfig; //                          摄像头的配置

    private CameraDevice mCameraDevice;  //                         相机设备
    private CameraManager mCameraManager;  //                       相机相关信息管理类
    private CameraCharacteristics mCameraCharacteristics; //        当前相机设备相关配置

    private CameraCaptureSession mCameraCaptureSession;  //         相机事务
    private CaptureRequest.Builder mCaptureBuilder; //              相机预览请求的构造器
    private CaptureRequest mCaptureRequest;  //                     相机预览请求
    private ImageReader mImageReader; //                            相机采集纹理预览数据

    private Surface mSurface; //                                    页面的Surface
    private SurfaceTexture surfaceTexture; //                       相机纹理

    private HandlerThread mBackgroundThread; //                     An additional thread for running tasks that shouldn't block the UI
    private Handler mBackgroundHandler; //                          for running tasks in the background
    private JobExecutor mJobExecutor; //                            线程池
    private Semaphore mCameraOpenCloseLock;//                       to prevent the app from exiting before closing the camera.

    private boolean isAutoFocus = true;//                           对焦模式 true: 自动对焦  false： 手动对焦
    private MeteringRectangle[] mResetRect = //                     自动对焦的对焦区域
            new MeteringRectangle[]{
                    new MeteringRectangle(0, 0, 0, 0, 0)
            };

    private FocusStateCallback mFocusStateCallback;//               对焦模式变化接口
    private ImageReader.OnImageAvailableListener
            mOnImageAvailableListener; //                           相机预览数据的回调接口
    private CameraCaptureSession.CaptureCallback
            mCaptureCallbackListener; //                            对焦回调接口
    private int mLatestAfState; //                                  上一次的对焦模式
    private CameraChangeCallback cameraChangeCallback;//            相机分辨率改变回调


    private static final int STATE_PREVIEW = 0;
    private static final int STATE_WAITING_LOCK = 1;
    private static final int STATE_WAITING_PRE_CAPTURE = 2;  //     相机状态：等待曝光为预曝光
    private static final int STATE_WAITING_NON_PRE_CAPTURE = 3; //  相机状态：等待曝光状态不是预曝光
    private static final int STATE_PICTURE_TAKEN = 4;
    private static final int STATE_VIDEO_TAKEN = 5;
    private int mState = STATE_PREVIEW;

    public CameraSession(Context context, FocusStateCallback mFocusStateCallback) {
        this.mFocusStateCallback = mFocusStateCallback;
        startBackgroundThread();  // 启动后台线程
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        mJobExecutor = new JobExecutor(); // 初始化线程池
        mCameraOpenCloseLock = new Semaphore(1);
        if(mCameraConfig == null) mCameraConfig = new CameraConfig();  //默认前置
        mLiveConfig = new LiveConfig();      //默认1080p
        mCaptureCallbackListener = new CameraCaptureSession.CaptureCallback() {
            private void process(CaptureResult result) {
                switch (mState) {
                    case STATE_PREVIEW:
                        // We have nothing to do when the camera preview is working normally.
                        break;
                    case STATE_WAITING_LOCK: {
                        Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                        if (afState == null) {
                        } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||
                                CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                            // CONTROL_AE_STATE can be null on some devices
                            Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                            if (aeState == null ||
                                    aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                                mState = STATE_PICTURE_TAKEN;
                            } else {
                                mState = STATE_WAITING_PRE_CAPTURE;
                            }
                        }
                        break;
                    }
                    case STATE_WAITING_PRE_CAPTURE:
                        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                        if (aeState == null ||
                                aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                                aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                            mState = STATE_WAITING_NON_PRE_CAPTURE;
                        }
                        break;
                    case STATE_WAITING_NON_PRE_CAPTURE:
                        // CONTROL_AE_STATE can be null on some devices
                        Integer aeStates = result.get(CaptureResult.CONTROL_AE_STATE);
                        if (aeStates == null || aeStates != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                            mState = STATE_PICTURE_TAKEN;
                        }
                        break;
                }
            }

            @Override
            public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull  //当图像捕获部分进行时就会回调该方法，此时一些(但不是全部)结果是可用的
                    CaptureRequest request, @NonNull CaptureResult partialResult) {
                super.onCaptureProgressed(session, request, partialResult);
                process(partialResult);
                updateAfState(partialResult);

            }

            @Override
            public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull  //当图像捕捉完全完成时，并且结果已经可用时回调该方法
                    CaptureRequest request, @NonNull TotalCaptureResult result) {
                super.onCaptureCompleted(session, request, result);
                process(result);
                updateAfState(result);
            }

            @Override
            public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull  //当相机设备产生 TotalCaptureResult 失败时就回调该方法
                    CaptureRequest request, @NonNull CaptureFailure failure) {
                super.onCaptureFailed(session, request, failure);
                Log.w(TAG, "onCaptureFailed reason:" + failure.getReason());
            }
        };
    }

    /**
     * 开启相机 此方法调用时 必须保证相机权限打开
     */
    public void openCamera(CameraChangeCallback cameraChangeCallback) {
        this.cameraChangeCallback = cameraChangeCallback;
        mJobExecutor.execute(new JobExecutor.Task<Void>() {

            @SuppressLint("MissingPermission")
            @Override
            public Void run() {
                settingCamera();
                try {
                    if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                        throw new RuntimeException("Time out waiting to lock camera opening.");
                    }
                    mCameraManager.openCamera(String.valueOf(mCameraConfig.getmCameraId()), new CameraDevice.StateCallback() {

                        @RequiresApi(api = Build.VERSION_CODES.P)
                        @Override
                        public void onOpened(@NonNull CameraDevice camera) {
                            mCameraOpenCloseLock.release();
                            mCameraDevice = camera;
                            createCameraPreviewSession();
                        }

                        @Override
                        public void onDisconnected(@NonNull CameraDevice camera) {
                            mCameraOpenCloseLock.release();
                            camera.close();
                            mCameraDevice = null;
                        }

                        @Override
                        public void onError(@NonNull CameraDevice camera, int error) {
                            mCameraOpenCloseLock.release();
                            camera.close();
                            mCameraDevice = null;
                        }
                    }, mBackgroundHandler);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return super.run();
            }
        });
    }

    /**
     * 关闭相机 出现问题后回调该方法
     */

    public void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if(mCameraCaptureSession != null) mCameraCaptureSession.close();
//            Optional.ofNullable(mCameraCaptureSession).ifPresent(cameraCaptureSession -> cameraCaptureSession.close());
            mCameraCaptureSession = null;
            if(mCameraDevice != null) mCameraDevice.close();
//            Optional.ofNullable(mCameraDevice).ifPresent(cameraDevice -> cameraDevice.close());
            mCameraDevice = null;

        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    /**
     * 切换相机 现在是前后摄像头切换
     */
    public void switchCamera() {
        releaseCamera();
        if (mCameraConfig.isFrontCamera()) {
            mCameraConfig.setFrontCamera(false);
            mCameraConfig.setmCameraId(0);
        } else {
            mCameraConfig.setFrontCamera(true);
            mCameraConfig.setmCameraId(1); //前置一般为1
        }
        openCamera(cameraChangeCallback);
    }


    /**
     * 选择指定的相机
     *
     * @param mCameraConfig
     */
    public void selectCamera(CameraConfig mCameraConfig) {
        releaseCamera();
        this.mCameraConfig = mCameraConfig;
        openCamera(cameraChangeCallback);
    }

    /**
     * 关联页面的surface
     *
     * @param surface
     * @param surfaceTexture
     */
    public void addSurface(Surface surface, SurfaceTexture surfaceTexture) {
        this.mSurface = surface;
        this.surfaceTexture = surfaceTexture;
    }

    /**
     * 相机打开成功后 建立相机会话
     */

    @RequiresApi(api = Build.VERSION_CODES.P)
    private void createCameraPreviewSession() {
        try {
            mCaptureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            // 照理这里是不会重新生成的  这里是为了保证代码健壮性
            if (surfaceTexture == null)
                surfaceTexture = new SurfaceTexture(OpenGLUtils.getExternalOESTextureID());
            if (mSurface == null) mSurface = new Surface(surfaceTexture);

            OutputConfiguration surface = new OutputConfiguration(mSurface);  // 预览的surface
            OutputConfiguration imageSurface = new OutputConfiguration(mImageReader.getSurface());  // 拍照的surface

            mCaptureBuilder.addTarget(mSurface);
            mCaptureBuilder.addTarget(mImageReader.getSurface());

            List<OutputConfiguration> outputConfigsAll = Arrays.asList(surface, imageSurface);
            SessionConfiguration sessionConfiguration = new SessionConfiguration(
                    SessionConfiguration.SESSION_REGULAR,
                    outputConfigsAll,
                    AsyncTask.SERIAL_EXECUTOR,
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            try {
                                mCameraCaptureSession = session; // 拿到相机事物
                                // 这里可以设置一些事务 如： 设置对焦模式 设置曝光模式
                                //自动对焦
                                mCaptureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO);
                                // 设置自动曝光
                                mCaptureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest
                                        .CONTROL_AE_MODE_ON_AUTO_FLASH);
                                mCaptureRequest = mCaptureBuilder.build();
                                mCameraCaptureSession.setRepeatingRequest(mCaptureRequest, null, mBackgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                        }
                    });
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                mCameraDevice.createCaptureSession(sessionConfiguration);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {

        }
    }

    /**
     * 设置合适当前相机的分辨率
     */

    public void settingCamera() {
        try {
            mCameraCharacteristics = mCameraManager.getCameraCharacteristics(String.valueOf(mCameraConfig.getmCameraId()));
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } finally {
            // 当前相机匹配的直播分辨率， 就是采集的分辨率（因为采集到的原始流是直接推给服务器的）
            Size liveSize = LiveSupportUtils.getCameraBestSize(mCameraConfig.isFrontCamera(), mLiveConfig.getLiveQuality());
            surfaceTexture.setDefaultBufferSize(liveSize.getWidth(), liveSize.getHeight());
            mImageReader = ImageReader.newInstance(liveSize.getWidth(), liveSize.getHeight(), ImageFormat.YUV_420_888, 1);
//            Optional.ofNullable(cameraChangeCallback).ifPresent(cameraChangeCallback -> cameraChangeCallback.viewChanged(mCameraConfig.isFrontCamera(), liveSize));
            if(cameraChangeCallback != null)
                cameraChangeCallback.viewChanged(mCameraConfig.isFrontCamera(), liveSize);
            mImageReader.setOnImageAvailableListener(
                    mOnImageAvailableListener, mBackgroundHandler);
        }
    }

    /**
     * 设置手动对焦 手动对焦只触发一次 之后会切换回自动对焦
     */
    public void cameraAFSetting(MeteringRectangle focusRect, MeteringRectangle meteringRect) {
        isAutoFocus = false;
        try {
            CaptureRequest.Builder builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            builder.addTarget(mSurface);
            int[] allAFMode = mCameraCharacteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
            int afMode = -1;
            for (int mode : allAFMode) {
                if (mode == CaptureRequest.CONTROL_AF_MODE_AUTO) {
                    afMode = CaptureRequest.CONTROL_AF_MODE_AUTO;
                }
            }
            if (afMode == -1) {
                afMode = allAFMode[0];
            }
            builder.set(CaptureRequest.CONTROL_AF_MODE, afMode);  // 检查相机是否支持手动对焦模式
            builder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);  // 3A模式（自动曝光、自动白平衡、自动对焦
            MeteringRectangle[] mFocusArea = new MeteringRectangle[]{focusRect};
            MeteringRectangle[] mMeteringArea = new MeteringRectangle[]{meteringRect};
            if (mCameraCharacteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF) > 0) {  // 自动对焦区域
                builder.set(CaptureRequest.CONTROL_AF_REGIONS, mFocusArea);
            }
            if (mCameraCharacteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AE) > 0) {  // 自动曝光区域
                builder.set(CaptureRequest.CONTROL_AE_REGIONS, mMeteringArea);
            }
            builder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_IDLE);  // 清除掉触发对焦的Request， 不然会不断对焦
            CaptureRequest request = builder.build();
            mCameraCaptureSession.setRepeatingRequest(request, null, mBackgroundHandler);
            builder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START); //触发对焦
            mCameraCaptureSession.capture(builder.build(), null, mBackgroundHandler); //发送上述设置的对焦请求，并监听回调
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (IllegalStateException e){

        }
    }

    /**
     * 控制当前自动聚焦模式的选择
     */
    public void sendControlFocusModeRequest() {
        isAutoFocus = true;
        int[] allAFMode = mCameraCharacteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
        int afMode = -1;
        for (int mode : allAFMode) {
            if (mode == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED) {
                afMode = CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED;
            }
        }
        if (afMode == -1) {
            afMode = allAFMode[0];
        }
        mCaptureBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO); // 3A模式（自动曝光、自动白平衡、自动对焦
        mCaptureBuilder.set(CaptureRequest.CONTROL_AF_MODE, afMode);  // 检查相机是否支持手动对焦模式
        mCaptureBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, mResetRect);
        mCaptureBuilder.set(CaptureRequest.CONTROL_AE_REGIONS, mResetRect);
        // cancel af trigger
        mCaptureBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_IDLE);
        mCaptureRequest = mCaptureBuilder.build();
        try {
            mCameraCaptureSession.setRepeatingRequest(mCaptureRequest, mCaptureCallbackListener, mBackgroundHandler);  //设置预览界面;  //设置预览界面
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }catch (IllegalStateException e){

        }
    }

    /**
     * 采集的视频信息回调
     *
     * @param listener
     */
    public void setImageReaderListener(ImageReader.OnImageAvailableListener listener) {
        this.mOnImageAvailableListener = listener;
    }

    public LiveConfig getmLiveConfig() {
        return mLiveConfig;
    }

    public void setmLiveConfig(LiveConfig mLiveConfig) {
        this.mLiveConfig = mLiveConfig;
    }

    /**
     * 对焦状态
     *
     * @return true：自动对焦 false：手动对焦
     */
    public boolean getFocusState() {
        return isAutoFocus;
    }

    /**
     * 相机摄像头状态
     *
     * @return true: 前置  false：后置
     */
    public boolean getCameraState() {
        return mCameraConfig == null || mCameraConfig.isFrontCamera();
    }

    /**
     * 释放相机相关
     */
    public void releaseCamera() {
        mJobExecutor.execute(new JobExecutor.Task<Void>() {

            @Override
            public Void run() {
//                Optional.ofNullable(mCameraDevice).ifPresent(cameraDevice -> mCameraDevice.close());
                if(mCameraDevice != null) mCameraDevice.close();
                return super.run();
            }
        });
    }

    /**
     * 更新当前对焦状态
     *
     * @param result
     */

    private void updateAfState(CaptureResult result) {
        Integer state = result.get(CaptureResult.CONTROL_AF_STATE);
        if (state != null && mLatestAfState != state) {
            mLatestAfState = state;
            if (state != 3)
                if(mFocusStateCallback != null) mFocusStateCallback.focusChanged(state);
//                Optional.ofNullable(mFocusStateCallback).ifPresent(cameraViewListener -> cameraViewListener.focusChanged(state));
        }
    }

    /**
     * 获取相机相关配置
     *
     * @return
     */
    public CameraCharacteristics getCameraCharacteristics() {
        return mCameraCharacteristics;
    }



    /**
     * 释放数据
     */

    public void release() {
        closeCamera();
        stopBackgroundThread();
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
