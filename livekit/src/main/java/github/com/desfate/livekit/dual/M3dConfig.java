package github.com.desfate.livekit.dual;

import android.util.Size;

import github.com.desfate.livekit.CameraSetting;

/**
 * 3d 相机的一些配置
 * 现在只支持横屏3d
 *
 * 这些参数都必须配置成固定值
 * 现阶段支持  4:3  16:9
 *
 * 这里还有两个版本的配置
 *
 * Alpha 是相机返回的图像大小为 4:3 或是 16:9 我们只需要交织即可
 * Beta  是相机返回的图像大小为 8:3 或是 32:9 我们需要做一下处理
 *
 *
 */
public class M3dConfig {

    public enum Preview_type{
        PREVIEW_4TO3,
        PREVIEW_16TO9,
        PREVIEW_16TO9_DUAL,
        PREVIEW_9TO16_DUAL
    }

    /**
     * 预览 4:3 模式下的一些配置 （后置 基于Alpha）
     */
    // 开启相机后的session参数 由于原始输出是 8：3 这里宽高比设置为 2.6666
    public static int M3d_REQUEST_4TO3_WIDTH = 2944;
    public static int M3d_REQUEST_4TO3_HEIGHT = 1104;

    //  GLSurfaceView 的宽高  这里由于显示预览的Surface是4:3的 所以选择一个宽高最接近上面配置的配置
    public static int M3d_VIEW_4TO3_WIDTH = 2880;
    public static int M3D_VIEW_4TO3_HEIGHT = 2160;

    /**
     * 预览 16:9 模式下的一些配置 （后置 基于Alpha）
     */
    // 开启相机后的session参数 由于原始输出是 32：9 这里宽高比设置为 3.5555
    public static int M3d_REQUEST_16TO9_WIDTH = 1920;
    public static int M3d_REQUEST_16TO9_HEIGHT = 540;

    //  GLSurfaceView 的宽高  这里由于显示预览的Surface是16:9的 所以选择一个宽高最接近上面配置的配置
    public static int M3d_VIEW_16TO9_WIDTH = 1920;
    public static int M3D_VIEW_16TO9_HEIGHT = 1080;

    /**
     * 预览 16:9   输出数据是32:9的格式 (拼接了两个16:9) （后置 基于Beta）
     */
    public static int M3D_REQUEST_16TO9_WIDTH_DUAL = 2560;
    public static int M3d_REQUEST_16TO9_HEIGHT_DUAL = 720;
    public static int M3d_VIEW_16TO9_WIDTH_DUAL = 1920;  // 我是输出的屏幕宽高
    public static int M3D_VIEW_16TO9_HEIGHT_DUAL = 1080;

    /**
     * 9:16 前置 输出数据是9：32的格式 （前置 基于Beta）
     */
    public static int M3D_REQUEST_9TO16_WIDTH_DUAL = 1080;
    public static int M3D_REQUEST_9TO16_HEIGHT_DUAL = 1920;
    public static int M3D_VIEW_9TO16_WIDTH_DUAL = 1080;
    public static int M3D_VIEW_9TO16_HEIGHT_DUAL = 1920;

    /**
     * 获取Session参数配置 默认4:3
     * @param type 显示比率的枚举
     * @return Size
     */
    public static Size getSessionSize(Preview_type type){
        switch (type){
            case PREVIEW_4TO3:
                return new Size(M3d_REQUEST_4TO3_WIDTH, M3d_REQUEST_4TO3_HEIGHT);
            case PREVIEW_16TO9:
                return new Size(M3d_REQUEST_16TO9_WIDTH, M3d_REQUEST_16TO9_HEIGHT);
            case PREVIEW_16TO9_DUAL:
                return new Size(M3D_REQUEST_16TO9_WIDTH_DUAL, M3d_REQUEST_16TO9_HEIGHT_DUAL);
            case PREVIEW_9TO16_DUAL:
                return new Size(M3D_REQUEST_9TO16_WIDTH_DUAL, M3D_REQUEST_9TO16_HEIGHT_DUAL);
        }
        return new Size(M3d_REQUEST_4TO3_WIDTH, M3d_REQUEST_4TO3_HEIGHT);
    }

    /**
     * 获取GLSurfaceView的大小参数 这是会通过onSurfaceViewChange 回调的数值
     * @param type 显示比率枚举
     * @return Size
     */
    public static Size getSurfaceViewSize(Preview_type type){
        switch (type){
            case PREVIEW_4TO3:
                return new Size(M3d_VIEW_4TO3_WIDTH, M3D_VIEW_4TO3_HEIGHT);
            case PREVIEW_16TO9:
                return new Size(M3d_VIEW_16TO9_WIDTH, M3D_VIEW_16TO9_HEIGHT);
            case PREVIEW_16TO9_DUAL:
                return new Size(M3d_VIEW_16TO9_WIDTH_DUAL, M3D_VIEW_16TO9_HEIGHT_DUAL);
            case PREVIEW_9TO16_DUAL:
                return new Size(M3D_VIEW_9TO16_WIDTH_DUAL, M3D_VIEW_9TO16_HEIGHT_DUAL);
        }
        return new Size(M3d_VIEW_4TO3_WIDTH, M3D_VIEW_4TO3_HEIGHT);
    }

    /**
     * 这几个配置是加入开启相机时的Surface中的
     * @return Size
     */
    public static Size getCameraSettingSize(){
        Size previewSize;
        if(CameraSetting.getInstance().getPreviewType() == M3dConfig.Preview_type.PREVIEW_4TO3) {
            previewSize = new Size(M3dConfig.M3d_REQUEST_4TO3_WIDTH, M3dConfig.M3d_REQUEST_4TO3_HEIGHT);
        }else if (CameraSetting.getInstance().getPreviewType() == M3dConfig.Preview_type.PREVIEW_16TO9){
            previewSize = new Size(M3dConfig.M3d_REQUEST_16TO9_WIDTH, M3dConfig.M3d_REQUEST_16TO9_HEIGHT * 2);
        }else if (CameraSetting.getInstance().getPreviewType() == M3dConfig.Preview_type.PREVIEW_16TO9_DUAL){
            previewSize = new Size(M3dConfig.M3D_REQUEST_16TO9_WIDTH_DUAL, M3dConfig.M3d_REQUEST_16TO9_HEIGHT_DUAL);
        }else if (CameraSetting.getInstance().getPreviewType() == M3dConfig.Preview_type.PREVIEW_9TO16_DUAL){
            previewSize = new Size(M3dConfig.M3D_REQUEST_9TO16_WIDTH_DUAL, M3dConfig.M3D_REQUEST_9TO16_HEIGHT_DUAL);
        }else{
            previewSize = new Size(M3D_REQUEST_9TO16_WIDTH_DUAL, M3D_REQUEST_9TO16_HEIGHT_DUAL);
        }
        return previewSize;
    }


    public static double getAspectRatio(){
        switch (CameraSetting.getInstance().getPreviewType()){
            case PREVIEW_4TO3:
                return 4d / 3;
            case PREVIEW_16TO9:
            case PREVIEW_16TO9_DUAL:
                return 16d / 9;
            case PREVIEW_9TO16_DUAL:
                return 16d / 9;
        }
        return 4d / 3;
    }


}
