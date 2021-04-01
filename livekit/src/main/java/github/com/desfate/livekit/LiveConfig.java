package github.com.desfate.livekit;

import github.com.desfate.livekit.utils.LiveSupportUtils;

/**
 * 我是直播配置  我是直播配置
 *
 * 我只关系几个类型  1.前置2d直播  2.后置2d直播  3.前置3d直播 4.后置3d直播
 *
 * 推流模式  我只关心 是通过textureid推流还是通过data推流
 *
 * 直播画面质量  如果没有意外一般都是使用 1080P
 *
 *
 */
public class LiveConfig {

    // 直播类型
    private LiveConstant.LiveCameraType pushCameraType = LiveConstant.LiveCameraType.CAMERA_DUAL_BACK;
    // 直播推流类型
    private LiveConstant.LivePushType livePushType = LiveConstant.LivePushType.DATA;
    // 直播视频质量
    private LiveConstant.LiveQuality liveQuality = LiveConstant.LiveQuality.LIVE_1080P; // 当前直播的画面质量

    /**
     * 是否是双摄模式
     * @return true 双摄 false 非双摄
     */
    public boolean isDual(){
        return pushCameraType == LiveConstant.LiveCameraType.CAMERA_DUAL_BACK
                || pushCameraType == LiveConstant.LiveCameraType.CAMERA_DUAL_FRONT;
    }

    /**
     * 是否是前置模式
     * @return true 前置 false 后置
     */
    public boolean isFront(){
        return pushCameraType == LiveConstant.LiveCameraType.CAMERA_FRONT
                || pushCameraType == LiveConstant.LiveCameraType.CAMERA_DUAL_FRONT;
    }

    /**
     * 通过当前配置获取已经配置好的逻辑id
     * @return 逻辑相机id
     */
    public int getLogicCameraId(){
        if(pushCameraType == LiveConstant.LiveCameraType.CAMERA_DUAL_BACK){
            return CameraConstant.DUAL_LOGIC_CAMERA_ID;
        }else if(pushCameraType == LiveConstant.LiveCameraType.CAMERA_DUAL_FRONT){
            return CameraConstant.DUAL_LOGIC_CAMERA_FRONT_ID;
        }else if(pushCameraType == LiveConstant.LiveCameraType.CAMERA_BACK){
            return CameraConstant.DEFAULT_LOGIC_CAMERA_ID;
        }else{
            return CameraConstant.DEFAULT_LOGIC_CAMERA_FRONT_ID;
        }
    }

    /**
     * 切换当前直播状态
     */
    public void switchCamera(){
        if(pushCameraType == LiveConstant.LiveCameraType.CAMERA_DUAL_BACK){
            pushCameraType = LiveConstant.LiveCameraType.CAMERA_DUAL_FRONT;
        }else if(pushCameraType == LiveConstant.LiveCameraType.CAMERA_DUAL_FRONT){
            pushCameraType = LiveConstant.LiveCameraType.CAMERA_DUAL_BACK;
        }else if(pushCameraType == LiveConstant.LiveCameraType.CAMERA_FRONT){
            pushCameraType = LiveConstant.LiveCameraType.CAMERA_BACK;
        }else{
            pushCameraType = LiveConstant.LiveCameraType.CAMERA_FRONT;
        }
        CameraSetting.getInstance().setPreviewType(getPushCameraType());
    }


    public LiveConstant.LiveCameraType getPushCameraType() {
        return pushCameraType;
    }

    public void setPushCameraType(LiveConstant.LiveCameraType pushCameraType) {
        this.pushCameraType = pushCameraType;
    }

    public LiveConstant.LivePushType getLivePushType() {
        return livePushType;
    }

    public void setLivePushType(LiveConstant.LivePushType livePushType) {
        this.livePushType = livePushType;
    }

    public LiveConstant.LiveQuality getLiveQuality() {
        return liveQuality;
    }

    public void setLiveQuality(LiveConstant.LiveQuality liveQuality) {
        this.liveQuality = liveQuality;
    }
}
