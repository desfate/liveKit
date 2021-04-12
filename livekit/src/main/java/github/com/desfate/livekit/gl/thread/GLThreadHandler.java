package github.com.desfate.livekit.gl.thread;

import android.annotation.TargetApi;
import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import github.com.desfate.livekit.gl.egl.EglCore;
import github.com.desfate.livekit.gl.egl.EglSurfaceBase;
import github.com.desfate.livekit.gl.interfaces.IEGLListener;

/**
 * GL线程
 */
public class GLThreadHandler extends Handler {
    final static private String TAG = "TXGLThreadHandler";

    public static final int MSG_INIT = 100;
    public static final int MSG_END = MSG_INIT + 1;
    public static final int MSG_REND = MSG_END + 1;
    public static final int MSG_RUN_TASK = MSG_REND + 1;

    public int mCaptureWidth = 1920;
    public int mCaptureHeight = 1080;
    public Surface mSurface = null;          //           Surface 视频数据容器  默认是空的 所以必然生成一个离屏渲染容器
    public EGLContext mEgl14Context = null;
    private IEGLListener mListener = null;

    private EglSurfaceBase mEglBase;

    public static void quitGLThread(Handler handler, HandlerThread thread) {
        final HandlerThread glThread = thread;
        final Handler glHandler = handler;
        if (glHandler == null || glThread == null) return;

        Message msg = new Message();
        msg.what = MSG_END;
        msg.obj = new Runnable() {
            @Override
            public void run() {
                Handler mainHandler = new Handler(Looper.getMainLooper());
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (glHandler != null) {
                            glHandler.removeCallbacksAndMessages(null);
                        }

                        if (glThread != null) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                                glThread.quitSafely();
                            } else {
                                glThread.quit();
                            }
                        }
                    }
                });
            }
        };
        glHandler.sendMessage(msg);
    }

    public GLThreadHandler(Looper looper) {
        super(looper);
    }

    public void setListener(IEGLListener listener) {
        mListener = listener;
    }

    public Surface getSurface() {
        return mSurface;
    }

    public void swap() {
        if (mEglBase != null) {
            mEglBase.swapBuffers();
        }
    }

    public void handleMessage(Message msg) {
        if (msg == null) return;

        switch (msg.what) {
            case MSG_INIT://  初始化GL
                onMsgInit(msg);
                break;
            case MSG_REND:
                try {
                    onMsgRend(msg);
                } catch (Exception e) {

                }
                break;
            case MSG_END:
                onMsgEnd(msg);
                break;
            default:
                break;
        }

        if (msg != null && msg.obj != null) {
            Runnable runTask = (Runnable) msg.obj;
            runTask.run();
        }
    }

    private void onMsgInit(Message msg) {
        try {
            initGL();
        } catch (Exception e) {
            Log.e(TAG, "surface-render: getInstance egl context exception " + mSurface);
            mSurface = null;
        }
    }

    private void onMsgEnd(Message msg) {
        destroyGL();
    }

    /**
     * 这里将当前EGL上下文返回
     * @param msg
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void onMsgRend(Message msg) {
        try {
            if (mListener != null) {
                mListener.onTextureProcess(EGL14.eglGetCurrentContext());
            }
        } catch (Exception e) {
            Log.e(TAG, "onMsgRend Exception " + e.getMessage());
            e.printStackTrace();
        }

    }

    /**
     * 初始化EGLSurface
     * @return true
     */
    private boolean initGL() {
        Log.d(TAG, String.format("getInstance egl size[%d/%d]", mCaptureWidth, mCaptureHeight));
        // EGL上下文
        EglCore eglCore = new EglCore(mEgl14Context, 0);
        // 生成EGLSurface （EglSurfaceBase里有EGLSurface）
        mEglBase = new EglSurfaceBase(eglCore);
        if (mSurface == null) {
            // 创建离屏渲染EGLSurface
            mEglBase.createOffscreenSurface(mCaptureWidth, mCaptureHeight);
        } else {
            mEglBase.createWindowSurface(mSurface);
        }
        mEglBase.makeCurrent(); // 切换当前上下文
        Log.w(TAG, "surface-render: create egl context " + mSurface);
        if (mListener != null) {
            mListener.onEGLCreate();   // 通知EGLSurface创建成功
        }
        return true;
    }


    private void destroyGL() {
        Log.w(TAG, "surface-render: destroy egl context " + mSurface);

        if (mListener != null) {
            mListener.onEGLDestroy(); // 通知EGLSurface销毁成功
        }

        if (mEglBase != null) {
            mEglBase.releaseEglSurface();
            mEglBase = null;
        }
        mSurface = null;
    }
}