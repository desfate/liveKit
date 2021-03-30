package github.com.desfate.livekit.live;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.util.Log;
import android.util.Size;

import java.util.ArrayList;
import java.util.List;

import github.com.desfate.livekit.CameraConstant;
import github.com.desfate.livekit.LiveConfig;
import github.com.desfate.livekit.LiveConstant;
import github.com.desfate.livekit.camera.news.CameraInfo;
import github.com.desfate.livekit.camera.news.CameraUtils;
import github.com.desfate.livekit.dual.PreviewConfig;
import github.com.desfate.livekit.dual.PreviewUtils;
import github.com.desfate.livekit.utils.LiveSupportUtils;

/**
 * 直播的一些工具
 */
public class LiveUtils {

    private final static String TAG = "LiveUtils";

    /**
     * 根据直播配置转换成摄像头数据
     * @param liveConfig 直播配置
     * @return CameraInfo 摄像头数据
     */
    public static CameraInfo LiveToCameraAdapter(Context context, LiveConfig liveConfig){
        if(liveConfig == null) {
            Log.e(TAG, "liveConfig == null please set a value");
            return null;
        }
//        //  摄像头方向
//        int cameraFront = liveConfig.getPushCameraType();
//
//        boolean isFront = true;
//        if(LiveConstant.LIVE_CAMERA_FRONT == cameraFront){
//            isFront = true;
//        }else if(LiveConstant.LIVE_CAMERA_DUAL_FRONT == cameraFront){
//            isFront = true;
//        }else{
//            isFront = false;
//        }
//        //  直播需要的摄像头size
//        Size cameraSize =  LiveSupportUtils.getCameraBestSize(isFront, liveConfig.getLiveQuality());
//        int logicCameraId = liveLogicCameraId(context, cameraFront);
//
//        int state = CameraConstant.CAMERA_STATE_NORMAL;  // 普通
//        if(liveConfig.getPushCameraType() == LiveConstant.LIVE_CAMERA_DUAL){  // 双摄推流 使用默认3号逻辑摄像头
//            logicCameraId = CameraConstant.DUAL_LOGIC_CAMERA_ID;
//            state = CameraConstant.CAMERA_STATE_DUAL;  // 双摄
//            cameraSize = PreviewUtils.getPreviewSize(cameraFront == LiveConstant.LIVE_CAMERA_FRONT, PreviewConfig.Preview_Quality.DUAL);
//        } else if(liveConfig.getPushCameraType() == LiveConstant.LIVE_CAMERA_DUAL_FRONT){  // 前置双摄推流
//            logicCameraId = CameraConstant.DUAL_LOGIC_CAMERA_FRONT_ID;
//            state = CameraConstant.CAMERA_STATE_DUAL;
//            cameraSize = PreviewUtils.getPreviewSize(cameraFront == LiveConstant.LIVE_CAMERA_FRONT, PreviewConfig.Preview_Quality.DUAL);
//        }
//
//        return new CameraInfo.CameraBuilder()
//                .setCameraFront(cameraFront)
//                .setDefaultBufferSize(cameraSize)
//                .setImageBufferSize(cameraSize)
//                .setLogicCameraId(logicCameraId)
//                .setState(state)
//                .build();
        return null;
    }

    /**
     * 根据直播配置转换成摄像头数据
     * @param liveConfig 直播配置
     * @return CameraInfo 摄像头数据
     */
    public static CameraInfo LiveTextureCameraAdapter(Context context, LiveConfig liveConfig){
        if(liveConfig == null) {
            Log.e(TAG, "liveConfig == null please set a value");
            return null;
        }
//        //  摄像头方向
//        int cameraFront = liveConfig.getPushCameraType();
//        //  直播需要的摄像头size
//        Size cameraSize =  LiveSupportUtils.getCameraTextureSize(cameraFront == LiveConstant.LIVE_CAMERA_FRONT, liveConfig.getLiveQuality());
//        int logicCameraId = liveLogicCameraId(context, cameraFront);
//
//        int state = CameraConstant.CAMERA_STATE_NORMAL;  // 普通
//        if(liveConfig.getPushCameraType() == LiveConstant.LIVE_CAMERA_DUAL){  // 双摄推流 使用默认3号逻辑摄像头
//            logicCameraId = CameraConstant.DUAL_LOGIC_CAMERA_ID;
//            state = CameraConstant.CAMERA_STATE_DUAL;  // 双摄
//            cameraSize = PreviewUtils.getPreviewSize(cameraFront == LiveConstant.LIVE_CAMERA_FRONT, PreviewConfig.Preview_Quality.DUAL);
//        }else if(liveConfig.getPushCameraType() == LiveConstant.LIVE_CAMERA_DUAL_FRONT){  // 前置双摄推流
//            logicCameraId = CameraConstant.DUAL_LOGIC_CAMERA_FRONT_ID;
//            state = CameraConstant.CAMERA_STATE_DUAL;
//            cameraSize = PreviewUtils.getPreviewSize(cameraFront == LiveConstant.LIVE_CAMERA_FRONT, PreviewConfig.Preview_Quality.DUAL);
//        }
//
//        return new CameraInfo.CameraBuilder()
//                .setCameraFront(cameraFront)
//                .setDefaultBufferSize(cameraSize)
//                .setImageBufferSize(cameraSize)
//                .setLogicCameraId(logicCameraId)
//                .setState(state)
//                .build();
        return null;
    }

    /**
     * 通过直播配置  选择合适的逻辑摄像头  这里还要根据手机进行调试  包括摄像头支持的分辨率 和 逻辑摄像头支持的摄像模式
     * @param context 上下文
     * @param front 摄像头方向
     * @return 逻辑摄像头id
     */
    public static int liveLogicCameraId(Context context , int front){
//        try {
//            List<Integer> frontCamera = new ArrayList<>();  // 照理前置只有一个
//            List<Integer> backCamera = new ArrayList<>();   // 照理后置大于等于一
//            // 列出所有的逻辑id
//            String[] logicCameraIds = CameraUtils.getCameraLogicId(context);
//            for(String logicId : logicCameraIds){
//                int frontid = CameraUtils.getCameraFront(CameraUtils.getLogicCameraCharacteristics(context, logicId));
//                if(frontid == CameraCharacteristics.LENS_FACING_FRONT){
//                    frontCamera.add(Integer.parseInt(logicId));
//                }else if(frontid == CameraCharacteristics.LENS_FACING_BACK){
//                    backCamera.add(Integer.parseInt(logicId));
//                }
//            }
//            //这个理论上是要配置合适自己手机的规则
//            if(front == LiveConstant.LIVE_CAMERA_FRONT && frontCamera.size() > 0){ // 前置
//                return frontCamera.get(0);
//            }else if(front == LiveConstant.LIVE_CAMERA_BACK && backCamera.size() > 0){
//                return backCamera.get(0);
//            }
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
        return 0;
    }
}
