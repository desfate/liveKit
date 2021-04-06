package github.com.desfate.livekit.ui;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.TextureView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import github.com.desfate.livekit.LiveConstant;
import github.com.desfate.livekit.camera.interfaces.CameraErrorCallBack;
import github.com.desfate.livekit.camera.news.CameraInfo;
import github.com.desfate.livekit.controls.MCameraControl;
import github.com.desfate.livekit.controls.MControl;
import github.com.desfate.livekit.dual.M3dPreviewControl;
import github.com.desfate.livekit.dual.MPreviewControl;
import github.com.desfate.livekit.live.LiveCallBack;
import github.com.desfate.livekit.LiveConfig;
import github.com.desfate.livekit.live.LivePushControl;
import github.com.desfate.livekit.utils.JobExecutor;

/**
 * 我是基于特定Android设备实现的双摄相机预览
 * 其实还是打开一个逻辑摄像头
 * 但是输出的数据是双摄采集的数据
 */
public class PreviewDualCameraView extends BaseLiveView{

    private MCameraControl control;  //   控制器

    private JobExecutor mJobExecutor;//   线程池

    public PreviewDualCameraView(Context context) {
        super(context);
    }

    public PreviewDualCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // 这个初始化必须在写入必要接口之前
    public void init(final LiveConfig liveConfig, LiveCallBack liveCallBack){
        mJobExecutor = new JobExecutor();
        control = new MCameraControl.MCameraControlBuilder()
                .setContext(getContext())
                .setBaseLiveView(PreviewDualCameraView.this)
                .setLiveConfig(liveConfig)
                .setLiveCallBack(liveCallBack)
                .setCallBack(new CameraErrorCallBack() {
                    @Override
                    public void onCameraOpenSuccess(CameraInfo info) {
                        mJobExecutor.execute(new JobExecutor.Task<Void>() {
                            @Override
                            public void onMainThread(Void result) {
                                super.onMainThread(result);
                                setAspectRatio(mAspectRatio);
                            }
                        });
                    }

                    @Override
                    public void onCameraOpenError(CameraInfo info, int error) {

                    }
                })
                .build();
    }

    /**
     * 获得控制器
     * @return 返回控制接口
     */
    public MControl getControl(){
        return control;
    }

    @Override
    public void surfaceCreated(GL10 gl , EGLConfig config) {
        control.surfaceCreated(gl, config);
    }

    @Override
    public void onClick(float X, float Y) {

    }

    @Override
    public void onDrawFrame(GL10 gl ,int mSurfaceId) {
        control.onDrawFrame(gl, mSurfaceId);
    }

    @Override
    public void onChanged(GL10 gl, int width, int height) {
        control.onChanged(gl, width, height);
    }

    @Override
    public void onFrame(SurfaceTexture surfaceTexture) {
        control.onFrame(surfaceTexture);
    }

}
