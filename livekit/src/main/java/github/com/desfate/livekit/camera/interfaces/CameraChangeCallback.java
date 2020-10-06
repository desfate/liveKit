package github.com.desfate.livekit.camera.interfaces;

import android.util.Size;

public interface CameraChangeCallback {
    /**
     * 预览画面大小出现变化
     * @param front
     * @param size
     */
    void viewChanged(boolean front, Size size);
}
