package github.com.desfate.livekit.live;

/**
 * 直播行为接口 （对外）
 */
public interface MLiveControl {

    /**
     * 开启推送
     */
    void startPush();

    /**
     * 切换相机 推送也要针对相应的切换进行转换
     */
    void switchCamera();

    /**
     * 当前摄像头状态
     * @return true： 前置  false： 后置
     */
    boolean isFront();

    /**
     * 停止推送
     */
    void stopPush();

    /**
     * 释放资源
     */
    void releaseRes();
}
