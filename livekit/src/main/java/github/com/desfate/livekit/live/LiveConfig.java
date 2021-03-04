package github.com.desfate.livekit.live;

import github.com.desfate.livekit.utils.LiveSupportUtils;

public class LiveConfig {

    public final static int LIVE_PUSH_DATA = 1; // 直接推送图片数据
    public final static int LIVE_PUSH_TEXTURE = 2;  // 根据textureId进行推送

    public final static int LIVE_CAMERA_FRONT = 1;  //  前置推流
    public final static int LIVE_CAMERA_BACK  = 2;  //  后置推流
    public final static int LIVE_CAMERA_OUTS  = 3;  //  外部推流
    public final static int LIVE_CAMERA_DUAL  = 4;  //  双摄推流

    private int pushCameraType = LIVE_CAMERA_FRONT;

    private int livePushType = LIVE_PUSH_DATA;

    private int liveQuality = LiveSupportUtils.LIVE_SIZE_1080; // 当前直播的画面质量

    public int getLiveQuality() {
        return liveQuality;
    }

    public void setLiveQuality(int liveQuality) {
        this.liveQuality = liveQuality;
    }

    public int getLivePushType() {
        return livePushType;
    }

    public void setLivePushType(int livePushType) {
        this.livePushType = livePushType;
    }

    public int getPushCameraType() {
        return pushCameraType;
    }

    public void setPushCameraType(int pushCameraType) {
        this.pushCameraType = pushCameraType;
    }

    public LiveConfig switchCameraFrontAndBack(){
        if(pushCameraType == LIVE_CAMERA_FRONT) pushCameraType = LIVE_CAMERA_BACK;
        else if(pushCameraType == LIVE_CAMERA_BACK) pushCameraType = LIVE_CAMERA_FRONT;
        return this;
    }
}
