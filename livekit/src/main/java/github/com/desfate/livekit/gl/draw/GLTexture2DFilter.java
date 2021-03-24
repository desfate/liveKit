package github.com.desfate.livekit.gl.draw;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.FloatBuffer;

import github.com.desfate.livekit.gl.egl.EglCore;

/**
 * 渲染GL_TEXTURE_2D到EGLSurface上，
 * 如果EGLSurface绑定TextureView的SurfaceTexture，就可以在TextureView上显示出来
 */
public class GLTexture2DFilter {

    private static final String VERTEX_SHADER =
            "uniform mat4 uMVPMatrix;\n" +
                    "uniform mat4 uTexMatrix;\n" +
                    "attribute vec4 aPosition;\n" +
                    "attribute vec4 aTextureCoord;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "void main() {\n" +
                    "    gl_Position = uMVPMatrix * aPosition;\n" +
                    "    vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n" +
                    "}\n";


    private static final String FRAGMENT_SHADER_2D =
            "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform sampler2D sTexture;\n" +
                    "void main() {\n" +
                    "    gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                    "}\n";

    private static final FloatBuffer DEF_VERTEX_BUF = EglCore.createFloatBuffer(EglCore.CUBE);
    private static final FloatBuffer DEF_TEX_BUF = EglCore.createFloatBuffer(EglCore.TEXTURE_ROTATION_0);
    private static final FloatBuffer DEF_ROTATED_BUF = EglCore.createFloatBuffer(EglCore.TEXTURE_ROTATED_90);
    private static final FloatBuffer DEF_ROTATED_MIRROR_BUF = EglCore.createFloatBuffer(EglCore.TEXTURE_ROTATED_270_AND_Mirror_LR);

    private int mProgramHandle;
    private int muMVPMatrixLoc;
    private int maPositionLoc;
    private int maTextureCoordLoc;
    private int muTexMatrixLoc;
    private float[] mMVPMatrix = new float[16];
    private float[] mModeMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];

    public GLTexture2DFilter() {
        mProgramHandle      = EglCore.createProgram(VERTEX_SHADER, FRAGMENT_SHADER_2D);

        maPositionLoc       = GLES20.glGetAttribLocation(mProgramHandle, "aPosition");
        maTextureCoordLoc   = GLES20.glGetAttribLocation(mProgramHandle, "aTextureCoord");
        muMVPMatrixLoc      = GLES20.glGetUniformLocation(mProgramHandle, "uMVPMatrix");
        muTexMatrixLoc      = GLES20.glGetUniformLocation(mProgramHandle, "uTexMatrix");
    }

    public void release() {
        if (mProgramHandle != -1) {
            GLES20.glDeleteProgram(mProgramHandle);
            mProgramHandle = -1;
        }
    }

    public void draw(int textureId, int videoWidth, int videoHeight, int viewWidth, int viewHeight, boolean front) {
        EglCore.checkGlError("draw start");

        GLES20.glViewport(0, 0, viewWidth, viewHeight);

        GLES20.glClearColor(0.F, 0.F, 0.F, 1.F);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Select the program.
        GLES20.glUseProgram(mProgramHandle);
        EglCore.checkGlError("glUseProgram");

        // Set the texture.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

        Matrix.setIdentityM(mMVPMatrix, 0);

        // FIXME: 2021/3/24 这里先不管前置
//        if(front) {  // 判断前后置
            fill(mMVPMatrix, videoWidth, videoHeight, viewWidth, viewHeight);  // 前置是竖屏  高 > 宽
//        }else{
//            fill(mMVPMatrix, videoWidth, videoHeight, viewWidth, viewWidth);  // 后置是横屏  宽 > 高
//        }

        // Copy the model / view / projection matrix over.
        GLES20.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, mMVPMatrix, 0);
        EglCore.checkGlError("glUniformMatrix4fv");

        // Copy the texture transformation matrix over.
        GLES20.glUniformMatrix4fv(muTexMatrixLoc, 1, false, EglCore.IDENTITY_MATRIX, 0);
        EglCore.checkGlError("glUniformMatrix4fv");

        // Enable the "aPosition" vertex attribute.
        GLES20.glEnableVertexAttribArray(maPositionLoc);
        EglCore.checkGlError("glEnableVertexAttribArray");

        // Connect vertexBuffer to "aPosition".
        GLES20.glVertexAttribPointer(maPositionLoc, 2, GLES20.GL_FLOAT, false, 0, DEF_VERTEX_BUF);
        EglCore.checkGlError("glVertexAttribPointer");

        // Enable the "aTextureCoord" vertex attribute.
        GLES20.glEnableVertexAttribArray(maTextureCoordLoc);
        EglCore.checkGlError("glEnableVertexAttribArray");

        if(front) {
            // Connect texBuffer to "aTextureCoord".
            GLES20.glVertexAttribPointer(maTextureCoordLoc, 2, GLES20.GL_FLOAT, false, 0, DEF_TEX_BUF);
            EglCore.checkGlError("glVertexAttribPointer");
        }else {
            GLES20.glVertexAttribPointer(maTextureCoordLoc, 2, GLES20.GL_FLOAT, false, 0, DEF_ROTATED_MIRROR_BUF);
            EglCore.checkGlError("glVertexAttribPointer");
        }


        // Draw the rect.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        EglCore.checkGlError("glDrawArrays");

        // Done -- disable vertex array, texture, and program.
        GLES20.glDisableVertexAttribArray(maPositionLoc);
        GLES20.glDisableVertexAttribArray(maTextureCoordLoc);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glUseProgram(0);
    }

    private void fill(float[] MVPMatrix, int videoWidth, int videoHeight, int viewWidth, int viewHeight) {
        // 这里是采集的数据源宽高
        int scaleWidth  = videoWidth;
        int scaleHeight = videoHeight;

        // 这里是需要显示的view和采集的数据的比例
        float ratioWidth  =  viewWidth * 1.0f / scaleWidth;
        float ratioHeight =  viewHeight * 1.0f / scaleHeight;

//        float ratio;
//        if (ratioWidth * scaleHeight > viewHeight) {
//            ratio = ratioHeight;
//        } else {
//            ratio = ratioWidth;
//        }

        Matrix.setIdentityM(mModeMatrix, 0);
        // 这里做一次缩放 来匹配view和采集的数据
        Matrix.scaleM(mModeMatrix, 0, ratioWidth * 1.0f , ratioHeight * 1.0f, 1);
        /**
         * float[] m 参数 : 生成矩阵元素的 float[] 数组
         * int mOffset 参数 : 矩阵数组的起始偏移量;
         * float left, float right, float bottom, float top 参数 : 近平面的 左, 右, 下, 上 的值;
         * float near 参数 : 近平面 与 视点之间的距离;
         * float far 参数 : 远平面 与 视点之间的距离;
         */
        if (viewWidth > viewHeight) {
            Matrix.orthoM(mProjectionMatrix, 0, -1f, 1f, -1f, 1f, -1f, 1f);  // 正交投影
        } else {
            Matrix.orthoM(mProjectionMatrix, 0, -1f, 1f, -1f, 1f, -1f, 1f);
        }
        Matrix.multiplyMM(MVPMatrix, 0, mProjectionMatrix, 0, mModeMatrix, 0);  // 将两个4x4矩阵相乘，并将结果存储在第三个4x4矩阵中
    }
}
