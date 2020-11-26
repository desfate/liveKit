package github.com.desfate.livekit.gl.interfaces;

import android.opengl.EGLContext;

public interface IEGLListener {
        void onEGLCreate();

        void onTextureProcess(EGLContext eglContext);

        void onEGLDestroy();
    }