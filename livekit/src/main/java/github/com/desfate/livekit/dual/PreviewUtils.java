package github.com.desfate.livekit.dual;

import android.content.Context;
import android.util.Log;
import android.util.Size;

import github.com.desfate.livekit.camera.news.CameraInfo;
import github.com.desfate.livekit.live.LiveConfig;
import github.com.desfate.livekit.utils.LiveSupportUtils;

public class PreviewUtils {

    public static String TAG = "PreviewUtils";

    public static int DUAL_LOGIC_CAMERA_ID = 3;

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
                .setCameraFront(cameraFront)
                .setDefaultBufferSize(cameraSize)
                .setImageBufferSize(cameraSize)
                .setLogicCameraId(logicCameraId)
                .setState(2)
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
                previewSize = new Size(2944, 1104);
                break;
        }
        return previewSize;
    }

    public static int getSupportCameraId(PreviewConfig config){
        if(config.getState() == 1){
            return DUAL_LOGIC_CAMERA_ID;
        }else{
            return 0;
        }
    }


}
