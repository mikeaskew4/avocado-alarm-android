package com.michaelaskew.avocadotimer.utilities;

import android.app.Activity;
import android.content.IntentSender;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.install.model.AppUpdateType;

public class UpdateHelper {

    private static final int RC_APP_UPDATE = 123;
    private AppUpdateManager mAppUpdateManager;
    private Activity mActivity;

    public UpdateHelper(Activity activity) {
        mActivity = activity;
        mAppUpdateManager = AppUpdateManagerFactory.create(activity);
    }

    public void checkForUpdate() {
        mAppUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                try {
                    mAppUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            AppUpdateType.FLEXIBLE,
                            mActivity,
                            RC_APP_UPDATE);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void onResumeUpdate() {
        // Existing code...
        mAppUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                try {
                    mAppUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            AppUpdateType.FLEXIBLE,
                            mActivity,
                            RC_APP_UPDATE);

                    // Listen for the download to complete
                    mAppUpdateManager.registerListener(installState -> {
                        if (installState.installStatus() == InstallStatus.DOWNLOADED) {
                            showRestartDialog();
                        }
                    });

                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void showRestartDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity)
                .setTitle("Update Ready")
                .setMessage("Update is ready to be installed. Restart now?")
                .setPositiveButton("Restart", (dialog, which) -> {
                    mAppUpdateManager.completeUpdate();
                })
                .setNegativeButton("Later", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }


    public boolean onActivityResult(int requestCode, int resultCode) {
        if (requestCode == RC_APP_UPDATE) {
            if (resultCode != Activity.RESULT_OK) {
                Toast.makeText(mActivity, "Update flow failed! Try a different update method.", Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        return false;
    }

    public void registerListener(InstallStateUpdatedListener listener) {
        mAppUpdateManager.registerListener(listener);
    }

    public void unregisterListener(InstallStateUpdatedListener listener) {
        mAppUpdateManager.unregisterListener(listener);
    }
}