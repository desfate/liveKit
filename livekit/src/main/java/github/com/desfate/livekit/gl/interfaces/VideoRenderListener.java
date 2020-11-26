package github.com.desfate.livekit.gl.interfaces;

import android.opengl.EGLContext;

public interface VideoRenderListener {
        void onRenderVideoFrame(int textureId, EGLContext eglContext);
    }