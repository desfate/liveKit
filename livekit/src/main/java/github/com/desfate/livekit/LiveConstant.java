package github.com.desfate.livekit;

/**
 * 直播配置
 */
public class LiveConstant {

    /**
     * 推流类型枚举
     */
    public enum LivePushType{
        DATA,    //     直接推送图片数据
        TEXTURE  //     根据textureId进行推送
    }

    /**
     * 当前直播类型
     */
    public enum LiveCameraType{
        CAMERA_FRONT,     //   前置推流
        CAMERA_BACK,       //  后置推流
        CAMERA_DUAL_FRONT, //  前置双摄推流
        CAMERA_DUAL_BACK//     双摄推流（后置）
    }

    /**
     * 直播质量
     */
    public enum LiveQuality{
        LIVE_1080P,   // 推流清晰度 1920 * 1080 / 1080 * 1920
        LIVE_720P     // 推流清晰度 1280 * 720  / 720  * 1280
    }


}
