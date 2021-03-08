package github.com.desfate.livekit.dual;

/**
 * 用于记录一些配置  例如3d相机id
 * 本身是单例模式
 */
public class CameraSetting {

    public static String M3D_CAMERA_ID = "3";   // 3d 模式下使用的相机ID （后置）  这里指示的是逻辑相机id

    public static volatile CameraSetting instance;

    // 需要本地记录的一些参数
    private M3dConfig.Preview_type previewType = M3dConfig.Preview_type.PREVIEW_16TO9; // 配置预览参数  默认4:3

    public static CameraSetting getInstance(){
        if(instance == null) {
            synchronized (CameraSetting.class) {
                if (instance == null) {
                    instance = new CameraSetting();
                }
            }
        }
        return instance;
    }

    public M3dConfig.Preview_type getPreviewType() {
        return previewType;
    }

    public void setPreviewType(M3dConfig.Preview_type previewType) {
        this.previewType = previewType;
    }
}
