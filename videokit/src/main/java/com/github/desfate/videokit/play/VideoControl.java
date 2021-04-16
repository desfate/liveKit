package com.github.desfate.videokit.play;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.github.desfate.videokit.R;

public class VideoControl implements IVideoControl{

    private SuperPlayerDef.PlayerState mCurrentPlayState;
    private ProgressBar mPbLiveLoading;
    private LinearLayout mLayoutReplay;


    private LinearLayout rplayer_ll_bottom; // 底部栏
    private LinearLayout title_lay;//          头部标题
    private LinearLayout title_control_lay;//  头部控制区

    private ImageView imageView;
    private ImageView lockbtn;

    boolean isLocked = false;

    public VideoControl(View view){
        mPbLiveLoading = view.findViewById(R.id.superplayer_pb_live);
        mLayoutReplay = view.findViewById(R.id.superplayer_ll_replay);
        imageView = view.findViewById(R.id.superplayer_iv_pause);
        lockbtn = view.findViewById(R.id.superplayer_iv_lock);
        rplayer_ll_bottom = view.findViewById(R.id.rplayer_ll_bottom);
        title_lay = view.findViewById(R.id.title_lay);
        title_control_lay = view.findViewById(R.id.title_control_lay);
        setListener();
    }

    private void setListener() {
        lockbtn.setOnClickListener(v -> {
            if(isLocked){
                isLocked = false;
                lockbtn.setImageResource(R.mipmap.superplayer_ic_player_unlock);
                rplayer_ll_bottom.setVisibility(View.VISIBLE);
                title_lay.setVisibility(View.VISIBLE);
                title_control_lay.setVisibility(View.VISIBLE);
            }else{
                isLocked = true;
                lockbtn.setImageResource(R.mipmap.superplayer_ic_player_lock);
                rplayer_ll_bottom.setVisibility(View.INVISIBLE);
                title_lay.setVisibility(View.INVISIBLE);
                title_control_lay.setVisibility(View.INVISIBLE);
            }
        });
    }

    public void updatePlayState(SuperPlayerDef.PlayerState playState) {
        switch (playState) {
            case PLAYING:
                imageView.setImageResource(R.mipmap.superplayer_ic_vod_pause_normal);
                toggleView(mPbLiveLoading, false);
                toggleView(mLayoutReplay, false);
                break;
            case LOADING:
                imageView.setImageResource(R.mipmap.superplayer_ic_vod_pause_normal);
                toggleView(mPbLiveLoading, true);
                toggleView(mLayoutReplay, false);
                break;
            case PAUSE:
                imageView.setImageResource(R.mipmap.superplayer_ic_vod_play_normal);
                toggleView(mLayoutReplay, false);
                break;
            case END:
                imageView.setImageResource(R.mipmap.superplayer_ic_vod_play_normal);
                toggleView(mLayoutReplay, true);
                break;
        }
        mCurrentPlayState = playState;
    }


    /**
     * 设置控件的可见性
     *
     * @param view      目标控件
     * @param isVisible 显示：true 隐藏：false
     */
    protected void toggleView(View view, boolean isVisible) {
        view.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }
}
