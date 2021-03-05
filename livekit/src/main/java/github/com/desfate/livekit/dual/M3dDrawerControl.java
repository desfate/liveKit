package github.com.desfate.livekit.dual;

import android.opengl.GLES20;
import android.util.Log;
import android.util.Size;
import android.view.Surface;


import com.future.Holography.Holography;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import github.com.desfate.livekit.ui.PreviewDualCameraView;
import github.com.desfate.livekit.utils.JobExecutor;

public class M3dDrawerControl {

    private int mDulTextureId = 0;
    private DualCameraSurfaceFactory mSurfaceFactory;
    private DualCameraContextFactory mContextFactory;
    public int mEglVersion = 2;

    private FrameBufferOBJ mFBO = null;
    private Render3D mRender3D;
    private JobExecutor mJobExecutor;
    private PreviewDualCameraView surfaceView;
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


    public M3dDrawerControl(PreviewDualCameraView surfaceView) {
        this.surfaceView = surfaceView;
        mJobExecutor = new JobExecutor();
        sessionSize = M3dConfig.getSessionSize(previewType);
        viewSize = M3dConfig.getSurfaceViewSize(previewType);
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
        System.out.println("@@@ ---------------------------------------------------------------  Craeated ");
        if (mRender3D == null) {
            mRender3D = new Render3D(surfaceView, 0);
        }
        mRenderVideo = new RenderVideo(surfaceView);
        mRcordCamera = new RecordCamera(surfaceView);
    }

    int bufferWidth = 0;
    int bufferHeight = 0;

    boolean isCreate = false;

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        System.out.println("@@@ ----------------------------------------------------- onSurfaceChanged" );
        mDeviceRotation = 1;
        if (Surface.ROTATION_90 == mDeviceRotation || Surface.ROTATION_270 == mDeviceRotation) {
            mDeviceMatrix.setIdentity();
//            mDeviceMatrix.translate(1.0f, 0.0f, 0);
            mDeviceMatrix.translate(1.0f, 0.0f, 0);
            mDeviceMatrix.rotate(90, 0, 0, 1);
            mDeviceMatrix.logMatrix();
        } else if (Surface.ROTATION_180 == mDeviceRotation) {
            mDeviceMatrix.setIdentity();
            mDeviceMatrix.translate(1.0f, 0.0f, 0);
            mDeviceMatrix.rotate(180, 0, 0, 1);
            mDeviceMatrix.logMatrix();
        }
        bufferWidth = width;
        bufferHeight = height;

        if (mFBO == null) {
            // 防止framebuffer被重复创建  这是可以显示出3d大小的区域
            mFBO = new FrameBufferOBJ(sessionSize.getWidth(), sessionSize.getHeight());
            mMidTexture = mFBO.getTexture();
            Holography.HolographyInit(bufferWidth, bufferHeight);

//            Holography.HolographyInit(bufferWidth, bufferHeight);
//            System.out.println("@@@@ Holography init = " + bufferWidth + "  bufferHeight = " + bufferHeight);
        }
        surfaceView.getmSurfaceTexture().setDefaultBufferSize(sessionSize.getWidth(), sessionSize.getHeight());
        refreshView();

    }

    public void onDrawFrame(GL10 gl) {
        if (updateSurfaceVid) {
            surfaceView.getmSurfaceTexture().updateTexImage();
            surfaceView.getmSurfaceTexture().getTransformMatrix(mSTMatrix.matrix);
            mResultMatrix.multply(mDeviceMatrix, mSTMatrix);   // 矩阵相乘
            updateSurfaceVid = false;
        }
        if(mFBO == null) return;
        // 3d绘制部分
        mFBO.used();
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT
                | GLES20.GL_COLOR_BUFFER_BIT
                | GLES20.GL_STENCIL_BUFFER_BIT);
        drawLeftRight(sessionSize.getWidth(), sessionSize.getHeight());
        mFBO.unused();
        GLES20.glViewport(0, 0, bufferWidth, bufferHeight);
        System.out.println("@@@@ bufferWidth = " + bufferWidth + "  bufferHeight = " + bufferHeight);
        mRender3D.drawSelf(mMidTexture);
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
}

