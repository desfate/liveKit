package github.com.desfate.livekit.live;

import github.com.desfate.livekit.utils.LiveSupportUtils;

public class LiveConfig {

    private int liveQuality = LiveSupportUtils.LIVE_SIZE_1080; // 当前直播的画面质量

    public int getLiveQuality() {
        return liveQuality;
    }

    public void setLiveQuality(int liveQuality) {
        this.liveQuality = liveQuality;
    }
}
