package github.com.desfate.livekit.dual;

import android.opengl.EGL14;
import android.opengl.GLSurfaceView;
import android.view.Surface;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;


// 重写 SurfaceFactory
public class DualCameraSurfaceFactory implements GLSurfaceView.EGLWindowSurfaceFactory {

    private EGL10 mEGL = null;
    private EGLDisplay mDisplay = null;
    private EGLConfig mConfig = null;

    private EGLSurface mCameraSurface = null;

    private EGLSurface mWindowSurface = null;

    private EGLContext mSurfaceHolderEGLContext = null;

    public EGLSurface createWindowSurface(EGL10 egl, EGLDisplay display,
                                          EGLConfig config, Object nativeWindow) {
        // 保存变量
        mEGL = egl;
        mDisplay = display;
        mConfig = config;

        EGLSurface result = null;
        try {
            result = egl.eglCreateWindowSurface(display, config, nativeWindow, null);
            mWindowSurface = result;
        } catch (IllegalArgumentException e) {
            // This exception indicates that the surface flinger surface
            // is not valid. This can happen if the surface flinger surface has
            // been torn down, but the application has not yet been
            // notified via SurfaceHolder.Callback.surfaceDestroyed.
            // In theory the application should be notified first,
            // but in practice sometimes it is not. See b/4588890
        }
        return result;
    }

    public void destroySurface(EGL10 egl, EGLDisplay display,
                               EGLSurface surface) {
        egl.eglDestroySurface(display, surface);
        destroyCameraSurface();
    }

    // 自定义
    public void setEglContext(EGLContext eglContext) {
        mSurfaceHolderEGLContext = eglContext;
    }

    public EGLSurface createCameraSurface(Surface s) {
        if(mCameraSurface == null || mCameraSurface == EGL10.EGL_NO_SURFACE) {
            try {
                int[] surfaceAttribs = {
                        EGL14.EGL_NONE
                };
                mCameraSurface = mEGL.eglCreateWindowSurface(mDisplay, mConfig, s, surfaceAttribs);
            } catch (IllegalArgumentException e) {

            }
        }

        if(mCameraSurface == EGL10.EGL_NO_SURFACE) {

        }
        return mCameraSurface;
    }

    public EGLSurface getCameraSurface() {
        return mCameraSurface;
    }
    public void destroyCameraSurface() {
        if(mCameraSurface != null) {
            mEGL.eglDestroySurface(mDisplay, mCameraSurface);
            mCameraSurface = null;
        }
    }

    public boolean isCameraEGLSurfaceAvailable() {
        if(mCameraSurface == null) {

            return false;
        }
        if(mCameraSurface == EGL10.EGL_NO_SURFACE) {

            return false;
        }
        return true;
    }

    public int useCameraEGLSurface() {
//            Log.w(TAG, "mSurfaceHolderEGLContext  = " + mSurfaceHolderEGLContext);

        if(isCameraEGLSurfaceAvailable()) {
            mEGL.eglMakeCurrent(mDisplay, mCameraSurface, mCameraSurface, mSurfaceHolderEGLContext);
            return EGL10.EGL_SUCCESS;
        }

        return 0;
    }

    public int swapCameraEGLSurface() {
        if (! mEGL.eglSwapBuffers(mDisplay, mCameraSurface)) {
            return mEGL.eglGetError();
        }
        return EGL10.EGL_SUCCESS;
    }

    public void restoreEGLSurface() {
        mEGL.eglMakeCurrent(mDisplay, mWindowSurface, mWindowSurface, mSurfaceHolderEGLContext);
    }

}
