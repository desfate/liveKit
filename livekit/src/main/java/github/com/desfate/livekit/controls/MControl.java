package github.com.desfate.livekit.controls;

public interface MControl {

    /**
     * 获取当前相机的状态
     * @return true:前置相机 false：后置相机
     */
    boolean isCameraFront();

    /**
     * 切换相机
     */
    void switchCamera();

    /**
     * 开始推流
     */
    void startPush();

    /**
     * 开启预览
     */
    void startPreview();

    /**
     * 释放资源
     */
    void release();

}
