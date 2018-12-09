package com.salton123.unstaller.util;

import android.os.Environment;
import android.util.Base64;

import com.salton123.log.XLog;
import com.salton123.unstaller.entity.AppInfo;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * User: newSalton@outlook.com
 * Date: 2018/12/9 5:25 PM
 * ModifyTime: 5:25 PM
 * Description:
 */
public class BackupManager {
    private static ExecutorService backThreadPool = Executors.newFixedThreadPool(8);

    private static final String backup_path = Environment.getExternalStorageDirectory() +
            File.separator + "backup" + File.separator;
    static int backupCount;

    public static void toBackup(final List<AppInfo> toBackupData, final IBackupProgress iBackup) {
        if (toBackupData == null) {
            XLog.i(BackupManager.class, "isValidData == false");
        } else {
            backupCount = 0;
            for (int i = 0; i < toBackupData.size(); i++) {
                final AppInfo item = toBackupData.get(i);
                backThreadPool.submit(new Callable<Boolean>() {
                    @Override
                    public Boolean call() {
                        try {
                            final String fromFilePath = item.path;
                            final String fileName = Base64.encodeToString(fromFilePath.getBytes(), 0);
                            String destPath = backup_path + fileName;
                            boolean result = Utils.copyFile(fromFilePath, destPath);
                            XLog.i(BackupManager.class, "[toBackup] finish task ,dest path="
                                    + destPath + ",from path=" + fromFilePath
                                    + ",decrypt=" + new String(Base64.decode(fileName, 0)));
                            if (iBackup != null) {
                                iBackup.onProgress(backupCount, toBackupData.size(), result);
                            }
                            return result;
                        } catch (Exception e) {
                            XLog.e(this, e.getMessage());
                        } finally {
                            backupCount++;
                        }
                        return false;
                    }
                });
            }
        }
    }

    public interface IBackupProgress {
        void onProgress(int current, int total, boolean isSuccess);
    }


}
