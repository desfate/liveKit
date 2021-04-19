package com.github.desfate.videokit.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.github.desfate.videokit.R;
import com.github.desfate.videokit.interfaces.VideoPlayInterface;
import com.github.desfate.videokit.play.IVideoControl;
import com.github.desfate.videokit.play.SuperPlayerDef;
import com.github.desfate.videokit.play.VideoControl;
import com.github.desfate.videokit.play.VideoPlayEngine;
import com.github.desfate.videokit.ui.supervideo.PointSeekBar;

import java.util.Locale;

/**
 * 播放器View
 * 我是容器
 * 包含了视频控制器（管理播放过程中的逻辑）
 * 包含了播放器核心 (处理视频输出 以及 回调当前视频播放状态信息)
 *
 * 这里UI来自腾讯超级播放器
 */
public class VideoPlayView extends FrameLayout implements View.OnClickListener, VideoPlayInterface {
    View view;  //                              根
    private VideoControl videoControl;  //      控制器
    private VideoPlayEngine videoEngine;//      核心
    // views
    private ImageView playBtn;
    private TextView playTime;
    private PointSeekBar seekBar;
    private TextView allTime;
    private TextView typeTv;
    private LinearLayout replayLay;
    private ImageView back;
    private TextView title;
    private ImageView superplayer_iv_danmuku;
    private ImageView superplayer_iv_snapshot;
    private ImageView superplayer_iv_more;

    private String playURl;

    public VideoPlayView(@NonNull Context context) {
        super(context);
        initView();
    }

    public VideoPlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        view =  LayoutInflater.from(getContext()).inflate(R.layout.video_play_layout, this);
        playBtn = view.findViewById(R.id.superplayer_iv_pause);
        playTime = view.findViewById(R.id.superplayer_tv_current);
        seekBar = view.findViewById(R.id.superplayer_seekbar_progress);
        allTime = view.findViewById(R.id.superplayer_tv_duration);
        typeTv = view.findViewById(R.id.superplayer_tv_quality);
        replayLay = view.findViewById(R.id.superplayer_ll_replay);
        back = view.findViewById(R.id.superplayer_iv_back);
        title = view.findViewById(R.id.superplayer_tv_title);
        superplayer_iv_danmuku = view.findViewById(R.id.superplayer_iv_danmuku);
        superplayer_iv_snapshot = view.findViewById(R.id.superplayer_iv_snapshot);
        superplayer_iv_more = view.findViewById(R.id.superplayer_iv_more);

        playBtn.setOnClickListener(this);
        typeTv.setOnClickListener(this);
        replayLay.setOnClickListener(this);

        seekBar.setOnSeekBarChangeListener(new PointSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(PointSeekBar seekBar, int progress, boolean fromUser) {
                // 拖动中的情况
                if(fromUser){

                }
            }

            @Override
            public void onStartTrackingTouch(PointSeekBar seekBar) {
                // 当按下的时候
                if(videoEngine != null) videoEngine.pausePlay();
            }

            @Override
            public void onStopTrackingTouch(PointSeekBar seekBar) {
                // 当松开的时候
                if(videoEngine != null) {
                    videoEngine.setProgress(seekBar.getProgress());
                    videoEngine.resumePlay();
                }
            }
        });
    }

    public void init(String playURl) {
        this.playURl = playURl;
        // 初始化控制器以及核心
        videoControl = new VideoControl(view);
        videoEngine = new VideoPlayEngine();
        videoEngine.init(getContext(), this);
        videoEngine.startPlay(playURl);

    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.superplayer_iv_pause) {
            if(videoEngine.getState() == 1){  // 播放中
                videoControl.updatePlayState(SuperPlayerDef.PlayerState.PAUSE);  // 播放中
                videoEngine.pausePlay();
            }else if(videoEngine.getState() == 2){  // 暂停中
                videoControl.updatePlayState(SuperPlayerDef.PlayerState.PLAYING);  // 播放中
                videoEngine.resumePlay();
            }else if(videoEngine.getState() == 3){ // 播放结束
                videoEngine.startPlay(playURl);
            }
        } else if (id == R.id.superplayer_ll_replay) {  // 重播按钮
            videoEngine.startPlay(playURl);
        }
    }

    public void setPreviewSurface(Surface surface){
        videoEngine.setSurface(surface);
    }

    public void setPreviewSurfaceHolder(SurfaceHolder holder){
        videoEngine.setSurfaceHolder(holder);
    }

    public IVideoControl getControl(){
        return videoControl;
    }


    @Override
    public void startPlaying() {
        allTime.setText(generateTime(videoEngine.getVideoAllTime()));
        videoControl.updatePlayState(SuperPlayerDef.PlayerState.PLAYING);  // 播放中
    }

    @Override
    public void endPlay() {
        videoControl.updatePlayState(SuperPlayerDef.PlayerState.END);  // 播放完成
    }

    @Override
    public void playProgress(int progress) {
        seekBar.setProgress(progress);
    }

    @Override
    public void playProgressTime(long progress) {
        playTime.setText(generateTime(progress));
    }

    @Override
    public void startCache() {
        videoControl.updatePlayState(SuperPlayerDef.PlayerState.LOADING);  // 缓冲中
    }

    @Override
    public void cacheProgress(int progress) {

    }

    @Override
    public void stopCache() {
        videoControl.updatePlayState(SuperPlayerDef.PlayerState.PLAYING);  // 缓冲结束
    }


    /**
     * 将秒数转换为hh:mm:ss的格式
     */
    private static String generateTime(long position) {
        int totalSeconds = (int) (position / 1000);

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        if (hours > 0) {
            return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes,
                    seconds).toString();
        } else {
            return String.format(Locale.US, "%02d:%02d", minutes, seconds)
                    .toString();
        }
    }

    public void release(){
        if(videoEngine != null) {
            videoEngine.release();
        }
    }

    public ImageView getBackImg(){
        return back;
    }

    public TextView getTitleView(){
        return title;
    }

    public ImageView getDanmuImg(){
        return superplayer_iv_danmuku;
    }

    public ImageView getShotImg(){
        return superplayer_iv_snapshot;
    }

    public ImageView getMoreImg(){
        return superplayer_iv_more;
    }
}
