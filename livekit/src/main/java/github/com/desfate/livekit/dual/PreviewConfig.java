package github.com.desfate.livekit.dual;

public class PreviewConfig {
    private int isCameraFront = 1; // 1:后置 2：前置 3：外部摄像头
    private int state = 0;  //        0:普通预览模式 1： 双摄预览模式
    private Preview_Quality quality_type = Preview_Quality.DUAL;

    public enum Preview_Quality{
        LOW, MID, HEIGHT, DUAL
    }

    public int getIsCameraFront() {
        return isCameraFront;
    }

    public void setIsCameraFront(int isCameraFront) {
        this.isCameraFront = isCameraFront;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public Preview_Quality getQuality_type() {
        return quality_type;
    }

    public void setQuality_type(Preview_Quality quality_type) {
        this.quality_type = quality_type;
    }
}
