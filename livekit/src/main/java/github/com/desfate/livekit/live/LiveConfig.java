package github.com.desfate.livekit.live;

import github.com.desfate.livekit.utils.LiveSupportUtils;

public class LiveConfig {

    public final static int LIVE_PUSH_DATA = 1; // 直接推送图片数据
    public final static int LIVE_PUSH_TEXTURE = 2;  // 根据textureId进行推送


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
}
