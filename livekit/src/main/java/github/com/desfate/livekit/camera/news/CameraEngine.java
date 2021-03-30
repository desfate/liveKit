package github.com.desfate.livekit.camera.news;

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
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import github.com.desfate.livekit.CameraConstant;
import github.com.desfate.livekit.camera.interfaces.CameraErrorCallBack;
import github.com.desfate.livekit.camera.interfaces.FocusStateCallback;
import github.com.desfate.livekit.dual.DualRequestKey;
import github.com.desfate.livekit.reders.OpenGLUtils;
import github.com.desfate.livekit.utils.JobExecutor;
import github.com.desfate.livekit.utils.LiveSupportUtils;

/**
 * 相机核心  此核心基于  Camera2
 * 这里考虑一个核心 支持 Camera2 以及 CameraX
 * 考虑用工厂模式 初始化不同类型的核心 （这里是同类商品  所以用工厂模式）
 *
 * 实现相机功能 向外暴露相关功能接口
 * <p>
 * Camera2 相机工作流程
 * <p>
 * 1. CameraManager                ------ openCamera()                           开启相机
 * 2. CameraDevice                 ------ createCaptureRequest()                 创建相机请求 可以获得一个请求信息的建造者
 * 3. SessionConfiguration         ------ new（）                                 配置相机会话中，的配置信息 同时绑定例如ImageReader | Surface | SurfaceTexture （如果有相机物理id 则可以绑定多物理摄像头） // https://developer.android.google.cn/training/camera/multi-camera?hl=en
 * 4. CameraDevice                 ------ createCaptureSession()                 创建相机会话
 *
 * 按设计模式优化结构
 * 接口隔离原则（Interface Segregation Principle，ISP） 处理 CameraInterface
 * 单一职责原则（Single Responsibility Principle，SRP）又称单一功能原则
 *
 */
public class CameraEngine implements CameraInterface {
    private final static String TAG = "CameraEngine";

    private CameraDevice mCameraDevice;  //                         相机设备
    private CameraManager mCameraManager;  //                       相机相关信息管理类

    private CameraCharacteristics mCameraCharacteristics; //        当前相机设备相关配置
    private CameraCaptureSession mCameraCaptureSession;  //         相机事务

    private CaptureRequest.Builder mCaptureBuilder; //              相机预览请求的构造器
    private CaptureRequest mCaptureRequest;  //                     相机预览请求

    private ImageReader mImageReader; //                            相机采集纹理预览数据

    private Surface mSurface; //                                    页面的Surface
    private Surface mFrameBufferSurface; //                         离屏渲染Surface FBO

    private DualRequestKey dualControl;//                           双摄输出控制器

    private HandlerThread mBackgroundThread; //                     An additional thread for running tasks that shouldn't block the UI
    private Handler mBackgroundHandler; //                          for running tasks in the background
    private JobExecutor mJobExecutor; //                            线程池
    private Semaphore mCameraOpenCloseLock;//                       to prevent the app from exiting before closing the camera.

    private CameraFocusControl focusControl;  //                    对焦管理

    private CameraInfo cameraInfo; //                               当前相机配置


    /*************************************************** 来自外部的数据 ***********************************************************/
    private Context mContext;
    private FocusStateCallback mFocusStateCallback;//                                                     对焦模式变化接口
    private ImageReader.OnImageAvailableListener mOnImageAvailableListener; //                           相机预览数据的回调接口
    private SurfaceTexture surfaceTexture; //                                                            相机纹理信息
    private SurfaceTexture frameBufferSurfaceTexture; //                                                 离屏渲染纹理信息
    private CameraErrorCallBack cameraCallBack;//                                                        相机运行时的一些问题回调

    private CameraEngine(Context context, CameraEngineBuilder builder) {
        getBuilder(builder);
        // 启动后台线程
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
        // 初始化线程池
        mJobExecutor = new JobExecutor();
        // 线程同步  防止多次开启相机
        mCameraOpenCloseLock = new Semaphore(1); //  最大线程数 1
        // 初始化Camera  Step: 1
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
    }


    public void getBuilder(CameraEngineBuilder builder) {
        this.mContext = builder.context;
        this.mFocusStateCallback = builder.mFocusStateCallback;
        this.mOnImageAvailableListener = builder.mOnImageAvailableListener;
        this.surfaceTexture = builder.mSurfaceTexture;
        this.cameraCallBack = builder.cameraCallBack;
        this.frameBufferSurfaceTexture = builder.mFrameSurfaceTexture;
    }

    /**
     * @param info 需要开启的摄像头的信息
     */
    @Override
    public void openCamera(final CameraInfo info) {
        mJobExecutor.execute(new JobExecutor.Task<Void>() {
            @SuppressLint("MissingPermission")
            @Override
            public Void run() {
                // 开启相机之前 需要配置一些 基础数据
                cameraInfoSetting(info);
                try {
                    if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                        throw new RuntimeException("Time out waiting to lock camera opening.");
                    }
                    // Step: 1 开启相机
                    mCameraManager.openCamera(String.valueOf(info.getLogicCameraId()), new CameraDevice.StateCallback() {
                        @RequiresApi(api = Build.VERSION_CODES.P)
                        @Override
                        public void onOpened(@NonNull CameraDevice camera) {  //  相机成功被打开
                            mCameraOpenCloseLock.release();
                            cameraInfo = info;
                            mCameraDevice = camera;
                            if(cameraCallBack != null){ //   开启相机成功
                                cameraCallBack.onCameraOpenSuccess(cameraInfo);
                            }
                            // 这里开启成功
                            // Step: 2 初创建相机请求
                            try {
                                createCameraPreviewSession();
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onDisconnected(@NonNull CameraDevice camera) {
                            mCameraOpenCloseLock.release();
                            camera.close();
                            mCameraDevice = null;
                        }

                        @Override
                        public void onError(@NonNull CameraDevice camera, int error) {
                            // 开启相机 onError 有五种情况 CameraErrorCode.class
                            Log.e(TAG, CameraErrorCode.errorCamera(error));
                            if(cameraCallBack != null){ // 开启相机失败
                                cameraCallBack.onCameraOpenError(cameraInfo, error);
                            }
                            mCameraOpenCloseLock.release();
                            camera.close();
                            mCameraDevice = null;
                        }
                    }, mBackgroundHandler);
                } catch (CameraAccessException | InterruptedException e) {
                    e.printStackTrace();
                }
                return super.run();
            }
        });
    }

    /**
     * 添加离屏渲染部分
     * @param surfaceTexture
     */
    @Override
    public void addSurfaceTexture(SurfaceTexture surfaceTexture) {
        this.frameBufferSurfaceTexture = surfaceTexture;
    }

    @Override
    public void switchCamera(CameraInfo info) {
        releaseCamera();
        openCamera(info);
    }

    @Override
    public void stopCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if(mCameraCaptureSession != null) mCameraCaptureSession.close();
            mCameraCaptureSession = null;
            if(mCameraDevice != null) mCameraDevice.close();
            mCameraDevice = null;
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }
    @Override
    public void releaseCamera() {
        mJobExecutor.execute(new JobExecutor.Task<Void>() {

            @Override
            public Void run() {
                if(mCameraDevice != null) mCameraDevice.close();
                return super.run();
            }
        });
    }
    /************************************************************************* 上层需要获取的一些信息反馈  **************************************/

    public CameraInfo getCameraInfo() {
        return cameraInfo;
    }

    public CameraCharacteristics getCameraCharacteristics(){
        return mCameraCharacteristics;
    }

    /************************************************************************* 对焦相关功能 *********************************************************************/

    @Override
    public int focusState() {
        return focusControl.getFocusState();  // 获取当前对焦状态
    }

    @Override
    public void autoFocus() {
        mCaptureBuilder = focusControl.setAFState();  // 设置自动对焦
        repeatingRequest();
    }

    @Override
    public void manualFocus(MeteringRectangle focusRect, MeteringRectangle meteringRect) {
        focusControl.setMFState(focusRect, meteringRect, mSurface, mCaptureCallbackListener, mBackgroundHandler);   // 手动对焦
    }

    /*************************************************************************  相机处理流程的方法  *******************************************************************************/

    /**
     * 设置预览分辨率 已经采集的Image分辨率
     *
     * @param info 相机配置
     */
    public void cameraInfoSetting(CameraInfo info) {
        try {
            mCameraCharacteristics = mCameraManager.getCameraCharacteristics(String.valueOf(info.getLogicCameraId()));
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } finally {
            surfaceTexture.setDefaultBufferSize(info.getDefaultBufferSize().getWidth(), info.getDefaultBufferSize().getHeight());
            if(frameBufferSurfaceTexture != null){
                frameBufferSurfaceTexture.setDefaultBufferSize(info.getDefaultBufferSize().getWidth(), info.getDefaultBufferSize().getHeight());
            }
            // imageReader 能不能决定采集到的数据宽高
            // 经过测试  发现 ImageReader 输出宽高是可以修改 ， 但是要依据相机参数 ， 它会自动匹配对应最合适的相机参数
            mImageReader = ImageReader.newInstance(info.getImageBufferSize().getWidth(), info.getImageBufferSize().getHeight(), ImageFormat.YUV_420_888, 1);
            mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);
        }

        // 如果是双摄模式下  这里要加入请求参数
        if(info.getState() == CameraConstant.CameraState.CAMERA_DUAL_FRONT
            || info.getState() == CameraConstant.CameraState.CAMERA_DUAL_BACK){
            dualControl = new DualRequestKey();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                dualControl.setAllKeys(mCameraCharacteristics);
            }
        }

    }

    /**
     * 相机流程
     *
     * @throws CameraAccessException
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    private void createCameraPreviewSession() throws CameraAccessException {
        // 保证健壮性  ！！！ 这段代码不应该被调用
        if (surfaceTexture == null) {
            surfaceTexture = new SurfaceTexture(OpenGLUtils.getExternalOESTextureID());
            Log.d(TAG, "surfaceTexture == null !!! new surfaceTexture");
        }
        if (mSurface == null) mSurface = new Surface(surfaceTexture);
        if (frameBufferSurfaceTexture != null) {  // 需要离屏渲染
            mFrameBufferSurface = new Surface(frameBufferSurfaceTexture);
        }

        mCaptureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        OutputConfiguration surface = new OutputConfiguration(mSurface);  // 预览的surface

        OutputConfiguration imageSurface = new OutputConfiguration(mImageReader.getSurface());  // 拍照的surface
        mCaptureBuilder.addTarget(mSurface);
        if(mFrameBufferSurface != null) {
            mCaptureBuilder.addTarget(mFrameBufferSurface);
        }
        mCaptureBuilder.addTarget(mImageReader.getSurface());

        List<OutputConfiguration> outputConfigsAll = Arrays.asList(surface, imageSurface);
        if(mFrameBufferSurface != null){
            OutputConfiguration frameSurface = new OutputConfiguration(mFrameBufferSurface);
            outputConfigsAll = Arrays.asList(surface, frameSurface, imageSurface);
        }

        SessionConfiguration sessionConfiguration = new SessionConfiguration(
                SessionConfiguration.SESSION_REGULAR,
                outputConfigsAll,
                AsyncTask.SERIAL_EXECUTOR,
                new CameraCaptureSession.StateCallback() {
                    // 摄像头设备完成自身配置后，将调用此方法，并且会话可以开始处理捕获请求
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession session) {
                        if(mCameraDevice == null) return;

                        mCameraCaptureSession = session; // 拿到相机事物
                        // 初始化对焦控制
                        focusControl = new CameraFocusControl(mCameraDevice, mCameraCharacteristics, mCaptureBuilder, mCameraCaptureSession);
                        focusControl.setFocusCallBack(mFocusStateCallback);
                        // 这里可以设置一些事务 如： 设置对焦模式 设置曝光模式
                        //自动对焦
                        mCaptureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO);
                        // 设置自动曝光
                        mCaptureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest
                                .CONTROL_AE_MODE_ON_AUTO_FLASH);

                        // 如果是双摄模式 这里要记录信息
                        if((cameraInfo.getState() == CameraConstant.CameraState.CAMERA_DUAL_FRONT
                                || cameraInfo.getState() == CameraConstant.CameraState.CAMERA_DUAL_BACK)
                                && dualControl != null) {
                            dualControl.setSpecialVendorTag(mCaptureBuilder);
                        }

                        mCaptureRequest = mCaptureBuilder.build();
                        try {
                            mCameraCaptureSession.setRepeatingRequest(mCaptureRequest, mCaptureCallbackListener, mBackgroundHandler);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                        // 如果无法根据请求配置会话，则调用此方法。
                        Log.d(TAG, "onConfigureFailed");
                    }
                });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // 创建相机会话
            mCameraDevice.createCaptureSession(sessionConfiguration);
        }
    }

    /**
     * 修改对焦模式后 需要调用这个方法
     */
    private void repeatingRequest() {
        //使用此方法，摄像头设备将使用提供的设置以CaptureRequest最大可能的速率连续拍摄图像
        mCaptureRequest = mCaptureBuilder.build();
        try {
            mCameraCaptureSession.setRepeatingRequest(mCaptureRequest, mCaptureCallbackListener, mBackgroundHandler);  //设置预览界面;  //设置预览界面
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {

        }
    }

    // 这段代码来自  https://github.com/googlearchive/android-Camera2Basic
    private static final int STATE_PREVIEW = 0; //                   Camera state: Showing camera preview.
    private static final int STATE_WAITING_LOCK = 1;//               Camera state: Waiting for the focus to be locked.
    private static final int STATE_WAITING_PRE_CAPTURE = 2;//        Camera state: Waiting for the exposure to be precapture state.
    private static final int STATE_WAITING_NON_PRE_CAPTURE = 3;//    Camera state: Waiting for the exposure state to be something other than precapture.
    private static final int STATE_PICTURE_TAKEN = 4; //             Camera state: Picture was taken.
    private int mState = STATE_PREVIEW;
    /**
     * 一个回调对象，用于跟踪CaptureRequest提交给摄像头设备的进度
     */
    CameraCaptureSession.CaptureCallback mCaptureCallbackListener = new CameraCaptureSession.CaptureCallback() {
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

        /**
         * This method is called when an image capture makes partial forward progress; some (but not all) results from an image capture are available.
         * @param session 此值返回的会话不能为null
         * @param request 向CameraDevice发出的请求
         * @param partialResult 捕获中的部分输出元数据，其中包括TotalCaptureResult字段的子集。此值不能为null
         */
        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull  //当图像捕获部分进行时就会回调该方法，此时一些(但不是全部)结果是可用的
                CaptureRequest request, @NonNull CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
            process(partialResult);
            focusControl.updateFocusState(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull  //当图像捕捉完全完成时，并且结果已经可用时回调该方法
                CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            process(result);
            focusControl.updateFocusState(result);
        }

        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull  //当相机设备产生 TotalCaptureResult 失败时就回调该方法
                CaptureRequest request, @NonNull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
            Log.w(TAG, "onCaptureFailed reason:" + failure.getReason());
        }
    };

    /**
     * 外部数据构造器
     */
    public static class CameraEngineBuilder {

        Context context;
        private FocusStateCallback mFocusStateCallback;//                                                     对焦模式变化接口
        private ImageReader.OnImageAvailableListener mOnImageAvailableListener; //                            相机预览数据的回调接口
        private SurfaceTexture mSurfaceTexture;//                                                             相机纹理信息
        private SurfaceTexture mFrameSurfaceTexture; //                                                        离屏渲染纹理信息  （不一定有）
        private CameraErrorCallBack cameraCallBack;//                                                         相机运行时的一些报错

        public CameraEngineBuilder(Context context) {
            this.context = context;
        }

        public CameraEngineBuilder setmFocusStateCallback(FocusStateCallback mFocusStateCallback) {
            this.mFocusStateCallback = mFocusStateCallback;
            return this;
        }

        public CameraEngineBuilder setmOnImageAvailableListener(ImageReader.OnImageAvailableListener mOnImageAvailableListener) {
            this.mOnImageAvailableListener = mOnImageAvailableListener;
            return this;
        }

        public CameraEngineBuilder setmSurfaceTexture(SurfaceTexture mSurfaceTexture) {
            this.mSurfaceTexture = mSurfaceTexture;
            return this;
        }

        public CameraEngineBuilder setCameraCallBack(CameraErrorCallBack cameraCallBack) {
            this.cameraCallBack = cameraCallBack;
            return this;
        }

        public CameraEngineBuilder setFrameSurfaceTexture(SurfaceTexture frameSurfaceTexture) {
            this.mFrameSurfaceTexture = frameSurfaceTexture;
            return this;
        }

        public CameraEngine build() {
            return new CameraEngine(context, this);
        }
    }
}
