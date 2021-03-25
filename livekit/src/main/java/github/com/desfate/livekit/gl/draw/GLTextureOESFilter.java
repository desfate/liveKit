package github.com.desfate.livekit.gl.draw;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import github.com.desfate.livekit.gl.egl.EglCore;

/**
 * 将外部纹理转为普通纹理，需要在OpenGL环境中使用
 */
public class GLTextureOESFilter {

    private static final String TAG = "GLTextureOESFilter";
    private int mOutputWidth = 0;
    private int mOutputHeight = 0;
    private float[] mProjectionMatrix = new float[16];
    private float[] mModeMatrix = new float[16];

    private static final int FLOAT_SIZE_BYTES = 4;
    private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
    private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
    private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;
    private static final int INVALID_TEXTURE_ID = -12345;
    private final float[] mTriangleVerticesData = {
            // X, Y, Z, U, V
            -1.0f, -1.0f, 0, 0.f, 0.f,
            1.0f, -1.0f, 0, 1.f, 0.f,
            -1.0f,  1.0f, 0, 0.f, 1.f,
            1.0f,  1.0f, 0, 1.f, 1.f,
    };

    private FloatBuffer mTriangleVertices;

    private static final String VERTEX_SHADER =
            "uniform mat4 uMVPMatrix;\n" +
                    "uniform mat4 uSTMatrix;\n" +
                    "attribute vec4 aPosition;\n" +
                    "attribute vec4 aTextureCoord;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "void main() {\n" +
                    "  gl_Position = uMVPMatrix * aPosition;\n" +
                    "  vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n" +
                    "}\n";

    private static final String FRAGMENT_SHADER_OESTEX =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +      // highp here doesn't seem to matter
                    "varying vec2 vTextureCoord;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "void main() {\n" +
                    "  gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                    "}\n";

    private float[] mMVPMatrix = new float[16];
    private float[] mSTMatrix = new float[16];

    private int mProgram;
    private int mFrameBufferTextureID = INVALID_TEXTURE_ID;
    private int mFrameBufferID = INVALID_TEXTURE_ID;
    private int muMVPMatrixHandle;
    private int muSTMatrixHandle;
    private int maPositionHandle;
    private int maTextureHandle;

    public GLTextureOESFilter() {
        mTriangleVertices = ByteBuffer.allocateDirect(
                mTriangleVerticesData.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTriangleVertices.put(mTriangleVerticesData).position(0);

        Matrix.setIdentityM(mSTMatrix, 0);

        create();
    }

    public void setMatrix(float[] mtx) {
        mSTMatrix = mtx;
    }

    /**
     * 这里往texture预览上进行绘制
     * @param textureId
     * @return
     */
    public int drawToTexture(int textureId) {

        if (mFrameBufferID == INVALID_TEXTURE_ID) {
            Log.d(TAG, "invalid frame buffer id");
            return textureId;
        }
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBufferID);

        draw(textureId);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        return  mFrameBufferTextureID;
    }

    public void release() {
        if (mProgram != -1) {
            GLES20.glDeleteProgram(mProgram);
            mProgram = -1;
        }

        destroyFrameBuffer();
    }

    /**
     * Initializes GL state.
     */
    private void create() {
        mProgram = EglCore.createProgram(VERTEX_SHADER, FRAGMENT_SHADER_OESTEX);
        if (mProgram == 0) {
            throw new RuntimeException("failed creating program");
        }
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        EglCore.checkGlError("glGetAttribLocation aPosition");
        if (maPositionHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aPosition");
        }
        maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
        EglCore.checkGlError("glGetAttribLocation aTextureCoord");
        if (maTextureHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aTextureCoord");
        }

        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        EglCore.checkGlError("glGetUniformLocation uMVPMatrix");
        if (muMVPMatrixHandle == -1) {
            throw new RuntimeException("Could not get attrib location for uMVPMatrix");
        }

        muSTMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uSTMatrix");
        EglCore.checkGlError("glGetUniformLocation uSTMatrix");
        if (muSTMatrixHandle == -1) {
            throw new RuntimeException("Could not get attrib location for uSTMatrix");
        }
    }

    public void setOutputResolution(int width, int height) {
        if (width == mOutputWidth && height == mOutputHeight) {
            return;
        }
        Log.d(TAG, "Output resolution change: " + mOutputWidth + "*" + mOutputHeight + " -> " + width + "*" + height);
        mOutputWidth = 2560;
        mOutputHeight = 720;

//        if (width > height) {
//            Matrix.orthoM(mProjectionMatrix, 0, - 1.f, 1.f, -1f, 1f, -1f, 1f);
//        } else {
            Matrix.orthoM(mProjectionMatrix, 0, -1f, 1f, -1.f, 1.f, -1f, 1f);
//        }

        reloadFrameBuffer();
    }

    // 这个绘制完成后会提交给推流服务 注意 这个和本地预览会相互影响
    private void draw(int textureId) {
        // FIXME: 2021/3/25 提交给服务器的数据要做一次处理  需要把2560 * 720 的数据填充满 1920 1080 / 1280 720 的显示区域中


        // 这里是需要绘制输出的部分  上传到腾讯服务器上的时候最大只支持1920 1080
        GLES20.glViewport(0, 0, mOutputWidth, mOutputHeight);
//        GLES20.glViewport(0, 0, mOutputWidth, mOutputHeight);
        GLES20.glClearColor(0.F, 0.F, 0.F, 1.F);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        GLES20.glUseProgram(mProgram);
        EglCore.checkGlError("glUseProgram");


        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);

        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
        EglCore.checkGlError("glVertexAttribPointer maPosition");
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        EglCore.checkGlError("glEnableVertexAttribArray maPositionHandle");

        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
        GLES20.glVertexAttribPointer(maTextureHandle, 2, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
        EglCore.checkGlError("glVertexAttribPointer maTextureHandle");
        GLES20.glEnableVertexAttribArray(maTextureHandle);
        EglCore.checkGlError("glEnableVertexAttribArray maTextureHandle");

        Matrix.setIdentityM(mMVPMatrix, 0);  // 创建一个单位矩阵
        Matrix.setIdentityM(mModeMatrix, 0);
        // 用来进行图像的缩放，第一个参数是需要变换的矩阵；第三、四、五个参数分别对应x,y,z 方向的缩放比例，当x方向缩放为0.5时，相当于向x方向缩放为原来的0.5倍，其他类似。
        Matrix.scaleM(mModeMatrix, 0, -1, 1, 1);
        Matrix.rotateM(mModeMatrix, 0, 90, 0, 0, -1);
//        Matrix.rotateM(mModeMatrix, 0, 90, 1, 0, 0);
        // 将两个矩阵相乘 mMVPMatrix 是结果
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mModeMatrix, 0);

//        // 这个结果进行一次变换
//        fill(mMVPMatrix, mOutputWidth, mOutputHeight, 1920, 1080);
        // 通过一致变量（uniform修饰的变量）引用将一致变量值传入渲染管线
        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(muSTMatrixHandle, 1, false, mSTMatrix, 0);
        EglCore.checkGlError("glDrawArrays");
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        EglCore.checkGlError("glDrawArrays");

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);

//        GLES20.glFinish();
    }

    private void reloadFrameBuffer() {

        Log.d(TAG, "reloadFrameBuffer. size = " + mOutputWidth + "*" + mOutputHeight);
        destroyFrameBuffer();

        int[] textures = new int[1];
        int[] frameBuffers = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glGenFramebuffers(1, frameBuffers, 0);

        mFrameBufferTextureID = textures[0];
        mFrameBufferID = frameBuffers[0];
        Log.d(TAG, "frameBuffer id = " + mFrameBufferID + ", texture id = " + mFrameBufferTextureID);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFrameBufferTextureID);
        EglCore.checkGlError("glBindTexture mFrameBufferTextureID");
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, mOutputWidth, mOutputHeight, 0,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
        EglCore.checkGlError("glTexParameter");

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBufferID);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, mFrameBufferTextureID, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

    }

    private void destroyFrameBuffer() {
        if (mFrameBufferID != INVALID_TEXTURE_ID) {
            int[] frameID = new int[1];
            frameID[0] = mFrameBufferID;
            GLES20.glDeleteFramebuffers(1, frameID,0);
            mFrameBufferID = INVALID_TEXTURE_ID;
        }
        if (mFrameBufferTextureID != INVALID_TEXTURE_ID) {
            int[] textureID = new int[1];
            textureID[0] = mFrameBufferTextureID;
            GLES20.glDeleteTextures(1, textureID, 0);
            mFrameBufferTextureID = INVALID_TEXTURE_ID;
        }
    }

    /**
     * 将输入的数据通过变换转换成指定数据输出
     * @param MVPMatrix    视频数组
     * @param videoWidth   输入视频宽度
     * @param videoHeight  输入视频高度
     * @param viewWidth    输出视频宽度
     * @param viewHeight   输出视频高度
     */
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
//        if (viewWidth > viewHeight) {
//            Matrix.orthoM(mProjectionMatrix, 0, -1f, 1f, -1f, 1f, -1f, 1f);  // 正交投影
//        } else {
//            Matrix.orthoM(mProjectionMatrix, 0, -1f, 1f, -1f, 1f, -1f, 1f);
//        }
//        Matrix.multiplyMM(MVPMatrix, 0, mProjectionMatrix, 0, mModeMatrix, 0);  // 将两个4x4矩阵相乘，并将结果存储在第三个4x4矩阵中
    }

}
