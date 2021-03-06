package truelecter.iig.android;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import truelecter.iig.Main;
import truelecter.iig.screen.AudioSpectrum;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class AndroidLauncher extends AndroidApplication {

    /**
     * Hook to pause playing or not
     */
    @Override
    protected void onPause() {
        super.onPause();
        AudioSpectrum.onAndroidPause();
    }

    /**
     * Clear all our activities
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Main.getInstance().saveConfig();
        int pid = android.os.Process.myPid();
        android.os.Process.killProcess(pid);
    }

    /**
     * Unpack our files to /data/data/truelecter.iig.android/files/ if needed.
     * Open file if file intent is provided.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        String firstRunString = "";
        try {
            firstRunString = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode + "";
        } catch (Exception e) {
            Log.w("AudioVZ", "Failed to get package version");
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        File firstRun = new File(getFilesDir().getAbsoluteFile() + "/FIRST_RUN_" + firstRunString);
        if (!firstRun.exists()) {
            copyFileOrDir("loading.gif", true);
            copyFileOrDir("pix.bmp", true);
            copyFileOrDir("data", false);
            try {
                firstRun.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Intent intent = getIntent();
        String action = intent.getAction();
        String scheme = intent.getScheme();
        File f = null;
        if ((Intent.ACTION_VIEW.compareTo(action) == 0) && (scheme.compareTo(ContentResolver.SCHEME_FILE) == 0)) {
            Uri uri = intent.getData();
            Log.v("AudioVZ", "File intent detected: " + action + " : " + uri.getPath());
            f = new File(uri.getPath());
        }
        initialize(new Main(this, f), config);
    }

    /**
     * Unpacks file or dir from apk
     * 
     * @param path
     *            - path to file or dir in .apk
     * @param onlyFiles
     *            - set to <b>true</b> if <b>path</b> is path of directory
     */
    public void copyFileOrDir(String path, boolean onlyFiles) {
        AssetManager assetManager = getAssets();
        String assets[] = null;
        try {
            assets = assetManager.list(path);
            Log.i("AudioVZ", "Path \"" + path + "\" has " + assets.length + " files.");
            if (assets.length == 0) {
                Log.i("AudioVZ", "Copying file " + path);
                copyFile(path);
            } else {
                if (!onlyFiles) {
                    String fullPath = getFilesDir().getAbsoluteFile() + "/" + path;
                    File dir = new File(fullPath);
                    if (!dir.exists()) {
                        dir.mkdir();
                    }
                    for (int i = 0; i < assets.length; ++i) {
                        copyFileOrDir(path + "/" + assets[i], false);
                    }
                }
            }
        } catch (IOException ex) {
            Log.e("AudioVZ", "I/O Exception");
            ex.printStackTrace();
        }
    }

    /**
     * Copy file from assets
     */
    private void copyFile(String filename) {
        AssetManager assetManager = getAssets();
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(filename);
            String newFileName = getFilesDir().getAbsoluteFile() + "/" + filename;
            out = new FileOutputStream(newFileName);
            byte[] buffer = new byte[1024];
            int read = -1;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
        } catch (Exception e) {
            Log.e("AudioVz", "Error copying file " + filename);
            e.printStackTrace();
        }
    }
}