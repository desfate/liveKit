package github.com.desfate.livekit.camera.news;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.params.MeteringRectangle;
import android.os.Handler;
import android.view.Surface;

import github.com.desfate.livekit.camera.interfaces.FocusStateCallback;

/**
 * 管理相机的对焦逻辑
 *
 * CaptureResult.CONTROL_AF_STATE 自动对焦状态分类
 * https://developer.android.com/reference/android/hardware/camera2/CaptureResult#CONTROL_AF_MODE
 * 自动调焦程序不控制镜头                                       CONTROL_AF_MODE_OFF  value = 0
 * 基本的自动对焦模式                                          CONTROL_AF_MODE_AUTO  value = 1
 * 特写对焦模式                                               CONTROL_AF_MODE_MACRO value = 2
 * AF算法会连续修改镜头位置，以尝试提供持续对焦的图像流(Video)     CONTROL_AF_MODE_CONTINUOUS_VIDEO value = 3
 * AF算法会连续修改镜头位置，以尝试提供持续对焦的图像流(PICTURE)   CONTROL_AF_MODE_CONTINUOUS_PICTURE value = 4
 * 扩展景深（数字聚焦）模式 摄像头设备将自动生成具有扩展景深的图像；拍照前无需进行特殊的对焦操作 CONTROL_AF_MODE_EDOF value = 5
 */
public class CameraFocusControl {

    public final static int AF = 0; // 自动对焦
    public final static int MF = 1; // 手动对焦

    private int focusMode = AF;
    private int mLatestAfState; //                                            对焦模式
    private MeteringRectangle[] mResetRect = //                               自动对焦的对焦区域
            new MeteringRectangle[]{
                    new MeteringRectangle(0, 0, 0, 0, 0)
            };

    CameraDevice mCameraDevice;
    CameraCharacteristics mCameraCharacteristics;
    CaptureRequest.Builder mCaptureBuilder;
    CameraCaptureSession mCameraCaptureSession;

    private FocusStateCallback mFocusStateCallback;//     对焦状态回调

    public CameraFocusControl(CameraDevice mCameraDevice, CameraCharacteristics mCameraCharacteristics,  CaptureRequest.Builder mCaptureBuilder, CameraCaptureSession mCameraCaptureSession){
        this.mCameraDevice = mCameraDevice;
        this.mCameraCharacteristics = mCameraCharacteristics;
        this.mCaptureBuilder = mCaptureBuilder;
        this.mCameraCaptureSession = mCameraCaptureSession;
    }

    public void setFocusCallBack(FocusStateCallback mFocusStateCallback){
        this.mFocusStateCallback = mFocusStateCallback;
    }

    /**
     * 对焦模式更新
     * @param partialResult 相机返回的信息
     */
    public void updateFocusState(CaptureResult partialResult){
        Integer state = partialResult.get(CaptureResult.CONTROL_AF_STATE);  // 自动对焦（AF）算法的当前状态。
        if (state != null && mLatestAfState != state) {
            mLatestAfState = state;  // 保存当前对焦状态
            if (state != CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO && mFocusStateCallback != null){  // 如果不是连续视频对焦模式 则通知上层 对焦模式变化
                mFocusStateCallback.focusChanged(state);
            }
        }
    }

    /**
     * 设置自动对焦模式
     */
    public CaptureRequest.Builder setAFState(){
        focusMode = AF;
        // 本相机设备支持的自动对焦（AF）模式列表
        int[] allAFMode = mCameraCharacteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
        int mAFMode = 0;
        if(allAFMode.length > 0)
            mAFMode = allAFMode[0];
        for (int afMode : allAFMode){
            if (afMode == CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE) {
                mAFMode = CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE;
                break;
            }
        }
        mCaptureBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO); // 3A模式（自动曝光、自动白平衡、自动对焦
        mCaptureBuilder.set(CaptureRequest.CONTROL_AF_MODE, mAFMode);  // 检查相机是否支持手动对焦模式
        mCaptureBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, mResetRect);
        mCaptureBuilder.set(CaptureRequest.CONTROL_AE_REGIONS, mResetRect);
        // cancel af trigger
        mCaptureBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_IDLE);
        return mCaptureBuilder;
    }

    /**
     * 手动对焦
     * @param focusRect 点击区域
     * @param meteringRect 曝光区域
     * @param surface 手动的surface
     * @param mCaptureCallbackListener 用于跟踪CaptureRequest提交给摄像头设备的进度
     * @param mBackgroundHandler 后台线程
     */
    public void setMFState(MeteringRectangle focusRect, MeteringRectangle meteringRect, Surface surface, CameraCaptureSession.CaptureCallback mCaptureCallbackListener, Handler mBackgroundHandler) {
        focusMode = MF;
        try {
            CaptureRequest.Builder builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            builder.addTarget(surface);
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
            mCameraCaptureSession.setRepeatingRequest(request, mCaptureCallbackListener, mBackgroundHandler);
            builder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START); //触发对焦
            mCameraCaptureSession.capture(builder.build(), null, mBackgroundHandler); //发送上述设置的对焦请求，并监听回调
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (IllegalStateException e){

        }
    }

    public int getFocusState(){
        return mLatestAfState;
    }

}
