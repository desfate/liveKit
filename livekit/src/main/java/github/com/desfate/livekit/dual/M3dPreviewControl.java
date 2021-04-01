package github.com.desfate.livekit.dual;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.Image;
import android.media.ImageReader;

import github.com.desfate.livekit.CameraAdapter;
import github.com.desfate.livekit.LiveConfig;
import github.com.desfate.livekit.camera.interfaces.CameraErrorCallBack;
import github.com.desfate.livekit.camera.news.CameraClient;
import github.com.desfate.livekit.camera.news.CameraInfo;
import github.com.desfate.livekit.ui.BaseLiveView;
import github.com.desfate.livekit.ui.PreviewDualCameraView;

/**
 * 用于显示预览的控制器 给上层用户操作
 *
 * 我只关系3d的预览绘制
 *
 */
public class M3dPreviewControl implements MPreviewControl {

    M3dDrawerControl m3dDrawerControl;
    LiveConfig liveConfig;

    public M3dPreviewControl(BaseLiveView view, LiveConfig liveConfig){
        m3dDrawerControl = new M3dDrawerControl(view,
                true);
        this.liveConfig = liveConfig;
    }

    @Override
    public void isDraw3D(boolean isDraw) {
        if(m3dDrawerControl == null) return;
        m3dDrawerControl.setDrawM3d(isDraw);
    }

    @Override
    public void switchCamera() {
        if(m3dDrawerControl == null) return;
        m3dDrawerControl.setFront(CameraAdapter.liveConfigToCameraInfo(liveConfig).isFront());
    }


    public M3dDrawerControl getDrawControl(){
        return m3dDrawerControl;
    }
}
