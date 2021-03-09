package github.com.desfate.libbuild;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.tencent.rtmp.ITXLivePlayListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePlayConfig;
import com.tencent.rtmp.TXLivePlayer;

import github.com.desfate.livekit.ui.DualLivePlayView;
import github.com.desfate.livekit.ui.LivePlayView;

/**
 * 播放测试类
 */
public class PlayActivity extends AppCompatActivity {

    protected TXLivePlayer mTXLivePlayer;

    protected TXLivePlayConfig mTXLivePlayConfig;

    private DualLivePlayView anchor_play_view;

//    private LivePlayView anchor_play_view;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        anchor_play_view = findViewById(R.id.anchor_play_view);
        anchor_play_view.setFrontChange(false);
        mTXLivePlayConfig = new TXLivePlayConfig();
        mTXLivePlayer = new TXLivePlayer(this);
        mTXLivePlayConfig.setAutoAdjustCacheTime(true);
        mTXLivePlayConfig.setMaxAutoAdjustCacheTime(2.0f);
        mTXLivePlayConfig.setMinAutoAdjustCacheTime(2.0f);
        mTXLivePlayer.setConfig(mTXLivePlayConfig);
        mTXLivePlayer.setPlayListener(new ITXLivePlayListener() {
            @Override
            public void onPlayEvent(final int event, final Bundle param) {
                if (event == TXLiveConstants.PLAY_ERR_NET_DISCONNECT) {
                    String msg = "[LivePlayer] 拉流失败[" + param.getString(TXLiveConstants.EVT_DESCRIPTION) + "]";
                } else if (event == TXLiveConstants.PLAY_EVT_CHANGE_RESOLUTION) {
                    int width = param.getInt(TXLiveConstants.EVT_PARAM1, 0);
                    int height = param.getInt(TXLiveConstants.EVT_PARAM2, 0);
                    // 这里可以获得 拿到的视频大小
                    // 视频推流过来时的 视频宽高  这里要根据视频宽高生成对应的显示页面
                    // fixme：这里记录拿到的视频宽高
//                    System.out.println("@@@ width = " + width + "  height = " + height );
                }

            }

            @Override
            public void onNetStatus(Bundle status) {

            }
        });
        mTXLivePlayer.setPlayerView(null);
        mTXLivePlayer.setRenderMode(TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN);
        mTXLivePlayer.setSurface(anchor_play_view.getmSurface());  // 绑定surface
        mTXLivePlayer.setSurfaceSize(1920, 1080);
        mTXLivePlayer.startPlay(TestConfig.PLAY_URL, TXLivePlayer.PLAY_TYPE_LIVE_FLV);


    }
}
