package github.com.desfate.livekit.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.util.Size;

public class ScreenUtils {

    /**
     * 获取屏幕状态 横屏 ？ 竖屏
     *
     * @param context 上下文
     * @return 1: 竖屏 2：横屏 ORIENTATION_LANDSCAPE ORIENTATION_PORTRAIT
     */
    public static int getScreenOrientation(Context context) {
        Configuration mConfiguration = context.getResources().getConfiguration(); //获取设置的配置信息
        return mConfiguration.orientation; //获取屏幕方向
    }

    /**
     * 获取屏幕Size
     * @param context 上下文
     * @return Size
     */

    public static Size getScreenSize(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return new Size(displayMetrics.widthPixels, displayMetrics.heightPixels);
    }

    /**
     * 根据手机分辨率  从dp转换到px
     *  px： 像素数量： 平常所说的1920×1080只是像素数量，也就是1920px×1080px，代表手机高度上有1920个像素点，宽度上有1080个像素点
     *  dp： 像素密度： 也就是dp会随着不同屏幕而改变控件长度的像素数量。
     *  dip：同dp
     *  sp: 与缩放无关的抽象像素（Scale-independent Pixel） android可以系统修改字体大小 所以要用sp
     *
     * @param context 上下文
     * @param dp dp数值
     * @return px
     */
    public static int dpToPx(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    /**
     * 根据手机分辨率 从px转换到dp
     *
     * @param context 上下文
     * @param px px数值
     * @return dp
     */
    public static int pxToDp(Context context, float px) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }

}
