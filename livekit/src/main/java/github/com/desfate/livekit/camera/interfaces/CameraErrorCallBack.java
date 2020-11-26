package github.com.desfate.livekit.camera.interfaces;

import github.com.desfate.livekit.camera.news.CameraInfo;

public interface CameraErrorCallBack {

    void onCameraOpenSuccess(CameraInfo info);

    void onCameraOpenError(CameraInfo info, int error);
}
