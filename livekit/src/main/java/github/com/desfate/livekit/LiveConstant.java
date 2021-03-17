package github.com.desfate.livekit;

public class LiveConstant {

    public final static int LIVE_PUSH_DATA = 1; // 直接推送图片数据
    public final static int LIVE_PUSH_TEXTURE = 2;  // 根据textureId进行推送

    public final static int LIVE_CAMERA_FRONT = 1;  //  前置推流
    public final static int LIVE_CAMERA_BACK  = 2;  //  后置推流
    public final static int LIVE_CAMERA_OUTS  = 3;  //  外部推流
    public final static int LIVE_CAMERA_DUAL  = 4;  //  双摄推流（后置）
    public final static int LIVE_CAMERA_DUAL_FRONT = 5; //  前置双摄推流

}
