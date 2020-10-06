package github.com.desfate.libbuild;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import github.com.desfate.livekit.LivePushView;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LivePushView view = new LivePushView(this);
        setContentView(R.layout.activity_main);
    }
}