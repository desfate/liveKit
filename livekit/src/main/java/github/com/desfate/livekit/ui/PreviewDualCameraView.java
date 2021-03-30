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
import github.com.desfate.livekit.dual.M3dDrawerControl;
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

    TextureView textureView = null;  //   背屏渲染
    M3dDrawerControl m3dDrawerControl;

    private LiveConfig liveConfig;//      直播配置数据
    private LivePushControl control;//    直播逻辑控制器

    private JobExecutor mJobExecutor;//   线程池

    public PreviewDualCameraView(Context context) {
        super(context);
    }

    public PreviewDualCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    
    // 这个初始化必须在写入必要接口之前
    public void init(final LiveConfig liveConfig, LiveCallBack liveCallBack){
        m3dDrawerControl = new M3dDrawerControl(PreviewDualCameraView.this,
                true);  //       初始化3d控制器
//        m3dDrawerControl.initGLFactory();
        mJobExecutor = new JobExecutor();
        this.liveConfig = liveConfig;
        if(liveConfig.getLivePushType() == LiveConstant.LivePushType.DATA){  // 数据推送
            control = new LivePushControl.LivePushControlBuilder()
                    .setContext(getContext())
                    .setLiveConfig(liveConfig)
                    .setSurfaceTexture(getmSurfaceTexture())
                    .setLiveCallBack(liveCallBack)
                    .setFocusView(null)
                    .setCameraErrorCallBack(new CameraErrorCallBack() {
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
        }else{  // texture 推送
            textureView = new TextureView(getContext());
            control = new LivePushControl.LivePushControlBuilder()
                    .setContext(getContext())
                    .setLiveConfig(liveConfig)
                    .setSurfaceTexture(getmSurfaceTexture())// 这个SurfaceTexture是作为预览的
                    .setTextureView(textureView)            // 这个textureView并不显示   这个是作为离屏渲染模块
                    .setLiveCallBack(liveCallBack)
                    .setCameraErrorCallBack(new CameraErrorCallBack() {
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
    }

    /**
     * 获取直播推流控制器
     * @return 控制器
     */
    public LivePushControl getControl() {
        return control;
    }

    public void startPreview(){
        control.startPreview();
    }

    @Override
    public void surfaceCreated(GL10 gl , EGLConfig config) {
//        dualCameraDrawer = new DualCameraDrawer();
        m3dDrawerControl.setTextureId(getSurfaceId());
        m3dDrawerControl.onCreated(gl, config);
    }

    @Override
    public void onClick(float X, float Y) {

    }

    @Override
    public void onDrawFrame(GL10 gl ,int mSurfaceId) {
        m3dDrawerControl.onDrawFrame(gl);
//        dualCameraDrawer.draw(mSurfaceId, false, getHeight() ,getWidth() );  //  暂时只支持后置
    }

    @Override
    public void onChanged(GL10 gl, int width, int height) {
        m3dDrawerControl.onSurfaceChanged(gl, width, height);
    }

    @Override
    public void onFrame(SurfaceTexture surfaceTexture) {
        m3dDrawerControl.canDrawerFrame(); // 设置可以开始绘制
    }

    @Override
    public void surfaceInit() {
        m3dDrawerControl.initGLFactory();
    }

    public void setM3dDrawer(boolean isDraw){
        if(m3dDrawerControl != null){
            m3dDrawerControl.setDrawM3d(isDraw);
        }
    }

    public void setFront(boolean front){
        if(m3dDrawerControl != null){
            m3dDrawerControl.setFront(front);
        }
    }

}
