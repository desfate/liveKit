package github.com.desfate.livekit;

/**
 * 相机的一系列固定值配置
 */
public final class CameraConstant {

    /**
     * 对焦动画相关配置
     */
    public static final int radiusOuterSize = 50; // dp
    public static final int radiusInnerSize = 30; // dp
    public static final int strokeWidthSize = 2;  // dp


    public static int DUAL_LOGIC_CAMERA_ID = 3;            //   这是双摄后置指定的逻辑ID
    public static int DUAL_LOGIC_CAMERA_FRONT_ID = 1;      //   这是双摄指定的前置逻辑ID
    public static int DEFAULT_LOGIC_CAMERA_ID = 0;         //   这是默认开启的普通相机ID （后置）
    public static int DEFAULT_LOGIC_CAMERA_FRONT_ID = 0;   //   这是默认开启的普通相机ID （前置）

    /**
     * 开启相机的状态
     */
    public enum CameraState{
        CAMERA_FRONT,           //               前置相机
        CAMERA_BACK,            //               后置相机
        CAMERA_DUAL_FRONT,      //               前置双摄
        CAMERA_DUAL_BACK        //               后置双摄
    }


}
