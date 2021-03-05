package github.com.desfate.livekit.dual;

import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGL11;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

public class DualCameraContextFactory implements GLSurfaceView.EGLContextFactory {
    private int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
    private DualCameraSurfaceFactory mSurfaceFactory;
    private int mEglVersion = 2;
    private EGLContext mEglContextSelfPointer = null;     // 这个仅仅保留的是指针，不需要释放
    public DualCameraContextFactory(int mEglVersion, EGLContext mEglContextSelfPointer, DualCameraSurfaceFactory mSurfaceFactory){
        this.mEglVersion = mEglVersion;
        this.mEglContextSelfPointer = mEglContextSelfPointer;
        this.mSurfaceFactory = mSurfaceFactory;
    }

    public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig config) {
        int[] attrib_list = {EGL_CONTEXT_CLIENT_VERSION, mEglVersion,
                EGL10.EGL_NONE };

        mEglContextSelfPointer = egl.eglCreateContext(display, config, EGL10.EGL_NO_CONTEXT,
                mEglVersion != 0 ? attrib_list : null);

        mSurfaceFactory.setEglContext(mEglContextSelfPointer);

        return mEglContextSelfPointer;
    }

    public void destroyContext(EGL10 egl, EGLDisplay display,
                               EGLContext context) {
        if (!egl.eglDestroyContext(display, context)) {

            if (EglSingleHelper.LOG_THREADS) {
//                Log.i("DualCameraContextFactory", "tid=" + Thread.currentThread().getId());
            }
            EglSingleHelper.throwEglException("eglDestroyContex", egl.eglGetError());
        }
        else {
            mEglContextSelfPointer = null;
            mSurfaceFactory.setEglContext(mEglContextSelfPointer);
        }
    }
    private static class EglSingleHelper {

        private final static boolean LOG_THREADS = false;

        public static void throwEglException(String function, int error) {
            String message = formatEglError(function, error);
            if (LOG_THREADS) {
//                Log.e("EglHelper", "throwEglException tid=" + Thread.currentThread().getId() + " "+ message);
            }
            throw new RuntimeException(message);
        }

        public static String formatEglError(String function, int error) {
            return function + " failed: " + getErrorString(error);
        }


        public static String getErrorString(int error) {
            switch (error) {
                case EGL10.EGL_SUCCESS:
                    return "EGL_SUCCESS";
                case EGL10.EGL_NOT_INITIALIZED:
                    return "EGL_NOT_INITIALIZED";
                case EGL10.EGL_BAD_ACCESS:
                    return "EGL_BAD_ACCESS";
                case EGL10.EGL_BAD_ALLOC:
                    return "EGL_BAD_ALLOC";
                case EGL10.EGL_BAD_ATTRIBUTE:
                    return "EGL_BAD_ATTRIBUTE";
                case EGL10.EGL_BAD_CONFIG:
                    return "EGL_BAD_CONFIG";
                case EGL10.EGL_BAD_CONTEXT:
                    return "EGL_BAD_CONTEXT";
                case EGL10.EGL_BAD_CURRENT_SURFACE:
                    return "EGL_BAD_CURRENT_SURFACE";
                case EGL10.EGL_BAD_DISPLAY:
                    return "EGL_BAD_DISPLAY";
                case EGL10.EGL_BAD_MATCH:
                    return "EGL_BAD_MATCH";
                case EGL10.EGL_BAD_NATIVE_PIXMAP:
                    return "EGL_BAD_NATIVE_PIXMAP";
                case EGL10.EGL_BAD_NATIVE_WINDOW:
                    return "EGL_BAD_NATIVE_WINDOW";
                case EGL10.EGL_BAD_PARAMETER:
                    return "EGL_BAD_PARAMETER";
                case EGL10.EGL_BAD_SURFACE:
                    return "EGL_BAD_SURFACE";
                case EGL11.EGL_CONTEXT_LOST:
                    return "EGL_CONTEXT_LOST";
                default:
                    return getHex(error);
            }
        }

        private static String getHex(int value) {
            return "0x" + Integer.toHexString(value);
        }
    }
}