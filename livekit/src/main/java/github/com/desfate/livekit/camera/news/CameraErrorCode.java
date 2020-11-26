package github.com.desfate.livekit.camera.news;

public class CameraErrorCode {

    /***************************************  CameraManager.openCamera  onError() 回调错误 *************************************/
    //  from https://developer.android.com/reference/android/hardware/camera2/CameraDevice.StateCallback
    // 指示相机设备已在使用中来报告错误代码。
    public final static int ERROR_CAMERA_IN_USE = 1;
    // 已达到系统范围内打开的摄像头数量的限制，在关闭以前的实例之前，无法打开更多的摄像头设备
    public final static int ERROR_MAX_CAMERAS_IN_USE = 2;
    // 指示由于设备策略而无法打开相机设备来报告错误代码。
    public final static int ERROR_CAMERA_DISABLED = 3;
    // 指示相机设备遇到致命错误来报告错误代码
    public final static int ERROR_CAMERA_DEVICE = 4;
    // 指示相机服务遇到致命错误来报告错误代码。
    public final static int ERROR_CAMERA_SERVICE = 5;

    public static String errorCamera(int type){
        switch (type){
            case ERROR_CAMERA_IN_USE:
                return "相机设备已在使用中";
            case ERROR_MAX_CAMERAS_IN_USE:
                return "已达到系统范围内打开的摄像头数量的限制";
            case ERROR_CAMERA_DISABLED:
                return "由于设备策略而无法打开相机设备";
            case ERROR_CAMERA_DEVICE:
                return "相机设备遇到致命错误";
            case ERROR_CAMERA_SERVICE:
                return "相机服务遇到致命错误";
        }
        return "";
    }

}
