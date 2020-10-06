package github.com.desfate.livekit.camera;

import android.content.Context;
import android.util.Size;

import github.com.desfate.livekit.utils.ScreenUtils;

/**
 * 用于camera
 */
public class CameraTools {

    /**
     * 找到当前相机最合适的Size
     * @param supportSizes    当前相机支持的所有Size
     * @param bestSize        最需要的Size
     * @return Size or null
     */
    public Size setCameraOutputs(Size[] supportSizes, Size bestSize){
//        if(supportSizes != null && supportSizes.length > 0){
//            Stream.of(supportSizes)
//                    .filter(size -> (bestSize.getHeight() * size.getWidth() == bestSize.getWidth() * size.getHeight()))
//
//
//        }
        return null;
    }

    /**
     * 当前直播支持的是16：9的分辨率， 实际预览大小应该根据手机摆放方式来确定， 竖屏应该是9：16， 横屏则为16：9
     * @param context
     * @return
     */

    public Size getViewSize(Context context){
        int orientation = ScreenUtils.getScreenOrientation(context);
        Size screenSize = ScreenUtils.getScreenSize(context);
        int width;
        int height;
        if(orientation == 1){
            width = screenSize.getWidth();
            height = 9 * width / 16;
        }else{
            height = screenSize.getHeight();
            width = 16 * height / 9;
        }
        return new Size(width, height);
    }


}
