package github.com.desfate.livekit.dual;

/**
 * 3d模式下的控制器功能
 */
public interface MPreviewControl {
    /**
     * 是否进行3d绘制
     * @param isDraw true： 3d绘制 false： 直接绘制预览
     */
    void isDraw3D(boolean isDraw);

    /**
     * 用户切换相机  预览也要进行相应的切换
     */
    void switchCamera();




}
