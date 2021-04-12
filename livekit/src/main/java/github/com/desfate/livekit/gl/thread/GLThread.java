package github.com.desfate.livekit.gl.thread;

import android.graphics.SurfaceTexture;
import android.opengl.EGLContext;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import github.com.desfate.livekit.gl.draw.GLTextureOESFilter;
import github.com.desfate.livekit.gl.interfaces.IEGLListener;
import github.com.desfate.livekit.gl.interfaces.IGLSurfaceTextureListener;

/**
 * 整个纹理绘制驱动核心
 */
public class GLThread {
    final static private String TAG = "GLThread";
    private volatile HandlerThread mHandlerThread = null;
    private volatile GLThreadHandler mGLHandler = null;    //   这里将EGLSurface一些状态返回

    private GLTextureOESFilter mGLFilter;
    private float[] mSTMatrix;
    private int[] mTextureID = null;
    private SurfaceTexture mSurfaceTexture = null;
    private IGLSurfaceTextureListener mListener;

    public GLThread() {
    }

    public void start() {
        Log.i(TAG, "surface-render: surface render start ");
        initGLThread();
    }

    public void stop() {
        Log.i(TAG, "surface-render: surface render stop ");
        unintGLThread();
    }

    public void setListener(IGLSurfaceTextureListener listener) {
        mListener = listener;
    }

    public Surface getSurface() {
        synchronized (this) {
            return mGLHandler != null ? mGLHandler.getSurface() : null;
        }
    }

    private int mInputWidth;
    private int mInputHeight;

    public void setInputSize(int width, int height) {
        mInputWidth = width;
        mInputHeight = height;
    }

    public void post(Runnable task) {
        synchronized (this) {
            if (mGLHandler != null) mGLHandler.post(task);
        }
    }

    private void initGLThread() {
        unintGLThread();

        synchronized (this) {
            mHandlerThread = new HandlerThread(TAG);
            mHandlerThread.start();
            mGLHandler = new GLThreadHandler(mHandlerThread.getLooper());
            mGLHandler.setListener(new IEGLListener() {
                @Override
                public void onEGLCreate() {
                    initSurfaceTexture();
                    mGLFilter = new GLTextureOESFilter();
                    mGLFilter.setOutputResolution(mInputWidth, mInputHeight);
                }

                @Override
                public void onTextureProcess(EGLContext eglContext) {  //  绘制驱动模块在这
                    // 收到每帧数据
                    if (mSurfaceTexture != null) {
                        mSurfaceTexture.updateTexImage();  // 拿到这一帧数据
                        mSurfaceTexture.getTransformMatrix(mSTMatrix);
                    }

                    if (mGLFilter != null) {
                        mGLFilter.setMatrix(mSTMatrix);
                        int textureId = mGLFilter.drawToTexture(mTextureID[0]);  //  在这绘制的

                        IGLSurfaceTextureListener listener = mListener;
                        if (listener != null) {
                            listener.onTextureProcess(textureId, eglContext);
                        }
                    }
                }

                @Override
                public void onEGLDestroy() {
                    destroySurfaceTexture();
                    if (mGLFilter != null) {
                        mGLFilter.release();
                        mGLFilter = null;
                    }
                }
            });

            Log.w(TAG, "surface-render: create gl thread " + mHandlerThread.getName());
        }

        sendMsg(GLThreadHandler.MSG_INIT);
    }

    private void unintGLThread() {
        synchronized (this) {
            if (mGLHandler != null) {
                GLThreadHandler.quitGLThread(mGLHandler, mHandlerThread);
                Log.w(TAG, "surface-render: destroy gl thread");
            }

            mGLHandler = null;
            mHandlerThread = null;
        }
    }

    /**
     * 每帧绘制时  通过线程通知
     * @param what
     */
    private void sendMsg(int what) {
        synchronized (this) {
            if (mGLHandler != null) {
                mGLHandler.sendEmptyMessage(what);
            }
        }
    }

    private void sendMsg(int what, Runnable completeTask) {
        synchronized (this) {
            if (mGLHandler != null) {
                Message msg = new Message();
                msg.what = what;
                msg.obj = completeTask;
                mGLHandler.sendMessage(msg);
            }
        }
    }

    private void destroySurfaceTexture() {
        Log.w(TAG, "destroy surface texture ");
        IGLSurfaceTextureListener listener = mListener;
        if (listener != null) {
            listener.onSurfaceTextureDestroy(mSurfaceTexture);
        }
        if (mSurfaceTexture != null) {
            mSurfaceTexture.setOnFrameAvailableListener(null);
            mSurfaceTexture.release();
//            mbCaptureAvailable = false;
            mSurfaceTexture = null;
        }

        if (mTextureID != null) {
            GLES20.glDeleteTextures(1, mTextureID, 0);
            mTextureID = null;
        }
    }

    private int createOESTextureID() {
        int[] texture = new int[1];
        GLES20.glGenTextures(1, texture, 0);

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        return texture[0];
    }

    private void initSurfaceTexture() {
        Log.w(TAG, "getInstance surface texture ");
        mSTMatrix = new float[16];
        mTextureID = new int[1];
        mTextureID[0] = createOESTextureID();

        mSurfaceTexture = new SurfaceTexture(mTextureID[0]);
        mSurfaceTexture.setDefaultBufferSize(mInputWidth, mInputHeight);
        mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                sendMsg(GLThreadHandler.MSG_RUN_TASK, new Runnable() {
                    @Override
                    public void run() {
                        sendMsg(GLThreadHandler.MSG_REND);
                    }
                });
            }
        });
        IGLSurfaceTextureListener listener = mListener;
        if (listener != null) {
            listener.onSurfaceTextureAvailable(mSurfaceTexture);
        }
    }
}
