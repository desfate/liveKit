package github.com.desfate.livekit.live;

/**
 * 用于给腾讯云上传直播页面的接口
 */
public interface LiveCallBack {
    /**
     * 根据每帧数据的视频推送
     * @param buffer
     * @param w
     * @param h
     */
    void startPushByData(byte[] buffer, int w, int h);

    /**
     * 发送自己采集的 texture 视频数据
     * @param textureID
     * @param w
     * @param h
     */
    void startPushByTextureId(int textureID, int w, int h);
}
