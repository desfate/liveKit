package com.github.desfate.videokit.play;

import android.content.Context;
import android.os.CountDownTimer;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.github.desfate.videokit.interfaces.VideoPlayInterface;
import com.pili.pldroid.player.AVOptions;
import com.pili.pldroid.player.PLMediaPlayer;
import com.pili.pldroid.player.PLOnErrorListener;
import com.pili.pldroid.player.PLOnImageCapturedListener;
import com.pili.pldroid.player.PLOnInfoListener;

import java.io.IOException;

/**
 * 播放器核心 使用的七牛提供的播放器核心
 * <p>
 * 支持网络播放以及支持缓存等功能
 */
public class VideoPlayEngine {

    private PLMediaPlayer mediaPlayer;
    private AVOptions options;

    VideoPlayInterface playInterface;
    private long videoAllTime = 0;  // 视频总时间
    private long mLastUpdateStatTime = 0;
    private int lastProgress = 0;

    private int state = 0; // 0: normal  1: playing 2: pause 3：end

    CountDownTimer timer; // 倒计时 用来处理进度条跳条问题


    public void init(Context context, VideoPlayInterface playInterface) {
        options = new AVOptions();
        options.setInteger(AVOptions.KEY_PREPARE_TIMEOUT, 10 * 1000);
        options.setInteger(AVOptions.KEY_MEDIACODEC, AVOptions.MEDIA_CODEC_SW_DECODE);
        options.setInteger(AVOptions.KEY_LOG_LEVEL, 0);
        options.setInteger(AVOptions.KEY_SEEK_MODE, 0);

        mediaPlayer = new PLMediaPlayer(context, options);
        this.playInterface = playInterface;
        setListener();
    }

    private void setListener() {
        mediaPlayer.setOnPreparedListener(i -> {
            videoAllTime = mediaPlayer.getDuration();
            mediaPlayer.start();  //  准备完成后开始自动播放视频
            state = 1;
            if(playInterface != null) playInterface.startPlaying();  // 开始播放
        });


        mediaPlayer.setOnVideoSizeChangedListener((i, i1) -> {
            // 视频大小变化 如果后续有切换全屏功能  则需要使用这个
        });

        mediaPlayer.setOnCompletionListener(() -> {
            // 当调用的播放器的 seekTo 方法后，SDK 会在 seek 成功后触发该回调
            // 播放完成会在这回调
            if(playInterface != null) {
                playInterface.endPlay();
                // 这里解决一下seek对齐问题 到最后会出现跳条问题
                // 这里应该做一次平滑操作
                if(lastProgress == 100){
                    return;
                }else{
                    // 开启倒计时器  尽量把最后平滑做在一秒之内
                    timer = new CountDownTimer((100 - lastProgress) * 50L, 50) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            playInterface.playProgress(lastProgress ++);
                        }

                        @Override
                        public void onFinish() {
                            playInterface.playProgress(100);
                        }
                    };
                    timer.start();
                }
            }
            state = 3; // 播放完成
        });

        mediaPlayer.setOnSeekCompleteListener(() -> {
            System.out.println("@@@@@@ = SeekComplete");
        });

        mediaPlayer.setOnBufferingUpdateListener(i -> {
            // 缓冲进度 就是i
            if(playInterface != null) playInterface.cacheProgress(i);
            long current = System.currentTimeMillis();
            if (current - mLastUpdateStatTime > 3000) {
                mLastUpdateStatTime = current;
            }
        });

        // 视频播放流程中的一系列信号通知
        mediaPlayer.setOnInfoListener((i, i1) -> {
            //MEDIA_INFO_UNKNOWN	1	未知消息
            //MEDIA_INFO_VIDEO_RENDERING_START	3	第一帧视频已成功渲染
            //MEDIA_INFO_CONNECTED	200	连接成功
            //MEDIA_INFO_METADATA	340	读取到 metadata 信息
            //MEDIA_INFO_BUFFERING_START	701	开始缓冲
            //MEDIA_INFO_BUFFERING_END	702	停止缓冲
            //MEDIA_INFO_SWITCHING_SW_DECODE	802	硬解失败，自动切换软解
            //MEDIA_INFO_CACHE_DOWN	901	预加载完成
            //MEDIA_INFO_LOOP_DONE	8088	loop 中的一次播放完成
            //MEDIA_INFO_VIDEO_ROTATION_CHANGED	10001	获取到视频的播放角度
            //MEDIA_INFO_AUDIO_RENDERING_START	10002	第一帧音频已成功播放
            //MEDIA_INFO_VIDEO_GOP_TIME	10003	获取视频的I帧间隔
            //MEDIA_INFO_VIDEO_BITRATE	20001	视频的码率统计结果
            //MEDIA_INFO_VIDEO_FPS	20002	视频的帧率统计结果
            //MEDIA_INFO_AUDIO_BITRATE	20003	音频的帧率统计结果
            //MEDIA_INFO_AUDIO_FPS	20004	音频的帧率统计结果
            //MEDIA_INFO_VIDEO_FRAME_RENDERING	10004	视频帧的时间戳
            //MEDIA_INFO_AUDIO_FRAME_RENDERING	10005	音频帧的时间戳
            //MEDIA_INFO_CACHED_COMPLETE	1345	离线缓存的部分播放完成
            //MEDIA_INFO_IS_SEEKING	565	上一次 seekTo 操作尚未完成
            switch (i) {
                // 用于检测FPS和码率
                case (PLOnInfoListener.MEDIA_INFO_VIDEO_BITRATE):
                case (PLOnInfoListener.MEDIA_INFO_VIDEO_FPS):
                    break;
                // 当前播放到的时间戳
                case (PLOnInfoListener.MEDIA_INFO_VIDEO_FRAME_RENDERING):
                    // 这里计算一下播放进度的百分比 直接返回百分比
                    // FIXME: 2021/4/16 这里有点问题 这里会出现时间偏差  进过几次测试 发现有4个点左右的偏差
                    System.out.println("@@@@@@ = FRAME_RENDERING = " +i1);
                    if(playInterface != null) {
                        lastProgress = (int)((double)i1 * 100 / videoAllTime);
                        playInterface.playProgress(lastProgress);
                        playInterface.playProgressTime(i1);
                    }
                    break;
                // 开始缓存
                case (PLOnInfoListener.MEDIA_INFO_BUFFERING_START):
                    if(playInterface != null) playInterface.startCache();
                    break;
                // 结束缓存
                case (PLOnInfoListener.MEDIA_INFO_BUFFERING_END):
                    if(playInterface != null) playInterface.stopCache();
                    break;
                case (PLOnInfoListener.MEDIA_INFO_IS_SEEKING):
                    System.out.println("@@@@@@ = SeekComplete error");
                    break;
            }
        });

        // 一些错误回调
        mediaPlayer.setOnErrorListener(new PLOnErrorListener() {
            @Override
            public boolean onError(int i) {
                //MEDIA_ERROR_UNKNOWN	-1	未知错误
                //ERROR_CODE_OPEN_FAILED	-2	播放器打开失败
                //ERROR_CODE_IO_ERROR	-3	网络异常
                //ERROR_CODE_SEEK_FAILED	-4	拖动失败
                //ERROR_CODE_CACHE_FAILED	-5	预加载失败
                //ERROR_CODE_HW_DECODE_FAILURE	-2003	硬解失败
                //ERROR_CODE_PLAYER_DESTROYED	-2008	播放器已被销毁，需要再次 setVideoURL 或 prepareAsync
                //ERROR_CODE_PLAYER_VERSION_NOT_MATCH	-9527	so 库版本不匹配，需要升级
                //ERROR_CODE_PLAYER_CREATE_AUDIO_FAILED	-4410	AudioTrack 初始化失败，可能无法播放音频
                return false;
            }
        });

        // 该回调用于监听 captureImage 完成的消息，当调用播放器的 captureImage 方法后，SDK 会在相应内容截取完成后触发该回调
        mediaPlayer.setOnImageCapturedListener(new PLOnImageCapturedListener() {
            @Override
            public void onImageCaptured(byte[] bytes) {

            }
        });
    }

    public long getVideoAllTime(){
        return videoAllTime;
    }

    public void setProgress(int progress){
        System.out.println("@@@@@@ = Seek to "+ progress);
        mediaPlayer.seekTo(videoAllTime * progress / 100);
    }

    /**
     * 获得当前播放器状态
     * @return 播放器状态
     */
    public int getState(){
        return state;
    }

    /**
     * 播放的网路地址
     *
     * @param url 网络视频源
     */
    public void startPlay(String url) {
        url = "http://demo-videos.qnsdk.com/movies/qiniu.mp4";
        try {
            state = 0;
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync(); // 开始准备同步  同时会在上方setOnPreparedListener中回调准备成功
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 暂停播放
     */
    public void pausePlay(){
        state = 2;
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    /**
     * 恢复播放
     */
    public void resumePlay(){
        state = 1;
        if(mediaPlayer != null){
            mediaPlayer.start();
        }
    }

    /**
     * 支持外部渲染 数据是单独放到一个外部Surface中进行渲染的
     *
     * @param surface 传入容器
     */
    public void setSurface(Surface surface) {
        if (mediaPlayer != null && surface != null) {
            mediaPlayer.setSurface(surface);
        }
    }

    public void setSurfaceHolder(SurfaceHolder holder){
        if(mediaPlayer != null && holder != null){
            mediaPlayer.setDisplay(holder);
        }
    }

    /**
     * 释放播放器资源
     */
    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        if(timer != null){
            timer.onFinish();
            timer.cancel();
            timer = null;
        }
        if(playInterface != null){
            playInterface = null;
        }
    }




}
