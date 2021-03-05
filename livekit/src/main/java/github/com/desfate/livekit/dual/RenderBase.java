package github.com.desfate.livekit.dual;

import android.opengl.GLES30;
import android.util.Log;

import java.nio.FloatBuffer;

public class RenderBase {
    static String TAG = "DualCamera";

    public  final int GL_TEXTURE_EXTERNAL_OES = 0x8D65;

    public int mProgram;
    public int muSTMatrixHandle;
    public int maPositionHandle;
    public int maTextureHandle;

    String mVertexShader;
    String mFragmentShader;

    FloatBuffer mVertexBuffer;
    FloatBuffer mTexCoorBuffer;

    public float[] mSTMatrix = {
            1,0,0,0,
            0,1,0,0,
            0,0,1,0,
            0,0,0,1
    };

    public float[] mIdeMatrix = {
            1,0,0,0,
            0,1,0,0,
            0,0,1,0,
            0,0,0,1
    };

    public float[] mTestMatrix = {
            1,0,0,0,
            0,1,0,0,
            0,0,1,0,
            0,0,0,1
    };

    public RenderBase(){

    }

    public void setTransformMatrix(float[] mtx){
        mSTMatrix = mtx;
    }


    public void checkGlError(String op) {
        int error;
        while ((error = GLES30.glGetError()) != GLES30.GL_NO_ERROR) {
            Log.e(TAG, op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }


    public void setDelt(int n) {
        // TODO Auto-generated method stub

    }

}