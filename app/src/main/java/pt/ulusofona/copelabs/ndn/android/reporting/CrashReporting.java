/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2018-02-07
 * This class is used to enable crash reporting
 * @author Miguel Tavares (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.reporting;


import android.app.Application;
import android.os.Environment;

import com.balsikandar.crashreporter.CrashReporter;

import java.io.File;

public class CrashReporting extends Application {

    /** This variable is used to reference the root path where the folder will be created */
    private static final String ROOT_PATH = Environment.getExternalStorageDirectory() + File.separator;

    /** This variable contains the folder name to be created */
    private static final String NDN_CRASH_FOLDER_NAME = "NDN-OPP_Crash";

    /** This variable holds the full path plus folder name */
    private static final String NDN_FULL_PATH = ROOT_PATH + NDN_CRASH_FOLDER_NAME;

    @Override
    public void onCreate() {
        super.onCreate();
        createCrashFolder();
        initCrashReporter();
    }

    /**
     * This method is used to create the folder
     * where the crash reports will be stored
     */
    private void createCrashFolder() {
        File folder = new File(NDN_FULL_PATH);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    /**
     * This method initialize the crash report feature
     */
    private void initCrashReporter() {
        CrashReporter.initialize(this, NDN_FULL_PATH);
        CrashReporter.disableNotification();
    }
}
