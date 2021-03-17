package github.com.desfate.livekit;

public final class CameraConstant {

    /**
     * 对焦动画相关配置
     */
    public static final int radiusOuterSize = 50; // dp
    public static final int radiusInnerSize = 30; // dp
    public static final int strokeWidthSize = 2;  // dp

    /**
     * 相机摄像头相关配置
     */
    public static final int CAMERA_FRONT = 2; // 前置
    public static final int CAMERA_BACK  = 1; // 后置
    public static final int CAMERA_OUT   = 3; // 外部

    public static final int CAMERA_STATE_DUAL = 1; // 双摄
    public static final int CAMERA_STATE_NORMAL = 0; // 普通预览模式

    public static int DUAL_LOGIC_CAMERA_ID = 3;
    public static int DUAL_LOGIC_CAMERA_FRONT_ID = 1;
    public static int DEFAULT_LOGIC_CAMERA_ID = 0;

}
