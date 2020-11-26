package github.com.desfate.libbuild;

import android.app.Application;

import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.rtmp.TXLiveBase;

//import com.tencent.rtmp.TXLiveBase;

public class TCApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        TXLiveBase.getInstance().setLicence(this, TestConfig.LICENCE_URL, TestConfig.LICENCE_KEY);
        CrashReport.initCrashReport(getApplicationContext(), "79952576b1", false);
    }
}
