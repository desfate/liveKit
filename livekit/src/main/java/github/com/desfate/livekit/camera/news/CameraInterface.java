package github.com.desfate.livekit.camera.news;

import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.params.MeteringRectangle;

/**
 * 相机应该具备的功能
 */
public interface CameraInterface {

    /**
     * 开启相机
     * @param info 需要开启的摄像头的信息
     */
    void openCamera(CameraInfo info);

    /**
     * 离屏渲染纹理
     * @param surfaceTexture 这个纹理用于 录像/直播推流
     */
    void addSurfaceTexture(SurfaceTexture surfaceTexture);

    /**
     * 切换相机
     * @param info 需要切换的摄像头信息
     */
    void switchCamera(CameraInfo info);

    /**
     * 关闭当前摄像头
     */
    void stopCamera();

    /**
     * 销毁摄像头流程
     */
    void releaseCamera();//     释放相机资源

    /****************************************************************  对焦相关功能 ***************************************************************************/

    /**
     * 返回对焦状态
     * @return int
     */
    int focusState(); //     获取对焦状态

    /**
     * 设置自动对焦
     */
    void autoFocus(); //    自动对焦

    /**
     * 调用手动对焦
     * @param focusRect 点击自动对焦矩阵区域
     * @param meteringRect 点击自动曝光区域
     */
    void manualFocus(MeteringRectangle focusRect, MeteringRectangle meteringRect); //   手动对焦

    /****************************************************************  相机自带的属性 ***************************************************************************/

    /**
     * 获取当前相机信息
     * @return
     */
    CameraInfo getCameraInfo();  // 获取相机配置

    /**
     * 获取当前设备信息
     * @return
     */
    CameraCharacteristics getCameraCharacteristics(); // 获取当前设备信息

}
