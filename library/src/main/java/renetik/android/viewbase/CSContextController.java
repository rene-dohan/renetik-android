package renetik.android.viewbase;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.IBinder;
import android.util.Base64;
import android.view.Display;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import renetik.android.CSApplicationKt;
import renetik.android.java.collections.CSList;

import static android.content.pm.PackageManager.GET_SIGNATURES;
import static android.text.format.DateFormat.getDateFormat;
import static android.text.format.DateFormat.getTimeFormat;
import static renetik.android.java.collections.CSListKt.list;
import static renetik.android.lang.CSLang.close;
import static renetik.android.lang.CSLang.empty;
import static renetik.android.lang.CSLang.error;
import static renetik.android.lang.CSLang.is;
import static renetik.android.lang.CSLang.set;
import static renetik.android.lang.CSLang.warn;

public abstract class CSContextController extends ContextWrapper {

    public CSContextController() {
        super(CSApplicationKt.application);
    }

    public CSContextController(Context context) {
        super(context);
    }

    public Context context() {
        return this;
    }

    public void unregisterReceiver(BroadcastReceiver receiver) {
        try {
            super.unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {
            warn(e);
        }
    }

    public String getVersionString() {
        return getPackageInfo().versionCode + "-" + getPackageInfo().versionName;
    }

    public String getAppKeyHash() {
        try {
            PackageInfo info = context().getPackageManager().getPackageInfo(context().getPackageName(), GET_SIGNATURES);
            if (set(info.signatures)) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(info.signatures[0].toByteArray());
                return Base64.encodeToString(md.digest(), Base64.DEFAULT);
            }
        } catch (NameNotFoundException | NoSuchAlgorithmException e) {
            error(e);
        }
        return "";
    }

    public PackageInfo getPackageInfo() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    @NonNull
    public String stringRes(int id) {
        if (empty(id)) return "";
        return context().getResources().getString(id);
    }

    public String getStringResource(int id) {
        return getStringResource(id, "UTF-8");
    }

    public String getStringResource(int id, String encoding) {
        try {
            return new String(getResource(id, context()), encoding);
        } catch (UnsupportedEncodingException e) {
            error(e);
            return null;
        }
    }

    protected byte[] getResource(int id, Context context) {
        try {
            Resources resources = context.getResources();
            InputStream is = resources.openRawResource(id);
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            byte[] readBuffer = new byte[4 * 1024];
            try {
                int read;
                do {
                    read = is.read(readBuffer, 0, readBuffer.length);
                    if (read == -1) break;
                    bout.write(readBuffer, 0, read);
                } while (true);
                return bout.toByteArray();
            } finally {
                close(is);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int dimension(int id) {
        return (int) context().getResources().getDimension(id);
    }

    public boolean isNetworkConnected() {
        NetworkInfo info = service(Context.CONNECTIVITY_SERVICE, ConnectivityManager.class).getActiveNetworkInfo();
        return is(info) && info.isConnected();
    }

    public Bitmap loadBitmap(int id) {
        if (empty(id)) return null;
        Drawable drawable = getDrawable(id);
        return ((BitmapDrawable) drawable).getBitmap();
    }

//    public Drawable getDrawable(int id) {
//        return ContextCompat.getDrawable(context(), id);
//    }

    protected float getBatteryPercent() {
        Intent batteryStatus = context().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        return level / (float) scale;
    }

    public int color(int color) {
        return ContextCompat.getColor(context(), color);
    }

    public <T> T service(String serviceName, Class<T> serviceClass) {
        return (T) context().getSystemService(serviceName);
    }

    public void hideKeyboard(IBinder windowToken) {
        InputMethodManager service = service(Context.INPUT_METHOD_SERVICE, InputMethodManager.class);
        service.hideSoftInputFromWindow(windowToken, 0);
    }

    public Display getDefaultDisplay() {
        return service(Context.WINDOW_SERVICE, WindowManager.class).getDefaultDisplay();
    }

    public int getStatusBarHeight() {
        int resource = context().getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resource > 0) {
            return context().getResources().getDimensionPixelSize(resource);
        }
        return 0;
    }

    protected Date timeFormatParse(String text) {
        try {
            return getTimeFormat(context()).parse(text);
        } catch (ParseException e) {
            warn(e);
        }
        return null;
    }

    protected Date dateFormatParse(String text) {
        try {
            return getDateFormat(context()).parse(text);
        } catch (ParseException e) {
            warn(e);
        }
        return null;
    }

    protected String dateFormat(Date date) {
        if (is(date)) return getDateFormat(context()).format(date);
        return null;
    }

    protected String timeFormat(Date date) {
        if (is(date)) return getTimeFormat(context()).format(date);
        return null;
    }

    protected CSList<String> getStringList(int id) {
        if (empty(id)) return list();
        return list(context().getResources().getStringArray(id));
    }

    protected CSList<Integer> getIntList(int id) {
        if (empty(id)) return list();
        int[] intArray = context().getResources().getIntArray(id);
        CSList<Integer> list = list(intArray.length);
        for (Integer integer : intArray) list.add(integer);
        return list;
    }

    public InputStream openInputStream(Uri uri) {
        InputStream inputStream = null;
        try {
            inputStream = context().getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            error(e);
        }
        return inputStream;
    }

    protected boolean isServiceRunning(Class<? extends Service> serviceClass) {
        for (RunningServiceInfo service : service(Context.ACTIVITY_SERVICE, ActivityManager.class).getRunningServices(Integer.MAX_VALUE))
            if (serviceClass.getName().equals(service.service.getClassName())) return true;
        return false;
    }

    protected void startService(Class<? extends Service> serviceClass) {
        startService(new Intent(context(), serviceClass));
    }

    protected void stopService(Class<? extends Service> serviceClass) {
        stopService(new Intent(context(), serviceClass));
    }

    protected void onDestroy() {
    }

    public boolean isPortrait() {
        return context().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    public boolean isLandscape() {
        return !isPortrait();
    }

}