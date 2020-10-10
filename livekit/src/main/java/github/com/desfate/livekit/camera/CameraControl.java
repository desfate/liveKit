package github.com.desfate.livekit.camera;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.params.MeteringRectangle;
import android.media.Image;
import android.media.ImageReader;
import android.os.Looper;

import java.util.concurrent.locks.ReentrantLock;

import github.com.desfate.livekit.BaseLiveView;
import github.com.desfate.livekit.camera.interfaces.CameraChangeCallback;
import github.com.desfate.livekit.camera.interfaces.FocusCallback;
import github.com.desfate.livekit.camera.interfaces.FocusStateCallback;
import github.com.desfate.livekit.camera.view.FocusView;
import github.com.desfate.livekit.live.LiveConfig;
import github.com.desfate.livekit.live.LiveManager;
import github.com.desfate.livekit.utils.ImageUtil;
import github.com.desfate.livekit.utils.JobExecutor;


/**
 * GlsurfaceView 和 Camera 相关不直接关联  通过control实现
 */
public class CameraControl {

       private final static String TAG = "CameraControl";

    LiveManager mLiveManager; //            直播管理器 用于管理直播相关接口
    BaseLiveView mBaseLiveView; //          显示的view 有自己的SurfaceTextureView 和 Surface
    CameraSession mCameraSession; //        相机会话 管理相机相关对象
    FocusControl mFocusControl; //          对焦管理
    private JobExecutor mJobExecutor;//     线程池

    Context mContext;

    boolean isPusher = false;  // 是否开始推流


    public CameraControl(Context context, BaseLiveView mBaseLiveView, FocusView focusView) {
        this.mContext = context;
        this.mBaseLiveView = mBaseLiveView;
        mJobExecutor = new JobExecutor();
        mCameraSession = new CameraSession(context, new FocusStateCallback() {
            @Override
            public void focusChanged(final int state) {
                mJobExecutor.execute(new JobExecutor.Task<Void>() {
                    @Override
                    public void onMainThread(Void result) {
                        super.onMainThread(result);
                        //对焦状态的更变  这边动画根据对焦状态进行相应的变化
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
                });
            }
        });
        mFocusControl = new FocusControl(focusView, Looper.getMainLooper(), new FocusCallback() {
            @Override
            public void focusFinish() {
                if (!mCameraSession.getFocusState()) {
                    mCameraSession.sendControlFocusModeRequest();
                }
            }
        });
    }

    /**
     * 打开相机
     */

    public void openCamera(CameraChangeCallback cameraChangeCallback) {
        // 设置surface
        mCameraSession.addSurface(mBaseLiveView.getmSurface(), mBaseLiveView.getmSurfaceTexture());
        // 获取视频数据 并转换成yuv420p（yu12）
        mCameraSession.setImageReaderListener(new ImageReader.OnImageAvailableListener() {
            private byte[] y;
            private byte[] u;
            private byte[] v;
            private byte[] nv21;
            private ReentrantLock lock = new ReentrantLock();  //锁是为了保证每次操作的都是同一个image


            @Override
            public void onImageAvailable(ImageReader reader) {
                if (!isPusher) return;
                if (mCameraSession == null) return;
                //将Y:U:V == 4:1:1的数据转换为 yu12（I420）
                Image image = reader.acquireNextImage(); //这个必须要有  不然会导致卡死
                switch (mCameraSession.getmLiveConfig().getLivePushType()) {
                    case LiveConfig.LIVE_PUSH_DATA:
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
                            if (mLiveManager != null)
                                mLiveManager.startPushByData(nv21, stride, image.getHeight());   // 向服务器推送数据
//                    Optional.ofNullable(mLiveManager).ifPresent(liveManager -> {
//                        liveManager.startPushByData(nv21, stride, image.getHeight());   // 向服务器推送数据
//                    });
                            lock.unlock();
                        }
                        break;
                    case LiveConfig.LIVE_PUSH_TEXTURE:
                        // 根据texture进行推送
                        if (mLiveManager != null)
                            mLiveManager.startPushByTextureId(mBaseLiveView.getmSurfaceId(), image.getWidth(), image.getHeight());
                        break;
                }
                image.close();
            }
        });
        mCameraSession.openCamera(cameraChangeCallback);  // 打开摄像头
    }

    /**
     * 切换相机
     */

    public void switchCamera() {
        mCameraSession.switchCamera();
    }

    public void startPush() {
        isPusher = true;
    }

    public void stopPush() {
        isPusher = false;
    }

    public void startFocus(float x, float y) {
        mFocusControl.startFocus(x, y);
    }


    public MeteringRectangle getFocusArea(float x, float y, boolean type) {
        return mFocusControl.getFocusArea(x, y, type);
    }

    public void setmLiveManager(LiveManager liveManager) {
        this.mLiveManager = liveManager;
    }


    public void cameraAFSetting(MeteringRectangle focusRect, MeteringRectangle meterRect) {
        mCameraSession.cameraAFSetting(focusRect, meterRect);
    }

    /**
     * 画面更变导致的对焦区域更新
     *
     * @param width
     * @param height
     */

    public void focusChanged(int width, int height) {
        mFocusControl.onPreviewChanged(width, height, mCameraSession.getCameraCharacteristics());
    }


    public boolean getCameraStata() {
        return mCameraSession.getCameraState();
    }


    public void release() {
        mCameraSession.release();
    }

    public void setLiveConfig(LiveConfig liveConfig) {
        if (liveConfig != null)
            mCameraSession.setmLiveConfig(liveConfig);
    }
}
