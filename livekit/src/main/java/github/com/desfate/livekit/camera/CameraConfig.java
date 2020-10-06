package github.com.desfate.livekit.camera;

public class CameraConfig {
    // 使用的相机是前置还是后置 true： 前置  false： 后置
    private boolean isFrontCamera = true;  // true: 竖屏  false：横屏

    private int mCameraId = 1; // 选择的相机物理ID

    public boolean isFrontCamera() {
        return isFrontCamera;
    }

    public void setFrontCamera(boolean frontCamera) {
        isFrontCamera = frontCamera;
    }

    public int getmCameraId() {
        return mCameraId;
    }

    public void setmCameraId(int mCameraId) {
        this.mCameraId = mCameraId;
    }
}
