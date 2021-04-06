package github.com.desfate.livekit.dual;

import android.opengl.GLES20;
import android.util.Size;
import android.view.Surface;


import com.future.Holography.Holography;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import github.com.desfate.livekit.CameraSetting;
import github.com.desfate.livekit.ui.BaseLiveView;
import github.com.desfate.livekit.utils.JobExecutor;

public class M3dDrawerControl {

    private int mDulTextureId = 0;
    private DualCameraSurfaceFactory mSurfaceFactory;
    private DualCameraContextFactory mContextFactory;
    public int mEglVersion = 2;

    private FrameBufferOBJ mFBO = null;
    private Render3D mRender3D;
    private JobExecutor mJobExecutor;
    private BaseLiveView surfaceView;
    private int mDeviceRotation;
    private double mAspectRatio = M3dConfig.getAspectRatio();

    // CameraRender info
    private boolean updateSurfaceVid;                              //   是否允许绘制  每次调用requestRender（）后设置为true
    private Matrix3d mSTMatrix = new Matrix3d();
    private RenderVideo mRenderVideo = null;
    private RecordCamera mRcordCamera = null;
    private Matrix3d mResultMatrix = new Matrix3d();
    private Matrix3d mDeviceMatrix = new Matrix3d();
    private int mMidTexture;

    // 获取预览模式参数
    private M3dConfig.Preview_type previewType = CameraSetting.getInstance().getPreviewType();
    private Size sessionSize;  // 输出大小
    private Size viewSize;     // 显示区域大小

    private boolean isRotate = true;  // 播放时传的是false 采集时传的是true
    private boolean isFront = false;  // 现在又新增功能  是否前置绘制  如果是前置绘制  绘制旋转方式和参数都可能需要进行修改
    private boolean isDrawM3d = true;//  是否绘制3d部分
    private boolean isChange = true; //  是否需要重新初始化绘制部分  因为前后摄像头切换会导致之前的参数失效

    public M3dDrawerControl(BaseLiveView surfaceView, boolean type) {
        this.surfaceView = surfaceView;
        mJobExecutor = new JobExecutor();
        sessionSize = M3dConfig.getSessionSize(previewType);
        viewSize = M3dConfig.getSurfaceViewSize(previewType);
        this.isRotate = type;
    }

    public void setTextureId(int textureId) {
        this.mDulTextureId = textureId;
    }



    public void initGL() {
        surfaceView.setEGLContextClientVersion(mEglVersion);
        mSurfaceFactory = new DualCameraSurfaceFactory();
        surfaceView.setEGLWindowSurfaceFactory(mSurfaceFactory);

        mContextFactory = new DualCameraContextFactory(mEglVersion, null, mSurfaceFactory);
        surfaceView.setEGLContextFactory(mContextFactory);
    }

    public void initGLFactory(){
        mSurfaceFactory = new DualCameraSurfaceFactory();
        surfaceView.setEGLWindowSurfaceFactory(mSurfaceFactory);

        mContextFactory = new DualCameraContextFactory(mEglVersion, null, mSurfaceFactory);
        surfaceView.setEGLContextFactory(mContextFactory);
    }

    public void onCreated(GL10 gl, EGLConfig config) {
//        System.out.println("@@@ ---------------------------------------------------------------  Craeated ");
        if (mRender3D == null) {
            mRender3D = new Render3D(surfaceView, 0);
        }
        mRenderVideo = new RenderVideo(surfaceView);
        mRcordCamera = new RecordCamera(surfaceView);
    }

    int bufferWidth = 0;
    int bufferHeight = 0;

    public void onSurfaceChanged(GL10 gl, int width, int height) {
//        System.out.println("@@@ ----------------------------------------------------- onSurfaceChanged" );
        mDeviceRotation = 1;
        if (Surface.ROTATION_90 == mDeviceRotation || Surface.ROTATION_270 == mDeviceRotation) {
            mDeviceMatrix.setIdentity();
//            mDeviceMatrix.translate(1.0f, 0.0f, 0);
            if(isRotate && !isFront) {
                mDeviceMatrix.translate(1.0f, 0.0f, 0);
                mDeviceMatrix.rotate(90, 0, 0, 1);
            }else if(isFront) {
                mDeviceMatrix.translate(0.0f, 0.0f, 0);
                mDeviceMatrix.rotate(0, 0, 0, 1);
            }else{
                mDeviceMatrix.translate(0.0f, 0.0f, 0);
                mDeviceMatrix.rotate(0, 0, 0, 1);
            }
            mDeviceMatrix.logMatrix();
        } else if (Surface.ROTATION_180 == mDeviceRotation) {
            mDeviceMatrix.setIdentity();
            mDeviceMatrix.translate(1.0f, 0.0f, 0);
            mDeviceMatrix.rotate(180, 0, 0, 1);
            mDeviceMatrix.logMatrix();
        }
        bufferWidth = width;
        bufferHeight = height;

        if (mFBO == null && isChange) {
            Holography.deinitHolography(); // 先释放一下
            // 防止framebuffer被重复创建  这是可以显示出3d大小的区域
            if(isRotate) {
                mFBO = new FrameBufferOBJ(sessionSize.getWidth(), sessionSize.getHeight());
            }else{
                mFBO = new FrameBufferOBJ(sessionSize.getWidth(), sessionSize.getHeight() * 2);
            }
            mMidTexture = mFBO.getTexture();
            Holography.HolographyInit(viewSize.getWidth(), viewSize.getHeight());  // 1280 720
            isChange = !isChange;
//            Holography.HolographyInit(bufferWidth, bufferHeight);
//            System.out.println("@@@@ Holography init = " + bufferWidth + "  bufferHeight = " + bufferHeight);
        }
        if(isRotate) {  // 我是推流预览
            surfaceView.getSurfaceTexture().setDefaultBufferSize(sessionSize.getWidth(), sessionSize.getHeight());  // 这里是拿到的数据大小（来自相机采集）
        }else{  // 我是拉流预览
//            surfaceView.getmSurfaceTexture().setDefaultBufferSize(sessionSize.getWidth(), sessionSize.getHeight() * 2);
            // 拉流时surface大小一般为直播需要显示的大小
            surfaceView.getSurfaceTexture().setDefaultBufferSize(viewSize.getWidth(), viewSize.getHeight()); // 这里是拿到的直播数据流 腾讯直播推流数据在1920 * 1080
        }
        refreshView();

    }

    public void onDrawFrame(GL10 gl) {
        if (updateSurfaceVid) {
            surfaceView.getSurfaceTexture().updateTexImage();
            surfaceView.getSurfaceTexture().getTransformMatrix(mSTMatrix.matrix);
            mResultMatrix.multply(mDeviceMatrix, mSTMatrix);   // 矩阵相乘
            updateSurfaceVid = false;
        }

        // 这里为了方便调试  加个控制  是否开启3d渲染
        if(isDrawM3d){
            // 3d绘制部分
            if(mFBO == null) return;
            mFBO.used();
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT
                    | GLES20.GL_COLOR_BUFFER_BIT
                    | GLES20.GL_STENCIL_BUFFER_BIT);
            if(isRotate) {
                drawLeftRight(sessionSize.getWidth(), sessionSize.getHeight());
            }else{
                drawLeftRight(sessionSize.getWidth(), sessionSize.getHeight() * 2);
            }
            if(mFBO == null) return;
            mFBO.unused();
            GLES20.glViewport(0, 0, bufferWidth, bufferHeight);
//        System.out.println("@@@@ bufferWidth = " + bufferWidth + "  bufferHeight = " + bufferHeight);
            mRender3D.drawSelf(mMidTexture);
        }else{
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT
                    | GLES20.GL_COLOR_BUFFER_BIT
                    | GLES20.GL_STENCIL_BUFFER_BIT);
            if(isRotate) {
                drawLeftRight(sessionSize.getWidth(), sessionSize.getHeight());
            }else{
                drawLeftRight(sessionSize.getWidth(), sessionSize.getHeight() * 2);
            }
        }


    }

    /**
     * 绘制side by side 部分
     *
     * @param width  绘制图像宽
     * @param height 绘制图像高
     */
    private void drawLeftRight(int width, int height) {
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT
                | GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_STENCIL_BUFFER_BIT);
        GLES20.glViewport(0, 0, width, height);
        mRenderVideo.setTransformMatrix(mResultMatrix.matrix);
        mRenderVideo.drawSelf(mDulTextureId);
        mResultMatrix.logMatrix();
        GLES20.glFinish();
    }

    boolean controls = false;

    public void refreshView() {
        mJobExecutor.execute(new JobExecutor.Task<Void>() {
            @Override
            public void onMainThread(Void result) {
                super.onMainThread(result);
                controls = true;
//                surfaceView.getHolder().setFixedSize(1472, 1104);  // 修改texture宽高
//                surfaceView.getHolder().setFixedSize(736, 552);  // 修改texture宽高
//                surfaceView.getHolder().setFixedSize(2944, 2208);  // 修改texture宽高
                surfaceView.getHolder().setFixedSize(viewSize.getWidth(), viewSize.getHeight());  // 修改texture宽高
                surfaceView.setAspectRatio(mAspectRatio);
            }
        });
    }

    /**
     * 开始绘制
     */
    public void canDrawerFrame() {
        updateSurfaceVid = true;
    }

    /**
     *  切换了前后置摄像头  获取的值也要相应改变
     * @param front true 前置 false 后置
     */
    public void setFront(boolean front) {
        if(isFront != front){
            isFront = front;
            isChange = true;
            if(mFBO != null) {
                mFBO.release();  // 这里要重新释放一下FBO
                mFBO = null;
            }
        }

        previewType = CameraSetting.getInstance().getPreviewType();
        mAspectRatio = M3dConfig.getAspectRatio();
        sessionSize = M3dConfig.getSessionSize(previewType);
        viewSize = M3dConfig.getSurfaceViewSize(previewType);
        // 这里照理应该重新初始化
        surfaceView.getHolder().setFixedSize(viewSize.getWidth(), viewSize.getHeight());  // 修改texture宽高
        surfaceView.setAspectRatio(mAspectRatio);
    }

    public void setDrawM3d(boolean drawM3d) {
        isDrawM3d = drawM3d;
    }
}

