package github.com.desfate.livekit.utils;

import android.util.Size;

import github.com.desfate.livekit.LiveConstant;

public class LiveSupportUtils {

    private final static int DEFAULT_WIDTH = 1920;
    private final static int DEFAULT_HEIGHT = 1080;

    public final static int LIVE_SIZE_2560 = 2560;
    public final static int LIVE_SIZE_1080 = 1080;
    public final static int LIVE_SIZE_720 = 720;

    /**
     * 获得当前直播需要的分辨率
     *
     * @param isFront  是否是前置 true:前置 false:后置  前置则 宽 > 高   后置
     * @param liveType 直播类型
     * @return
     */

    public static Size getCameraBestSize(boolean isFront, LiveConstant.LiveQuality liveType) {
        int width = DEFAULT_WIDTH;
        int height = DEFAULT_HEIGHT;
        switch (liveType) {
            case LIVE_720P:
                width = 1280;
                height = 720;
                break;
            case LIVE_1080P:
                width = 1920;
                height = 1080;
                break;
        }
        if(isFront){ // 前置宽小于高
            return new Size(height, width);
        }else{
            return new Size(width, height);
        }
    }

    public static Size getCameraTextureSize(boolean isFront, int liveType) {
        int width = DEFAULT_WIDTH;
        int height = DEFAULT_HEIGHT;
        switch (liveType) {
            case LIVE_SIZE_720:
                width = 1280;
                height = 720;
                break;
            case LIVE_SIZE_1080:
                width = 1920;
                height = 1080;
                break;
            case LIVE_SIZE_2560:
                width = 2560;
                height = 1440;
                break;
        }
        if(isFront) {
            return new Size(height, width);
        }else{
            return new Size(width, height);
        }
    }


}
