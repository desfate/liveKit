package github.com.desfate.livekit.camera.interfaces;

import android.opengl.EGLContext;

public interface OnFrameAvailable {
    void onFrame(int textureId, EGLContext eglContext);
}
