package github.com.desfate.livekit.ui;

import android.content.Context;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.WindowManager;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import github.com.desfate.livekit.dual.M3dConfig;
import github.com.desfate.livekit.utils.ScreenUtils;
import github.com.desfate.livekit.reders.OpenGLUtils;

/**
 * 直播/预览/播放 GLSurfaceView基类
 */
public abstract class BaseLiveView extends BaseSizeView
        implements GLSurfaceView.Renderer ,SurfaceTexture.OnFrameAvailableListener{

    private final static String TAG = "BaseLiveView";

    private int mSurfaceId;                   //  textureId
    private SurfaceTexture mSurfaceTexture;   //  图像流容器
    private Surface mSurface;                 //  surface

    /************************************ 控制部分 ********************************/
    private final TouchControl touchControl;  //  用于抽离手势

    public BaseLiveView(Context context) {
        this(context, null);
    }

    public BaseLiveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initSurface();
        touchControl = new TouchControl(context, new TouchControl.onClick() {
            @Override
            public void onClicked(float upX, float upY) {
                onClick(upX, upY);
            }
        });
        setEGLContextClientVersion(2);  //现在OpenGL ES版本已经到3.0了，Android平台上目前有1.0和2.0
        setRenderer(this);  //添加渲染器
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY); // 渲染模式 并不会一直渲染
        setPreserveEGLContextOnPause(true);
    }

    public void initSurface(){
        if(mSurfaceId == 0)
            mSurfaceId = OpenGLUtils.getExternalOESTextureID();
        if(mSurfaceTexture == null) {
            mSurfaceTexture = new SurfaceTexture(mSurfaceId);
            mSurfaceTexture.setOnFrameAvailableListener(this);
        }
        if(mSurface == null) {
            mSurface = new Surface(mSurfaceTexture);
        }
    }

    public abstract void surfaceCreated(GL10 gl,EGLConfig config);

    public abstract void onClick(float X, float Y);

    public abstract void onDrawFrame(GL10 gl, int mSurfaceId);

    public abstract void onChanged(GL10 gl, int width, int height);

    public abstract void onFrame(SurfaceTexture surfaceTexture);

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        onFrame(surfaceTexture); // 当数据帧有效的时候 则会从这里进行回调
        requestRender(); //         显示要求进行渲染，即触发Renderer的onDrawFrame()
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        surfaceCreated(gl, config);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        onChanged(gl, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // 绘制预览
        GLES20.glClearColor(0, 0, 0, 0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        mSurfaceTexture.updateTexImage();  //updateTexImage()方法会将ImageStream的图片数据更新到GL_OES_EGL_image_external类型的纹理中。
        onDrawFrame(gl, mSurfaceId);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(touchControl != null) touchControl.onTouchEvent(event); // 绑定一下touch事件
        return super.onTouchEvent(event);
    }

    public SurfaceTexture getSurfaceTexture() {
        return mSurfaceTexture;
    }

    public void setSurfaceTexture(SurfaceTexture surfaceTexture){
        this.mSurfaceTexture = surfaceTexture;
    }

    public Surface getSurface() {
        if(mSurface == null) initSurface();
        return mSurface;
    }

    public int getSurfaceId(){
        return mSurfaceId;
    }

}
