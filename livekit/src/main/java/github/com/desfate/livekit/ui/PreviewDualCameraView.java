package github.com.desfate.livekit.ui;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.Display;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import javax.microedition.khronos.egl.EGLConfig;

import github.com.desfate.livekit.camera.CameraPreviewControl;
import github.com.desfate.livekit.camera.interfaces.CameraErrorCallBack;
import github.com.desfate.livekit.camera.news.CameraInfo;
import github.com.desfate.livekit.dual.PreviewConfig;
import github.com.desfate.livekit.dual.PreviewControl;
import github.com.desfate.livekit.live.LiveCallBack;
import github.com.desfate.livekit.live.LiveConfig;
import github.com.desfate.livekit.live.LivePushControl;
import github.com.desfate.livekit.reders.DualCameraDrawer;
import github.com.desfate.livekit.utils.JobExecutor;

/**
 * 我是基于特定Android设备实现的双摄相机预览
 * 其实还是打开一个逻辑摄像头
 * 但是输出的数据是双摄采集的数据
 */
public class PreviewDualCameraView extends BaseLiveView{

    DualCameraDrawer dualCameraDrawer;


    private LiveConfig liveConfig;//      直播配置数据
    private LivePushControl control;//    直播逻辑控制器

    private JobExecutor mJobExecutor;//   线程池

    public PreviewDualCameraView(Context context) {
        super(context);
    }

    public PreviewDualCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void init(final LiveConfig liveConfig, LiveCallBack liveCallBack){
        mJobExecutor = new JobExecutor();
        this.liveConfig = liveConfig;
        control = new LivePushControl.LivePushControlBuilder()
                .setContext(getContext())
                .setLiveConfig(liveConfig)
                .setSurfaceTexture(getmSurfaceTexture())
                .setLiveCallBack(liveCallBack)
                .setFocusView(null)
                .setCameraErrorCallBack(new CameraErrorCallBack() {
                    @Override
                    public void onCameraOpenSuccess(CameraInfo info) {
                        post(new Runnable() {
                            @Override
                            public void run() {
//                                addFocusView(liveConfig, parent);
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
    public void surfaceCreated(EGLConfig config) {
        dualCameraDrawer = new DualCameraDrawer();
    }

    @Override
    public void onClick(float X, float Y) {

    }

    @Override
    public void onDrawFrame(int mSurfaceId) {
        dualCameraDrawer.draw(mSurfaceId, false, getHeight() ,getWidth() );  //  暂时只支持后置
    }
    double mAspectRatio = 2.666666;
    private static final double ASPECT_TOLERANCE = 0.03;
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int previewWidth = MeasureSpec.getSize(widthMeasureSpec);
        int previewHeight = MeasureSpec.getSize(heightMeasureSpec);
        boolean widthLonger = previewWidth > previewHeight;
        int longSide = (widthLonger ? previewWidth : previewHeight);
        int shortSide = (widthLonger ? previewHeight : previewWidth);

        if (mAspectRatio > 0) {
            double fullScreenRatio = findFullscreenRatio(getContext());
            if (Math.abs((mAspectRatio - fullScreenRatio)) <= ASPECT_TOLERANCE) {
                // full screen preview case
                if (longSide < shortSide * mAspectRatio) {
                    longSide = Math.round((float) (shortSide * mAspectRatio) / 2) * 2;
                } else {
                    shortSide = Math.round((float) (longSide / mAspectRatio) / 2) * 2;
                }
            } else {
                // standard (4:3) preview case  fixme this to 4 : 3 for 3d Test
                if (longSide > shortSide * mAspectRatio) {
                    longSide = Math.round((float) (shortSide * mAspectRatio) / 2) * 2;
                } else {
                    shortSide = Math.round((float) (longSide / mAspectRatio) / 2) * 2;
                }
            }
        }
        if (widthLonger) {
            previewWidth = longSide;
            previewHeight = shortSide;
        } else {
            previewWidth = shortSide;
            previewHeight = longSide;
        }
        setMeasuredDimension(previewWidth, previewHeight);
    }

    public void setAspectRatio(double aspectRatio) {
        if (mAspectRatio != aspectRatio) {
            mAspectRatio = aspectRatio;
            requestLayout();
        }
    }

    private static double findFullscreenRatio(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point point = new Point();
        display.getRealSize(point);

        double fullscreen;
        if (point.x > point.y) {
            fullscreen = (double) point.x / point.y;
        } else {
            fullscreen = (double) point.y / point.x;
        }
        return fullscreen;
    }


}
