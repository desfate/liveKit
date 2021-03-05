package github.com.desfate.livekit.dual;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 工具类
 * 
 * @author zgl
 * 
 */
public class Utils {
	public static final String TAG = "Sight";

	/** 打印log Error */
	public static void showLogError(String text) {
		Log.e(TAG, text);
	}

	/** 打印log Debug */
	public static void showLogDebug(String text) {
		Log.d(TAG, text);
	}

	/** 弹Toast提示框 */
	public static void showToast(Context context, String text) {
		Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
	}

	/**
	 * 创建弹出对话框
	 * 
	 * @return
	 */
	public static Dialog createDialog(Context context, View menuView) {
		Builder builder = new Builder(context);
		builder.setView(menuView);
		AlertDialog dialog = builder.create();
		dialog.show();
		// Dialog dialog = new Dialog(context);
		// dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		// dialog.setContentView(menuView);
		Window dialogWindow = dialog.getWindow();
		// dialogWindow.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.pop_background));
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		dialogWindow.setGravity(Gravity.CENTER);
		lp.width = 800;
		lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
		dialogWindow.setAttributes(lp);
		return dialog;
	}

	/**
	 * 启动其他应用activity（activity须注册非DEFAULT的intent-filter）
	 * 
	 * @param context
	 *            上下文
	 * @param packageName
	 *            目标应用的包名
	 * @param activityName
	 *            目标activity名
	 * @throws ActivityNotFoundException
	 */
	public static void runOtherApp(Context context, String packageName, String activityName) throws ActivityNotFoundException {
		ComponentName componentName = new ComponentName(packageName, activityName);
		Intent intent = new Intent();
		intent.setComponent(componentName);
		try {
			context.startActivity(intent);
		} catch (ActivityNotFoundException e) {
			showLogError("Unable to find activity class.");
			showToast(context, "Unable to find activity class.");
			e.printStackTrace();
			// throw e;
		}
	}

	/** 根据intent的uri判断该视频是否存在于列表中 */
	public static boolean isVideoInUri(Uri uri) {
		return "media".equals(uri.getAuthority());
	}

	/**
	 * 格式化视频时长显示
	 * 
	 * @param duration
	 *            毫秒
	 * @return Returns a (localized) string for the given duration (in seconds).
	 */
	public static String formatDurationShow(int duration) {
		duration = duration / 1000;
		int h = duration / 3600;
		int m = (duration - h * 3600) / 60;
		int s = duration - (h * 3600 + m * 60);
		String durationValue;
		if (h == 0) {
			durationValue = String.format("%1$02d:%2$02d", m, s);
		} else {
			durationValue = String.format("%1$d:%2$02d:%3$02d", h, m, s);
		}
		return durationValue;
	}


	/**
	 * 格式化时间显示
	 * 
	 * @param date
	 *            数字类型日期
	 * @return
	 */
	public static String formatDate(long date) {
		DateFormat formater = DateFormat.getDateTimeInstance();
		return formater.format(new Date(date));
	}

	/** 3D光栅屏开关控制操作 */
	public static void option3DGrating(Activity activity, boolean swih) {
		Utils.showLogDebug("option3DGuanshan " + swih);
	}


	/**
	 * 获取某天零点时间
	 * 
	 * @param whichDay
	 *            0为当天 -1为昨天 1为明天，依次类推
	 * @return
	 */
	public static long getMillisZero(int whichDay) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.add(Calendar.DATE, whichDay);
		return cal.getTimeInMillis();

		// Date date = new Date();
		// long l = 24*60*60*1000; //每天的毫秒数
		// //date.getTime()是现在的毫秒数，它 减去 当天零点到现在的毫秒数（
		// 现在的毫秒数%一天总的毫秒数，取余。），理论上等于零点的毫秒数，不过这个毫秒数是UTC+0时区的。
		// //减8个小时的毫秒值是为了解决时区的问题。
		// return (date.getTime() - (date.getTime()%l) - 8* 60 * 60 *1000);
	}

	/** 获取控件相对屏幕的x坐标 */
	public static int getScreenLocationX(View view) {
		int[] location = new int[2];
		view.getLocationOnScreen(location);
		int x = location[0];
		return x;
	}



	/** 保存json到文本 */
	public static void saveJson(String text, File file) {
		String str = text;
		PrintWriter pfp;
		try {
			pfp = new PrintWriter(file);
			pfp.print(str);
			pfp.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}




	/**
	 * 通过MD5加密生成MD5的数值
	 * 
	 * @param password
	 * @return
	 */
	public static String produceMD5(String password) {
		MessageDigest md;
		try {
			// 生成一个MD5加密计算摘要
			md = MessageDigest.getInstance("MD5");
			// 计算md5函数
			md.update(password.getBytes());
			// digest()最后确定返回md5 hash值，返回值为8为字符串。因为md5 hash值是16位的hex值，实际上就是8位的字符
			// BigInteger函数则将8位的字符串转换成16位hex值，用字符串来表示；得到字符串形式的hash值
			String pwd = new BigInteger(1, md.digest()).toString(16);
			return pwd;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return password;
	}

	/**
	 * 根据视频绝对路径获取content uri
	 * 
	 * @return content uri
	 */
	public static Uri getVideoContentUri(Context context, Uri filUri) {
		String filePath = filUri.getPath();
		Uri contentUri = null;
		Cursor cursor = null;
		String column = MediaStore.Video.VideoColumns.DATA;
		try {
			cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, column + "=?", new String[] { filePath }, null);
			if (cursor != null && cursor.moveToFirst()) {
				contentUri = Uri.parse(MediaStore.Video.Media.EXTERNAL_CONTENT_URI + "/" + cursor.getInt(cursor.getColumnIndex(MediaStore.Video.VideoColumns._ID)));
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return contentUri;
	}

	/**
	 * 检查当前网络是否可用
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetworkAvailable(Context context) {
		// 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (connectivityManager == null) {
			return false;
		} else {
			// 获取NetworkInfo对象
			NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();

			if (networkInfo != null && networkInfo.length > 0) {
				for (int i = 0; i < networkInfo.length; i++) {
					showLogDebug(i + "===状态===" + networkInfo[i].getState());
					showLogDebug(i + "===类型===" + networkInfo[i].getTypeName());
					// 判断当前网络状态是否为连接状态
					if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}
	

	 /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 获取屏幕高度 px
     *
     * @param context activity下的
     * @return
     */
    public static int getWindowHeight(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }

    /**
     * 获取屏幕宽度 px
     *
     * @param context activity下的
     * @return
     */
    public static int getWindowWidth(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }


	/**
	 * 获取手机型号
	 *
	 * @return  手机型号
	 */
	public static String getSystemModel() {
		return android.os.Build.MODEL;
	}



	public static int convertDipToPixels(Context context, int dip) {
		Resources resources = context.getResources();
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, resources.getDisplayMetrics());
		return (int) px;
	}
}
