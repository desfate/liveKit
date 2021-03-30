package github.com.desfate.livekit.dual;

import android.content.Context;
import android.util.Log;
import android.util.Size;

import github.com.desfate.livekit.CameraConstant;
import github.com.desfate.livekit.CameraSetting;
import github.com.desfate.livekit.camera.news.CameraInfo;

public class PreviewUtils {

    public static String TAG = "PreviewUtils";

    /**
     * 双摄预览
     * @param context
     * @param config
     * @return
     */
    public static CameraInfo dualToCameraInfo(Context context, PreviewConfig config){
        if(config == null) {
            Log.e(TAG, "liveConfig == null please set a value");
            return null;
        }
        //  摄像头方向
        int cameraFront = config.getIsCameraFront();
        //  预览需要的摄像头size
        Size cameraSize =  getPreviewSize(cameraFront == 1, config.getQuality_type());
        int logicCameraId = getSupportCameraId(config);

        return new CameraInfo.CameraBuilder()
                .setDefaultBufferSize(cameraSize)
                .setImageBufferSize(cameraSize)
                .setLogicCameraId(logicCameraId)
                .setState(CameraConstant.CameraState.CAMERA_DUAL_BACK)  // dual
                .build();
    }

    public static Size getPreviewSize(boolean isFront, PreviewConfig.Preview_Quality quality){
        Size previewSize = null;
        switch (quality){
            case LOW:
                break;
            case MID:
                break;
            case HEIGHT:
                break;
            case DUAL:
                if(CameraSetting.getInstance().getPreviewType() == M3dConfig.Preview_type.PREVIEW_4TO3) {
                    previewSize = new Size(M3dConfig.M3d_REQUEST_4TO3_WIDTH, M3dConfig.M3d_REQUEST_4TO3_HEIGHT);
                }else if (CameraSetting.getInstance().getPreviewType() == M3dConfig.Preview_type.PREVIEW_16TO9){
                    previewSize = new Size(M3dConfig.M3d_REQUEST_16TO9_WIDTH, M3dConfig.M3d_REQUEST_16TO9_HEIGHT * 2);
                }else if (CameraSetting.getInstance().getPreviewType() == M3dConfig.Preview_type.PREVIEW_16TO9_DUAL){
                    previewSize = new Size(M3dConfig.M3D_REQUEST_16TO9_WIDTH_DUAL, M3dConfig.M3d_REQUEST_16TO9_HEIGHT_DUAL);
                }
//                else if (CameraSetting.getInstance().getPreviewType() == M3dConfig.Preview_type.PREVIEW_9TO16){
//                    previewSize = new Size(M3dConfig.M3D_REQUEST_9TO16_WIDTH_DUAL, M3dConfig.M3D_REQUEST_9TO16_HEIGHT_DUAL);
//                }
                break;
        }
//        if(!isFront) previewSize = new Size(previewSize.getHeight(),  previewSize.getWidth());
        return previewSize;
    }

    public static int getSupportCameraId(PreviewConfig config){
//        if(config.getState() == CameraConstant.CAMERA_STATE_DUAL){
//            return CameraConstant.DUAL_LOGIC_CAMERA_ID;
//        }else if(config.getState() == CameraConstant.CAMERA_STATE_DUAL
//                && config.getIsCameraFront() == CameraConstant.CAMERA_FRONT){ // 前置 双摄
//            return CameraConstant.DUAL_LOGIC_CAMERA_FRONT_ID;
//        }else{
            return CameraConstant.DEFAULT_LOGIC_CAMERA_ID;
//        }
    }


}
