package github.com.desfate.livekit.gl.interfaces;

import android.graphics.SurfaceTexture;
import android.opengl.EGLContext;

public interface IGLSurfaceTextureListener {
        /**
         * SurfaceTexture可用
         *
         * @param surfaceTexture 可用的SurfaceTexture
         */
        void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture);

        int onTextureProcess(int textureId, EGLContext eglContext);

        /**
         * SurfaceTexture销毁
         *
         * @param surfaceTexture 可用的SurfaceTexture
         */
        void onSurfaceTextureDestroy(SurfaceTexture surfaceTexture);
    }