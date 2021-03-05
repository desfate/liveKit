package github.com.desfate.livekit.dual;

import android.opengl.GLES30;
import android.view.SurfaceView;

import com.future.Holography.RenderDrawByC;


public class RenderVideo extends RenderBase {

    public RenderVideo(SurfaceView mv) {
        initShader(mv);
    }

    boolean mis2d = false;

    public void setIs2D(boolean is2d) {
        mis2d = is2d;
    }

    public void initShader(SurfaceView mv) {
        mVertexShader = ShaderUtil.loadFromAssetsFile("vertex2d.sh", mv.getResources());
        mFragmentShader = ShaderUtil.loadFromAssetsFile("frag2d.sh", mv.getResources());
        mProgram = ShaderUtil.createProgram(mVertexShader, mFragmentShader);
        if (mProgram == 0) {
            return;
        }
        maPositionHandle = GLES30.glGetAttribLocation(mProgram, "aPosition");
        checkGlError("glGetAttribLocation aPosition");
        if (maPositionHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aPosition");
        }
        maTextureHandle = GLES30.glGetAttribLocation(mProgram, "aTextureCoord");
        checkGlError("glGetAttribLocation aTextureCoord");
        if (maTextureHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aTextureCoord");
        }

        muSTMatrixHandle = GLES30.glGetUniformLocation(mProgram, "uSTMatrix");
        checkGlError("glGetUniformLocation uSTMatrix");
        if (muSTMatrixHandle == -1) {
            throw new RuntimeException("Could not get attrib location for uSTMatrix");
        }
    }

    public void drawSelfLeft(int textureId) {
        GLES30.glUseProgram(mProgram);
        checkGlError("glUseProgram");

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureId);

        RenderDrawByC.drawRender2D(maPositionHandle, maTextureHandle);

        GLES30.glUniformMatrix4fv(muSTMatrixHandle, 1, false, mSTMatrix, 0);

        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);
        checkGlError("glDrawArrays");

    }


    public void drawSelfRight(int textureId) {
        GLES30.glUseProgram(mProgram);
        checkGlError("glUseProgram");

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureId);

        RenderDrawByC.drawRender2DR(maPositionHandle, maTextureHandle);

        GLES30.glUniformMatrix4fv(muSTMatrixHandle, 1, false, mSTMatrix, 0);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);
        checkGlError("glDrawArrays");

    }

    public void drawSelfTop(int textureId) {
        GLES30.glUseProgram(mProgram);
        checkGlError("glUseProgram");

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureId);

        RenderDrawByC.drawRender2DTop(maPositionHandle, maTextureHandle);

        GLES30.glUniformMatrix4fv(muSTMatrixHandle, 1, false, mSTMatrix, 0);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);
        checkGlError("glDrawArrays");
    }

    public void drawSelfBottom(int textureId) {
        GLES30.glUseProgram(mProgram);
        checkGlError("glUseProgram");

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureId);

        RenderDrawByC.drawRender2DBottom(maPositionHandle, maTextureHandle);

        GLES30.glUniformMatrix4fv(muSTMatrixHandle, 1, false, mSTMatrix, 0);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);
        checkGlError("glDrawArrays");
    }

    public void drawSelf(int textureId) {
        checkGlError("glFramebufferRenderbuffer");

        GLES30.glUseProgram(mProgram);
        checkGlError("glUseProgram");

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureId);


        if (mis2d) {
            RenderDrawByC.drawRender2D(maPositionHandle, maTextureHandle);
        } else {
            RenderDrawByC.drawRender(maPositionHandle, maTextureHandle);
        }

        /*
        mCurrentVerticesT.position(0);
        GLES30.glVertexAttribPointer(maPositionHandle, 3, GLES30.GL_FLOAT, false,
                VERTICES_DATA_T_STRIDE_BYTES, mCurrentVerticesT);
        checkGlError("glVertexAttribPointer maPosition");
        GLES30.glEnableVertexAttribArray(maPositionHandle);
        checkGlError("glEnableVertexAttribArray maPositionHandle");

        mCurrentVerticesV.position(0);
        GLES30.glVertexAttribPointer(maTextureHandle, 3, GLES30.GL_FLOAT, false,
	             VERTICES_DATA_V_STRIDE_BYTES, mCurrentVerticesV);

        checkGlError("glVertexAttribPointer maTextureHandle");
        GLES30.glEnableVertexAttribArray(maTextureHandle);
        checkGlError("glEnableVertexAttribArray maTextureHandle");
//		*/

        GLES30.glUniformMatrix4fv(muSTMatrixHandle, 1, false, mSTMatrix, 0);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);
        checkGlError("glDrawArrays");

    }

}
