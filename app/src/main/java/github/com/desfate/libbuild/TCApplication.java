package github.com.desfate.libbuild;

import android.app.Application;

import com.tencent.rtmp.TXLiveBase;

//import com.tencent.rtmp.TXLiveBase;

public class TCApplication extends Application {

    public final static String LICENCE_URL =
            "http://license.vod2.myqcloud.com/license/v1/fbc229653a09ab6ec9e0d2f9d30db945/TXLiveSDK.licence";
    public final static String LICENCE_KEY =
            "7ebeb7a8f2cd241daf84cfa2e8a66191";


    @Override
    public void onCreate() {
        super.onCreate();
        TXLiveBase.getInstance().setLicence(this, LICENCE_URL, LICENCE_KEY);
    }
}
