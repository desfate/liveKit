package github.com.desfate.livekit.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.util.Size;

public class ScreenUtils {

    /**
     * 获取屏幕状态 横屏 ？ 竖屏
     *
     * @param context
     * @return 1: 竖屏 2：横屏 ORIENTATION_LANDSCAPE ORIENTATION_PORTRAIT
     */
    public static int getScreenOrientation(Context context) {
        Configuration mConfiguration = context.getResources().getConfiguration(); //获取设置的配置信息
        return mConfiguration.orientation; //获取屏幕方向
    }

    /**
     * 获取屏幕Size
     *
     * @param context
     * @return
     */

    public static Size getScreenSize(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return new Size(displayMetrics.widthPixels, displayMetrics.heightPixels);
    }

    /**
     * 根据手机分辨率  从dp转换到px
     *
     * @param context
     * @param dp
     * @return
     */
    public static int dpToPx(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    /**
     * 根据手机分辨率 从px转换到dp
     *
     * @param context
     * @param px
     * @return
     */
    public static int pxToDp(Context context, float px) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }

}
