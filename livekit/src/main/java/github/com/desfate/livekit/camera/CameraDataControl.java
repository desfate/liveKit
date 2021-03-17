package github.com.desfate.livekit.camera;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.params.MeteringRectangle;
import android.media.Image;
import android.media.ImageReader;
import android.os.Looper;

import java.util.concurrent.locks.ReentrantLock;

import github.com.desfate.livekit.camera.interfaces.CameraErrorCallBack;
import github.com.desfate.livekit.camera.interfaces.FocusCallback;
import github.com.desfate.livekit.camera.interfaces.FocusStateCallback;
import github.com.desfate.livekit.camera.news.CameraClient;
import github.com.desfate.livekit.camera.news.CameraFocusControl;
import github.com.desfate.livekit.camera.news.CameraInfo;
import github.com.desfate.livekit.ui.FocusView;
import github.com.desfate.livekit.live.LiveCallBack;
import github.com.desfate.livekit.live.LivePushInterface;
import github.com.desfate.livekit.utils.ImageUtil;
import github.com.desfate.livekit.utils.JobExecutor;


/**
 * GlsurfaceView 和 Camera 相关不直接关联  通过control实现
 */
public class CameraDataControl implements LivePushInterface {

    private final static String TAG = "CameraControl";

    CameraClient mCameraClient; //          相机对象
    FocusControl mFocusControl; //          对焦管理
    private JobExecutor mJobExecutor;//     线程池
    CameraInfo info;
    boolean isPusher = false;  //           是否开始推流
    LiveCallBack liveCallBack;

    // 相机获取的每帧数据
    ImageReader.OnImageAvailableListener mImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        private byte[] y;
        private byte[] u;
        private byte[] v;
        private byte[] nv21;
        private final ReentrantLock lock = new ReentrantLock();  //锁是为了保证每次操作的都是同一个image
        @Override
        public void onImageAvailable(ImageReader imageReader) {
            //将Y:U:V == 4:1:1的数据转换为 yu12（I420）
            Image image = imageReader.acquireNextImage(); //这个必须要有  不然会导致卡死
            if (!isPusher) {
                image.close();
                return;
            }

            if (image.getFormat() == ImageFormat.YUV_420_888) {
                Image.Plane[] planes = image.getPlanes();
                // 加锁确保y、u、v来源于同一个Image
                lock.lock();
                if (y == null) {
                    y = new byte[planes[0].getBuffer().limit() - planes[0].getBuffer().position()];
                    u = new byte[planes[1].getBuffer().limit() - planes[1].getBuffer().position()];
                    v = new byte[planes[2].getBuffer().limit() - planes[2].getBuffer().position()];
                }
                if (image.getPlanes()[0].getBuffer().remaining() == y.length) {
                    planes[0].getBuffer().get(y);
                    planes[1].getBuffer().get(u);
                    planes[2].getBuffer().get(v);
                }
                int stride = planes[0].getRowStride();
                if (nv21 == null) {
                    nv21 = new byte[stride * image.getHeight() * 3 / 2];
                }
                // 采样率是4:2:2 这里照理不应该出现422采样率的 如果出现 将422转换为420
                if (y.length / u.length == 2) {
                    ImageUtil.yuv422ToYuv420p(y, u, v, nv21, stride, image.getHeight());
                }

                // 回传数据是YUV420 这里保证采样率是4:1:1
                if (y.length / u.length == 4) {
                    ImageUtil.yuv420ToYuv420p(y, u, v, nv21, stride, image.getHeight());
                }

                byte[] mNv21 =  new byte[stride * image.getHeight() * 3 / 4];
                boolean sign = true;
                int key = 0;
                for(byte a : nv21){
                    if(sign) {
                        if(key < mNv21.length) {
                            mNv21[key] = a;
                        }
                    }
                    key ++;
                    sign = !sign;
                }
                if(liveCallBack != null) liveCallBack.startPushByData(mNv21, stride / 2, image.getHeight());  // 向服务器推送数据
                lock.unlock();
                image.close();
            }
        }
    };

    // 相机对焦状态更变回调
    FocusStateCallback callback = new FocusStateCallback() {
        @Override
        public void focusChanged(final int state) {
            mJobExecutor.execute(new JobExecutor.Task<Void>() {
                @Override
                public void onMainThread(Void result) {
                    super.onMainThread(result);
                    focusStateChanged(state);
                }
            });
        }
    };

    public CameraDataControl(Context context, SurfaceTexture surfaceTexture, CameraInfo cameraInfo, final LiveCallBack liveCallBack, CameraErrorCallBack errorCallBack) {
        this.info = cameraInfo;
        this.liveCallBack = liveCallBack;
        mJobExecutor = new JobExecutor();
        mCameraClient = new CameraClient.CameraClientBuilder()
                .setContext(context)
                .setmOnImageAvailableListener(mImageAvailableListener)
                .setmFocusStateCallback(callback)
                .setSurfaceTexture(surfaceTexture)
                .setCameraErrorCallBack(errorCallBack)
                .build();
    }

    /*********************************************** 自定义对焦动画 *******************************************/
    /**
     * 对焦状态变化的回调
     * @param state
     */
    private void focusStateChanged(int state){
        //对焦状态的更变  这边动画根据对焦状态进行相应的变化
        if(mFocusControl == null) return;
        switch (state) {
            case CaptureResult.CONTROL_AF_STATE_ACTIVE_SCAN: // AF正在执行一个AF扫描，因为它是由AF触发器触发的。 value = 3
                mFocusControl.startFocus();
                break;
            case CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED: // AF未能成功地集中注意力，并且锁定了焦点。 value = 5
                mFocusControl.focusFailed();
                break;
            case CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED: // AF相信它是正确的并且锁定了焦点。 value = 4
            case CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED: // AF目前认为它是焦点，但可能在任何时候重新启动扫描。 value = 2
            case CaptureResult.CONTROL_AF_STATE_PASSIVE_UNFOCUSED:  // AF在没有找到焦点的情况下完成了被动扫描，并且可以在任何时候重新启动扫描。 value = 6
                mFocusControl.focusSuccess();
                break;
            case CaptureResult.CONTROL_AF_STATE_PASSIVE_SCAN:  // AF目前正在进行一种自动对焦模式的自动对焦，这是一种持续的自动对焦模式。 value = 1
                mFocusControl.autoFocus();
                break;
            case CaptureResult.CONTROL_AF_STATE_INACTIVE:  // AF已经关闭，或者还没有被要求扫描。 value = 0
                mFocusControl.hideFocusView();
                break;
        }
    }

    /**
     * 设置自定义对焦模块
     * @param focusView 对焦模块的view
     */
    @Override
    public FocusControl customerFocus(FocusView focusView){
        mFocusControl = new FocusControl(focusView, Looper.getMainLooper(), new FocusCallback() {
            @Override
            public void focusFinish() {
                if (mCameraClient.getCamera().focusState() == CameraFocusControl.MF) {
                    mCameraClient.getCamera().autoFocus();
                }
            }
        });
        return mFocusControl;
    }

    @Override
    public void focusViewChange(int width, int height) {
        focusChanged(width, height);
    }

    @Override
    public void startMFocus(float x, float y) {
        startFocus(x, y);
    }

    /**
     * 开始对焦
     * @param x 对焦位置 x 轴
     * @param y 对焦位置 y 轴
     */
    public void startFocus(float x, float y) {
        mFocusControl.startFocus(x, y);
        MeteringRectangle focusRect = mFocusControl.getFocusArea(x, y, true);
        MeteringRectangle meterRect = mFocusControl.getFocusArea(x, y, false);
        cameraMF(focusRect, meterRect);  //  设置手动对焦
    }

    /**
     * 设置手动对焦
     * @param focusRect
     * @param meterRect
     */
    public void cameraMF(MeteringRectangle focusRect, MeteringRectangle meterRect) {
        mCameraClient.getCamera().manualFocus(focusRect, meterRect);
    }

    /**
     * 画面更变导致的对焦区域更新
     *
     * @param width
     * @param height
     */

    public void focusChanged(int width, int height) {
        mFocusControl.onPreviewChanged(width, height, mCameraClient.getCamera().getCameraCharacteristics());
    }


    /****************************************************************  必须要实现的行为  ***************************************************/
    /**
     * 设置预览
     */
    @Override
    public void startPreview() {
        mCameraClient.getCamera().openCamera(this.info);
    }

    /**
     * 开始推流
     */
    @Override
    public void startPush() {
        isPusher = true;
    }

    /**
     * 切换前后摄像头
     * @param info
     */
    @Override
    public void switchCamera(CameraInfo info) {
        mCameraClient.getCamera().switchCamera(info);
    }

    /**
     * 停止上传
     */
    @Override
    public void stopPush() {
        isPusher = false;
    }

    /**
     * 释放资源防止内存泄露
     */
    @Override
    public void releaseRes() {
        mCameraClient.getCamera().releaseCamera();
    }

}
