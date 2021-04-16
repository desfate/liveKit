package com.github.desfate.videokit.interfaces;

/**
 * 记录视频播放中的一些时间
 */
public interface VideoPlayInterface {

    /**
     * 开始播放通知
     */
    void startPlaying();

    /**
     * 播放完成
     */
    void endPlay();

    /**
     * 播放进度通知
     * @param progress 进度
     */
    void playProgress(int progress);

    void playProgressTime(long progress);

    /**
     * 开始缓存
     */
    void startCache();

    /**
     * 缓存进度通知
     * @param progress 进度
     */
    void cacheProgress(int progress);

    /**
     * 停止缓存
     */
    void stopCache();

}
