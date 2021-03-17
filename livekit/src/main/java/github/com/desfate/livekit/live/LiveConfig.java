package github.com.desfate.livekit.live;

import github.com.desfate.livekit.LiveConstant;
import github.com.desfate.livekit.utils.LiveSupportUtils;

public class LiveConfig {

    private int pushCameraType = LiveConstant.LIVE_CAMERA_FRONT;

    private int livePushType = LiveConstant.LIVE_PUSH_DATA;

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

    /**
     * 切换相机前后置
     * @return 切换后的配置
     */
    public LiveConfig switchCameraFrontAndBack(){
        if(pushCameraType == LiveConstant.LIVE_CAMERA_FRONT) pushCameraType = LiveConstant.LIVE_CAMERA_BACK;
        else if(pushCameraType == LiveConstant.LIVE_CAMERA_BACK) pushCameraType = LiveConstant.LIVE_CAMERA_FRONT;
        return this;
    }
}
