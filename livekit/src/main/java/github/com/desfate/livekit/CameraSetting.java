package github.com.desfate.livekit;

import github.com.desfate.livekit.dual.M3dConfig;

/**
 * 本身是单例模式
 * 这里配置的是一些共享参数
 * 因为通过单例生成  大家都能访问到
 *
 * 现阶段也只支持3d模式直播拍摄
 *
 */
public class CameraSetting {

    public static volatile CameraSetting instance;

    // 需要本地记录的一些参数
    // 这个是决定了开启3d模式相机的一系列参数 普通模式下照理不会访问
    private M3dConfig.Preview_type previewType = M3dConfig.Preview_type.PREVIEW_16TO9_DUAL; // 配置预览参数

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

    /**
     * fixme 照理这个值不需要矫正 但是涉及到前后摄像头切换 这个值也会改变
     * 这里也可以通过当前直播类型矫正预览值
     * @param type 直播类型
     */
    public void setPreviewType(LiveConstant.LiveCameraType type){
        if(type == LiveConstant.LiveCameraType.CAMERA_DUAL_FRONT) {  // 双摄前置
            previewType = M3dConfig.Preview_type.PREVIEW_9TO16_DUAL;
        } else if(type == LiveConstant.LiveCameraType.CAMERA_DUAL_BACK){
            previewType = M3dConfig.Preview_type.PREVIEW_16TO9_DUAL;
        }
    }
}
