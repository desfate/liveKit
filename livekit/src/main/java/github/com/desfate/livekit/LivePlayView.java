package github.com.desfate.livekit;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

import javax.microedition.khronos.egl.EGLConfig;

import github.com.desfate.livekit.camera.CameraControl;
import github.com.desfate.livekit.camera.view.FocusView;
import github.com.desfate.livekit.reders.CameraDrawer;


/**
 * 基于GLSurfaceView的观众观看页面
 */
public class LivePlayView extends BaseLiveView {
    private CameraDrawer mDrawer; //      opengl渲染代码

    public LivePlayView(Context context) {
        super(context);
    }

    public LivePlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void surfaceCreated(EGLConfig config) {
        mDrawer = new CameraDrawer();
    }

    @Override
    public void onClick(float X, float Y) {

    }

    @Override
    public void onDrawFrame(int mSurfaceId) {
        mDrawer.draw(mSurfaceId, true);
    }
}
