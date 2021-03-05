package github.com.desfate.livekit.dual;

import android.opengl.GLES10;
import android.opengl.GLES20;
import android.util.Log;
import android.view.SurfaceView;

import com.future.Holography.Holography;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;


public class Render3D {
    public String TAG = "Holography";
    public  final int GL_TEXTURE_EXTERNAL_OES = 0x8D65;
    private int mProgram;
    private int maPositionHandle;
    private int maTextureHandle;

    private int muSapler0;
    private int muSapler1;
    private int muSapler2;
    //private int muSapler3;

    int    posTexture1;
    // int    posTexture2;
    // int    posTexture3;

    int mposPerOffset;

    float mperOffset = 0;

    public final static float mscopePerOffset = 0.02f;


    String mVertexShader;
    String mFragmentShader;

    private FloatBuffer mVertices;

    private static final int FLOAT_SIZE_BYTES = 4;
    private static final int VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
    private static final int VERTICES_DATA_POS_OFFSET = 0;
    private static final int VERTICES_DATA_UV_OFFSET = 3;

    private final float[] mVerticesData = {
            // X, Y, Z, U, V
            -1.0f, -1.0f, 0, 0.f, 0.f,
            1.0f, -1.0f, 0, 1.f, 0.f,
            -1.0f, 1.0f, 0, 0.f, 1.f,

            -1.0f,  1.0f, 0, 0.f, 1.f,
            1.0f,   -1.0f, 0, 1.f, 0.f,
            1.0f,   1.0f, 0, 1.f, 1.f,};

    int vCount=6;



    //ShowByGyro useGyro = ShowByGyro.getSelf();


    public Render3D(SurfaceView sv, int lrsx ){

        initVertexData();

        if( lrsx == 0 ) {
            initShader(sv);
        }
        else {
            initShaderSX(sv);
        }

        posTexture1 = ShaderUtil.initTexture();
        // posTexture2 = ShaderUtil.initTexture();
        //  posTexture3 = ShaderUtil.initTexture();

        //  Log.d(TAG,"texture: " + posTexture1 + " " + posTexture2 + " " + posTexture3);

        // 	Holography.setTexture(posTexture1);

        updateDelt();
    }

    public int setPerOffset(float peroffset)
    {
        if( peroffset > mscopePerOffset || peroffset < -mscopePerOffset ) {
            Log.w(TAG, "peroffset extend scope");
            return -1;
        }
        mperOffset = peroffset;
        return 0;
    }


    public void initVertexData() {
        Log.d(TAG, "initVertexData");
        mVertices = ByteBuffer
                .allocateDirect(mVerticesData.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mVertices.put(mVerticesData).position(0);

    }

    public void initShaderSX(SurfaceView sv) {
        Log.d(TAG, "initShader");
        mVertexShader = ShaderUtil.loadFromAssetsFile("vertex3D.sh",
                sv.getResources());
        mFragmentShader = ShaderUtil.loadFromAssetsFile("frag3Dsx.sh",
                sv.getResources());
        mProgram = ShaderUtil.createProgram(mVertexShader, mFragmentShader);
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        if (maPositionHandle == -1) {
            throw new RuntimeException(
                    "Could not get attrib location for aPosition");
        }
        maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
        checkGlError("glGetAttribLocation aTextureCoord");
        if (maTextureHandle == -1) {
            throw new RuntimeException(
                    "Could not get attrib location for aTextureCoord");
        }

        muSapler0 = GLES20.glGetUniformLocation(mProgram, "Sampler0");
        if (muSapler0 == -1) {
            throw new RuntimeException(
                    "Could not get attrib location for muSapler0");
        }

        muSapler1 = GLES20.glGetUniformLocation(mProgram, "Sampler1");
        if (muSapler1 == -1) {
            throw new RuntimeException(
                    "Could not get attrib location for muSapler1");
        }

    }

    public void initShader(SurfaceView sv) {
        Log.d(TAG, "initShader");
        mVertexShader = ShaderUtil.loadFromAssetsFile("vertex3D.sh",
                sv.getResources());
        //  mFragmentShader = ShaderUtil.loadFromAssetsFile("frag3DOLED.sh",
        //       sv.getResources());
        mFragmentShader = ShaderUtil.loadFromAssetsFile("frag3D.sh",
                sv.getResources());
        mProgram = ShaderUtil.createProgram(mVertexShader, mFragmentShader);
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        if (maPositionHandle == -1) {
            throw new RuntimeException(
                    "Could not get attrib location for aPosition");
        }
        maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
        checkGlError("glGetAttribLocation aTextureCoord");
        if (maTextureHandle == -1) {
            throw new RuntimeException(
                    "Could not get attrib location for aTextureCoord");
        }

        muSapler0 = GLES20.glGetUniformLocation(mProgram, "Sampler0");
        if (muSapler0 == -1) {
            throw new RuntimeException(
                    "Could not get attrib location for muSapler0");
        }

        muSapler1 = GLES20.glGetUniformLocation(mProgram, "Sampler1");
        if (muSapler1 == -1) {
            throw new RuntimeException("Could not get attrib location for muSapler1");
        }
        //    muSapler2 = GLES20.glGetUniformLocation(mProgram, "Sampler2");
        //     if (muSapler2 == -1) {
        //       throw new RuntimeException("Could not get attrib location for muSapler2");
        //    }
        //    muSapler3 = GLES20.glGetUniformLocation(mProgram, "Sampler3");
        //   if (muSapler3 == -1) {
        //     throw new RuntimeException("Could not get attrib location for muSapler3");
        //     }
        //   mposPerOffset = GLES20.glGetUniformLocation(mProgram, "perOffset");
        //   if (mposPerOffset == -1) {
        //   	throw new RuntimeException("Could not get attrib location for mposPerOffset");
        //	}

    }

    public void drawSelf(int texId) {

        GLES20.glClearColor(0.f, 0.f, 0.f, 1.0f);
        GLES20.glUseProgram(mProgram);
        checkGlError("glUseProgram");
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        updateDelt();

        mVertices.position(VERTICES_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT,
                false, VERTICES_DATA_STRIDE_BYTES, mVertices);
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        checkGlError("glEnableVertexAttribArray maPositionHandle2");

        mVertices.position(VERTICES_DATA_UV_OFFSET);
        GLES20.glVertexAttribPointer(maTextureHandle, 3, GLES20.GL_FLOAT,
                false, VERTICES_DATA_STRIDE_BYTES, mVertices);

        checkGlError("glVertexAttribPointer maTextureHandle2");
        GLES20.glEnableVertexAttribArray(maTextureHandle);
        checkGlError("glEnableVertexAttribArray maTextureHandle2");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId);
        GLES20.glUniform1i(muSapler0, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, posTexture1);
        GLES20.glUniform1i(muSapler1, 1);

        //  GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        //  GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, posTexture2);
        //  GLES20.glUniform1i(muSapler2,2);

        //   GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
        //   GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, posTexture3);
        //  GLES20.glUniform1i(muSapler3, 3);

        //  GLES20.glUniform1f(mposPerOffset, mperOffset);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
        checkGlError("glDrawArrays");
    }

    public void drawSelf(int texId1,int texId2) {

        GLES20.glClearColor(0.f, 0.f, 0.f, 1.0f);
        GLES20.glUseProgram(mProgram);
        checkGlError("glUseProgram");
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        updateDelt();

        mVertices.position(VERTICES_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT,
                false, VERTICES_DATA_STRIDE_BYTES, mVertices);
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        checkGlError("glEnableVertexAttribArray maPositionHandle2");

        mVertices.position(VERTICES_DATA_UV_OFFSET);
        GLES20.glVertexAttribPointer(maTextureHandle, 3, GLES20.GL_FLOAT,
                false, VERTICES_DATA_STRIDE_BYTES, mVertices);

        checkGlError("glVertexAttribPointer maTextureHandle2");
        GLES20.glEnableVertexAttribArray(maTextureHandle);
        checkGlError("glEnableVertexAttribArray maTextureHandle2");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, texId1);
        GLES20.glUniform1i(muSapler0, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, posTexture1);
        GLES20.glUniform1i(muSapler1, 1);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, texId2);
        GLES20.glUniform1i(muSapler2,2);

        //   GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
        //   GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, posTexture3);
        //  GLES20.glUniform1i(muSapler3, 3);

        //  GLES20.glUniform1f(mposPerOffset, mperOffset);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
        checkGlError("glDrawArrays");
    }

    private void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }

    private void  updateDelt(){

        //Log.d(TAG,"updateDelt: " + posTexture1 + " " + posTexture2 + " " + posTexture3);

        // 	GLES10.glBindTexture(GLES10.GL_TEXTURE_2D, posTexture1);
        // 	Log.d(TAG,"updateDelt1");
        //    if(useGyro.isUseGyro()) {
        //     Holography.updateAddGyro(1, useGyro.calVal());
        //  } else {
        //       Holography.update2(1);
        //   }

        //  checkGlError("update2(1)");
        //   GLES10.glBindTexture(GLES10.GL_TEXTURE_2D, posTexture2);
        // 	Log.d(TAG,"updateDelt2");
        //  if(useGyro.isUseGyro()) {
        //       Holography.updateAddGyro(2, useGyro.calVal());
        //    } else {
        //       Holography.update2(2);
        //   }

        //    checkGlError("update2(2)");
        //    GLES10.glBindTexture(GLES10.GL_TEXTURE_2D, posTexture3);
        // 	Log.d(TAG,"updateDelt3");
        //    if(useGyro.isUseGyro()) {
        //         Holography.updateAddGyro(3, useGyro.calVal());
        //     } else {
        //        Holography.update2(3);
        //    }
        GLES10.glBindTexture(GLES10.GL_TEXTURE_2D, posTexture1);
//         	Log.e(TAG,"updateDelt");

        Holography.update(0,0);

        checkGlError("update2(3)");
    }
}
