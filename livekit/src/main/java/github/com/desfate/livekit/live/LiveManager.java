package github.com.desfate.livekit.live;

/**
 * 这里包含一些和直播sdk相关的接口  用于解耦直播相关
 */
public interface LiveManager {

    /**
     * 根据每帧数据的视频推送
     * @param buffer
     * @param w
     * @param h
     */
     void startPushByData(byte[] buffer, int w, int h);
}
