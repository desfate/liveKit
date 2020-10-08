package github.com.desfate.livekit;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

import javax.microedition.khronos.egl.EGLConfig;

import github.com.desfate.livekit.camera.CameraControl;
import github.com.desfate.livekit.camera.view.FocusView;


/**
 * 基于GLSurfaceView的观众观看页面
 */
public class LivePlayView extends BaseLiveView {

    CameraControl cameraControl; //       相机管理
    FocusView mFocusView; //              对焦View  这个view是要加入LivePlayView的父布局

    public LivePlayView(Context context) {
        super(context);
    }

    public LivePlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mFocusView = new FocusView(context);
        cameraControl = new CameraControl(context, this, mFocusView);
    }

    @Override
    public void surfaceCreated(EGLConfig config) {

    }

    @Override
    public void onClick(float X, float Y) {

    }

    @Override
    public void onDrawFrame(int mSurfaceId) {

    }

    /**
     * 把对焦动画的view 加入父布局
     * @param viewGroup
     */
    public void setParentLayout(ViewGroup viewGroup){
        viewGroup.addView(mFocusView);
    }

    public CameraControl getControl(){
        return cameraControl;
    }


}
