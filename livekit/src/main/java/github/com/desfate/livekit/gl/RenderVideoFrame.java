package github.com.desfate.livekit.gl;

import android.graphics.SurfaceTexture;
import android.opengl.EGLContext;
import android.opengl.GLES20;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import java.nio.ByteBuffer;

import github.com.desfate.livekit.gl.thread.GLThreadHandler;
import github.com.desfate.livekit.gl.draw.GLI420RenderFilter;
import github.com.desfate.livekit.gl.draw.GLTexture2DFilter;
import github.com.desfate.livekit.gl.interfaces.IEGLListener;
import github.com.desfate.livekit.gl.interfaces.VideoRenderListener;

/**
 * 定义渲染功能
 *
 *  onRenderVideoFrame 回调回来的每一帧数据  这边通过自定义渲染  显示成本地预览
 *
 */
public class RenderVideoFrame {

    private final static String TAG = "RenderVideoFrame";

    private IEGLListener ieglListener;
    private VideoRenderListener renderListener;

    private GLThreadHandler mGLHandler;
    private HandlerThread mGLThread;

    private TextureView mRenderView;
    private SurfaceTexture mSurfaceTexture;

    private GLI420RenderFilter mYUVFilter;
    private GLTexture2DFilter mTextureFilter;

//    private boolean updateSize = false;
    private int mTextureId = -1;
    private int mVideoWidth;
    private int mVideoHeight;
    private ByteBuffer mYdata;
    private ByteBuffer mUVData;
    private boolean front;

    public void changeCamera(){
        this.front = !front;
//        System.out.println( "front = " + this.front);
    }

    public RenderVideoFrame(Size size, final boolean front){
        this.front = front;

        mVideoWidth = size.getWidth();
        mVideoHeight = size.getHeight();

        ieglListener = new IEGLListener() {
            @Override
            public void onEGLCreate() {
                initGL();
            }

            @Override
            public void onTextureProcess(EGLContext eglContext) {
                if (mRenderView != null) {
                    if (mTextureId != -1) {
                        if (mTextureFilter != null) {
                            // 这里是本地预览 + 二次渲染
                            mTextureFilter.draw(mTextureId, mVideoWidth, mVideoHeight, mRenderView.getWidth(), mRenderView.getHeight(), RenderVideoFrame.this.front);
                        }
                        mGLHandler.swap();
                        mTextureId = -1;
                    } else if (mUVData != null && mYdata != null) {
                        ByteBuffer yData = null;
                        ByteBuffer uvData = null;
                        synchronized (this) {
                            yData = mYdata;
                            uvData = mUVData;
                            mYdata = null;
                            mUVData = null;
                        }
                        if (mYUVFilter != null && yData != null && uvData != null) {
                            mYUVFilter.drawFrame(yData, uvData, mVideoWidth, mVideoHeight, mRenderView.getWidth(), mRenderView.getHeight());
                            mGLHandler.swap();
                        }
                    }
                }
            }

            @Override
            public void onEGLDestroy() {
                unInitGL();
            }
        };

        renderListener = new VideoRenderListener() {
            @Override
            public void onRenderVideoFrame(int textureId, EGLContext eglContext) {
                //
                if (mGLHandler == null) {
                    mRenderType = RENDER_TYPE_TEXTURE;
                    //OpenGL渲染线程共享SDK的eglContext
                    // 这里是本地预览时的渲染代码
                    createGLThread(eglContext);
                }

                mTextureId = textureId;
                GLES20.glFinish();
                sendMsg(GLThreadHandler.MSG_REND);
            }
        };
    }

    public void start(TextureView textureView){
        if(textureView == null) return;
        mRenderView = textureView; //保存视频渲染view，在渲染时用来获取渲染画布尺寸
        mRenderView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
                mSurfaceTexture = surfaceTexture;//         保存surfaceTexture，用于创建OpenGL线程
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
                mSurfaceTexture = surfaceTexture;//         保存surfaceTexture，用于创建OpenGL线程
                // 更新OpenGL线程中的surface
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                //surface释放了，需要停止渲染
                mSurfaceTexture = null;
                destroyGLThread();
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

            }
        });
    }


    private static final int RENDER_TYPE_TEXTURE = 0;
    private static final int RENDER_TYPE_I420 = 1;

    private int mRenderType = RENDER_TYPE_TEXTURE;


    /**
     * 创建OpenGL线程，绑定TextureView的SurfaceTexture和SDK 的EGLContext
     */
    private void createGLThread(EGLContext eglContext) {
        if (mSurfaceTexture == null) return;
        destroyGLThread();
        synchronized (this) {
            mGLThread = new HandlerThread(TAG);
            mGLThread.start();
            mGLHandler = new GLThreadHandler(mGLThread.getLooper());
            mGLHandler.mSurface = new Surface(mSurfaceTexture);
            mGLHandler.mEgl14Context = eglContext;
            mGLHandler.setListener(ieglListener);
            Log.w(TAG, "surface-render: create gl thread " + mGLThread.getName());
        }
        sendMsg(GLThreadHandler.MSG_INIT);
    }

    private void initGL() {
        if (mRenderType == RENDER_TYPE_TEXTURE) {
            mTextureFilter = new GLTexture2DFilter();
        } else if (mRenderType == RENDER_TYPE_I420) {
            mYUVFilter = new GLI420RenderFilter();
        }
    }

    private void sendMsg(int what) {
        synchronized (this) {
            if (mGLHandler != null) {
                mGLHandler.sendEmptyMessage(what);
            }
        }
    }

    private void unInitGL() {
        if (mTextureFilter != null) {
            mTextureFilter.release();
            mTextureFilter = null;
        }
        if (mYUVFilter != null) {
            mYUVFilter.release();
            mYUVFilter = null;
        }
    }


    private void destroyGLThread() {
        synchronized (this) {
            if (mGLHandler != null) {
                GLThreadHandler.quitGLThread(mGLHandler, mGLThread);
                Log.w(TAG, "surface-render: destroy gl thread");
            }
            mGLHandler = null;
            mGLThread = null;
        }
    }

    public VideoRenderListener getRenderFrame(){
        return this.renderListener;
    }


}
