package github.com.desfate.livekit;

import android.util.Size;

import github.com.desfate.livekit.camera.news.CameraInfo;
import github.com.desfate.livekit.dual.M3dConfig;

/**
 *
 * 我负责适配
 *
 * 之前写的配置太多了
 * 这里进行一次重新整理
 * 以后获取配置信息只从这里获取
 *
 * fixme 这里有几个需求  1. 用户只关心直播配置  通过直播配置  这里要转换成相机的相应配置 LiveConfig ---> CameraInfo
 *
 *
 *
 * 总共会有集中情况 但是开启相机总共是情况
 * 1.  开启前置相机
 * 2.  开启后置相机
 * 3.  开启前置双摄
 * 4.  开启后置双摄
 *
 */
public class CameraAdapter {

    /**
     * 把直播配置适配成相机配置
     * @param config 直播配置
     * @return 相机配置
     */
    public static CameraInfo liveConfigToCameraInfo(LiveConfig config){
        if(config == null) return null;
        Size cameraSize = new Size(0,0);
        int logicCameraId = 0;
        // 首先确定当前直播类型
        if(config.isDual()){
            // 这里矫正一下预览的值
            CameraSetting.getInstance().setPreviewType(config.getPushCameraType());
            // 当前是双摄模式 传入的相机Size就需要适配对应的Size
            cameraSize = M3dConfig.getCameraSettingSize();
            // 获得逻辑id
            logicCameraId = config.getLogicCameraId();
        }
        return new CameraInfo.CameraBuilder()
                .setDefaultBufferSize(cameraSize)
                .setImageBufferSize(cameraSize)
                .setLogicCameraId(logicCameraId)
                .setState(liveStateToCameraState(config.getPushCameraType()))
                .build();
    }

    /**
     * 将直播的类型转换为相机配置的类型
     * @param liveCameraType 直播的类型
     * @return 相机的类型
     */
    private static CameraConstant.CameraState liveStateToCameraState(LiveConstant.LiveCameraType liveCameraType){
        if(liveCameraType == LiveConstant.LiveCameraType.CAMERA_FRONT){
            // 前置推流
            return CameraConstant.CameraState.CAMERA_FRONT;
        }else if(liveCameraType == LiveConstant.LiveCameraType.CAMERA_BACK){
            // 后置推流
            return CameraConstant.CameraState.CAMERA_BACK;
        }else if(liveCameraType == LiveConstant.LiveCameraType.CAMERA_DUAL_FRONT){
            // 前置双摄
            return CameraConstant.CameraState.CAMERA_DUAL_FRONT;
        }else if(liveCameraType == LiveConstant.LiveCameraType.CAMERA_DUAL_BACK){
            // 后置双摄
            return CameraConstant.CameraState.CAMERA_DUAL_BACK;
        }else {
            return CameraConstant.CameraState.CAMERA_DUAL_BACK;
        }
    }






}
