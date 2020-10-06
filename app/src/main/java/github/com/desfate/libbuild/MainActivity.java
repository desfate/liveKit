package github.com.desfate.libbuild;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import github.com.desfate.livekit.LivePushView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LivePushView view = new LivePushView(this);
        setContentView(R.layout.activity_main);
    }
}