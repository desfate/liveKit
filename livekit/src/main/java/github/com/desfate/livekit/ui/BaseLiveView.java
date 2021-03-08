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

public abstract class BaseLiveView extends GLSurfaceView implements GLSurfaceView.Renderer , SurfaceTexture.OnFrameAvailableListener{

    private int mSurfaceId;                   //  textureId
    private SurfaceTexture mSurfaceTexture;   //  图像流容器
    private Surface mSurface;                 //  surface

    public BaseLiveView(Context context) {
        this(context, null);
    }


    public BaseLiveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initSurface();
        mClickDistance = ScreenUtils.getScreenSize(context).getWidth() / 20;
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
            // 看过腾讯给的textureDemo 这里每一帧回调的通知 是通过线程去处理
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

    public abstract void surfaceInit();

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        onFrame(surfaceTexture);
        requestRender();
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
        mSurfaceTexture.updateTexImage();  //SurfaceTexture对象所关联的OpenGLES中纹理对象的内容将被更新为Image Stream中最新的图片
        onDrawFrame(gl, mSurfaceId);
    }

    private float mDownX;//                 手指按下的X轴
    private float mDownY;//                 手指按下的Y轴
    private long mTouchTime;//              抬起时的time
    private long mDownTime;//               按下时的time
    private  float mClickDistance;//        用于区分移动距离， 当前动作时点击还是滑动
    private  final long DELAY_TIME = 200;// 长按还是点击

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownTime = System.currentTimeMillis();
                mDownX = event.getX();
                mDownY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                mTouchTime = System.currentTimeMillis() - mDownTime;
                detectGesture(mDownX, event.getX(), mDownY, event.getY());
                break;
        }
        return true;
    }

    private void detectGesture(float downX, float upX, float downY, float upY) {
        float distanceX = upX - downX;
        float distanceY = upY - downY;
        if (Math.abs(distanceX) < mClickDistance
                && Math.abs(distanceY) < mClickDistance
                && mTouchTime < DELAY_TIME) {
            onClick(upX, upY);
        }
    }

    public SurfaceTexture getmSurfaceTexture() {
        return mSurfaceTexture;
    }

    public void setSurfaceTexture(SurfaceTexture surfaceTexture){
        this.mSurfaceTexture = surfaceTexture;
    }

    public Surface getmSurface() {
        if(mSurface == null) initSurface();
        return mSurface;
    }

    public int getSurfaceId(){
        return mSurfaceId;
    }


    double mAspectRatio = M3dConfig.getAspectRatio();
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
