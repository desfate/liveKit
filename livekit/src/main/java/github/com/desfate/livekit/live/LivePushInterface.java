package github.com.desfate.livekit.live;

import github.com.desfate.livekit.camera.FocusControl;
import github.com.desfate.livekit.camera.news.CameraInfo;
import github.com.desfate.livekit.ui.FocusView;

/**
 *
 * 控制器需要具备的一些功能
 * 1： 绑定本地预览
 * 2： 设置直播类型
 * 3： 开启直播上传
 * 4： 切换直播摄像头
 * 5： 停止直播，释放资源
 *
 *   PushDataAgent  数据类型
 *   PushTextureIdAgent textureId类型
 */
public interface LivePushInterface {

    void startPreview();//                         开始预览

    void startPush();//                            开始推流

    void switchCamera(CameraInfo info);//          切换相机

    void stopPush();//                             停止推流

    void releaseRes();//                           释放资源

    FocusControl customerFocus(FocusView focusView);  //    自定义对焦逻辑 需要拿到对焦视图 进行绑定

    void focusViewChange(int width, int height); //         UI界面发生了变化 对焦页面要重新设置

    void startMFocus(float x, float y); //                      手动点击对焦
}
