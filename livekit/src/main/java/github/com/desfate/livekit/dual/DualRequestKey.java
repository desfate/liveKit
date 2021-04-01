package github.com.desfate.livekit.dual;

import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.List;

import github.com.desfate.livekit.CameraSetting;

public class DualRequestKey {
    public static final String VSDOF_KEY = "com.mediatek.multicamfeature.multiCamFeatureMode";
    public static final String STEREO_WARNING_KEY = "com.mediatek.stereofeature.stereowarning";
    public static final String DOF_LEVEL_KEY = "com.mediatek.stereofeature.doflevel";
    public static final String PREVIEW_SIZE_KEY = "com.mediatek.vsdoffeature.vsdofFeaturePreviewSize";
    public static final String MTK_VSDOF_FEATURE_WARNING = "com.mediatek.vsdoffeature.vsdofFeatureWarning";
    public static final String MTK_VSDOF_FEATURE_CAPTURE_WARNING_MSG = "com.mediatek.vsdoffeature.vsdofFeatureCaptureWarningMsg";
    public static final String XAPI_CAP_FEATURE_KEY = "com.mediatek.control.capture.xapiCapFeature";
    public static final String XAPI_PRV_FEATURE_KEY = "com.mediatek.control.capture.xapiPrvFeature";

    private CaptureRequest.Key<int[]> mVsdofKey = null;
    private CaptureRequest.Key<int[]> mWarningKey = null;
    private CaptureResult.Key<int[]> mStereoWarningKey = null;
    private CaptureResult.Key<int[]> mVsdofWarningKey = null;
    private CaptureRequest.Key<int[]> mDofLevelKey = null;
    private CaptureRequest.Key<int[]> mPreviewSizeKey = null;
    private CaptureRequest.Key<int[]> mXapiPrvFeatureKey = null;
    private CaptureRequest.Key<int[]> mXapiCapFeatureKey = null;

    private static final int[] VSDOF_KEY_VALUE = new int[]{1};
    private static final int[] PREVIEW_SIZE_KEY_VALUE = new int[]{1080,1920};
    private static final int LEVEL_DEFAULT = 4;
    private int mCurrentLevel = LEVEL_DEFAULT;
    private static int[] CURRENT_DOFLEVEL_VALUE = new int[]{LEVEL_DEFAULT};
    private static final int DUAL_CAMERA_TOO_FAR = 1 << 31;
    private static int mVsdofWarningValue = 0;

    private static final int XAPI_PRV_FEATURE_RTB = 1 << 0;
    private static final int XAPI_CAP_FEATURE_BOKEH = 1 << 0;
    private static int[] DUAL_CAMERA_TOO_FAR_VALUE = new int[]{mVsdofWarningValue};
    private static int[] XAPI_CAP_FEATURE_VALUE = new int[]{XAPI_CAP_FEATURE_BOKEH};
    private static int[] XAPI_PRV_FEATURE_VALUE = new int[]{XAPI_PRV_FEATURE_RTB};

    @RequiresApi(api = Build.VERSION_CODES.P)
    public static CaptureRequest.Key<int[]>
    getAvailableSessionKeys(CameraCharacteristics characteristics, String key) {
        if (characteristics == null) {
            return null;
        }
        CaptureRequest.Key<int[]> keyP2NotificationRequest = null;
        List<CaptureRequest.Key<?>> requestKeyList =
                characteristics.getAvailableSessionKeys();
        if (requestKeyList == null) {
            return null;
        }
        for (CaptureRequest.Key<?> requestKey : requestKeyList) {
            if (requestKey.getName().equals(key)) {
                keyP2NotificationRequest =
                        (CaptureRequest.Key<int[]>) requestKey;
            }
        }
        return keyP2NotificationRequest;
    }

    /**
     * Get request key.
     * @param characteristics the camera characteristics.
     * @param key the request key.
     */
    public CaptureRequest.Key<int[]>
    getRequestKey(CameraCharacteristics characteristics, String key) {
        if (characteristics == null) {
            return null;
        }
        CaptureRequest.Key<int[]> keyP2NotificationRequest = null;
        List<CaptureRequest.Key<?>> requestKeyList =
                characteristics.getAvailableCaptureRequestKeys();
        if (requestKeyList == null) {
            return null;
        }
        for (CaptureRequest.Key<?> requestKey : requestKeyList) {
            if (requestKey.getName().equals(key)) {
                keyP2NotificationRequest = (CaptureRequest.Key<int[]>) requestKey;
            }
        }
        return  keyP2NotificationRequest;
    }


    @RequiresApi(api = Build.VERSION_CODES.P)
    public void setAllKeys(CameraCharacteristics mCameraCharacteristics){
        mVsdofKey = getAvailableSessionKeys(
                mCameraCharacteristics, VSDOF_KEY);
        mWarningKey = getRequestKey(
                mCameraCharacteristics, MTK_VSDOF_FEATURE_CAPTURE_WARNING_MSG);
        mDofLevelKey = getRequestKey(
                mCameraCharacteristics, DOF_LEVEL_KEY);
        mPreviewSizeKey = getAvailableSessionKeys(
                mCameraCharacteristics, PREVIEW_SIZE_KEY);
        //xapi added, start
        mXapiPrvFeatureKey = getAvailableSessionKeys(
                mCameraCharacteristics, XAPI_PRV_FEATURE_KEY);
        mXapiCapFeatureKey = getAvailableSessionKeys(
                mCameraCharacteristics, XAPI_CAP_FEATURE_KEY);
    }


    public void setSpecialVendorTag(CaptureRequest.Builder builder) {
        if (mVsdofKey != null) {
            builder.set(mVsdofKey, VSDOF_KEY_VALUE);
        }
        if (mDofLevelKey != null) {
            CURRENT_DOFLEVEL_VALUE[0] = mCurrentLevel;
            builder.set(mDofLevelKey, CURRENT_DOFLEVEL_VALUE);
        }
        if (mPreviewSizeKey != null) {
            PREVIEW_SIZE_KEY_VALUE[0] = M3dConfig.getSessionSize(CameraSetting.getInstance().getPreviewType()).getWidth();
            PREVIEW_SIZE_KEY_VALUE[1] = M3dConfig.getSessionSize(CameraSetting.getInstance().getPreviewType()).getHeight() * 2;
            builder.set(mPreviewSizeKey, PREVIEW_SIZE_KEY_VALUE);
        }
        if (mWarningKey != null) {
            DUAL_CAMERA_TOO_FAR_VALUE = new int[]{mVsdofWarningValue};
            builder.set(mWarningKey, DUAL_CAMERA_TOO_FAR_VALUE);
        } else {

        }

        if (mXapiCapFeatureKey != null) {
            builder.set(mXapiCapFeatureKey, XAPI_CAP_FEATURE_VALUE);
        } else {
        }

        if (mXapiPrvFeatureKey != null) {
            builder.set(mXapiPrvFeatureKey, XAPI_PRV_FEATURE_VALUE);
        } else {
        }
//        configureBGService(builder);
    }


    public void releaseTagKey(){
        mVsdofKey = null;
        mWarningKey = null;
        mDofLevelKey =  null;
        mPreviewSizeKey = null;
        //xapi added, start
        mXapiPrvFeatureKey = null;
        mXapiCapFeatureKey = null;
    }

}
