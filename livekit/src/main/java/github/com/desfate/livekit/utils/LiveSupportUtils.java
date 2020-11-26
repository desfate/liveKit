package github.com.desfate.livekit.utils;

import android.util.Size;

public class LiveSupportUtils {

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

    public static Size getCameraBestSize(boolean isFront, int liveType) {
        switch (liveType) {
            case LIVE_SIZE_720:
                return new Size(1280, 720);
            case LIVE_SIZE_1080:
                return new Size(1920,1080);
            case LIVE_SIZE_2560:
                return new Size(2560,1440);
        }
        return new Size(1920, 1080);
    }

    public static Size getCameraTextureSize(boolean isFront, int liveType) {
        switch (liveType) {
            case LIVE_SIZE_720:
                if(isFront){
                    return new Size(720, 1280);
                }else {
                    return new Size(1280, 720);
                }
            case LIVE_SIZE_1080:
                if(isFront) {
                    return new Size(1080, 1920);
                }else {
                    return new Size(1920, 1080);
                }
            case LIVE_SIZE_2560:
                if(isFront) {
                    return new Size(1440, 2560);
                }else {
                    return new Size(2560, 1440);
                }
        }
        if(isFront) {
            return new Size(1080, 1920);
        }else{
            return new Size(1920, 1080);
        }
    }


}
