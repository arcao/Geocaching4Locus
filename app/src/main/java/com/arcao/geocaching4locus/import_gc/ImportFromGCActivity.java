package com.arcao.geocaching4locus.import_gc;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.base.util.AnalyticsUtil;
import com.arcao.geocaching4locus.base.util.LocusMapUtil;
import com.arcao.geocaching4locus.base.util.PermissionUtil;
import com.arcao.geocaching4locus.error.fragment.NoExternalStoragePermissionErrorDialogFragment;
import com.arcao.geocaching4locus.import_gc.fragment.GCNumberInputDialogFragment;
import com.arcao.geocaching4locus.import_gc.fragment.ImportDialogFragment;

import java.util.Arrays;

import timber.log.Timber;

public class ImportFromGCActivity extends AppCompatActivity implements ImportDialogFragment.DialogListener, GCNumberInputDialogFragment.DialogListener {
    private static final int REQUEST_SIGN_ON = 1;
    private static final int REQUEST_IMPORT_ERROR = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (LocusMapUtil.isLocusNotInstalled(this)) {
            LocusMapUtil.showLocusMissingError(this);
            return;
        }

        // test if user is logged in
        if (App.get(this).getAccountManager().requestSignOn(this, REQUEST_SIGN_ON)) {
            return;
        }

        if (savedInstanceState != null)
            return;

        if (PermissionUtil.requestExternalStoragePermission(this))
            showGCNumberInputDialog();
    }

    private void showGCNumberInputDialog() {
        GCNumberInputDialogFragment.newInstance().show(getFragmentManager(), GCNumberInputDialogFragment.FRAGMENT_TAG);
    }

    private void startImport(String[] cacheIds) {
        Timber.i("source: importFromGC;%s", Arrays.toString(cacheIds));

        AnalyticsUtil.actionImportGC(App.get(this).getAccountManager().isPremium());

        ImportDialogFragment.newInstance(cacheIds).show(getFragmentManager(), ImportDialogFragment.FRAGMENT_TAG);
    }

    @Override
    public void onInputFinished(@NonNull String[] input) {
        if (input.length > 0) {
            startImport(input);
        } else {
            onImportFinished(null);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // restart update process after log in
        if (requestCode == REQUEST_SIGN_ON) {
            if (resultCode == RESULT_OK) {
                if (PermissionUtil.requestExternalStoragePermission(this))
                    showGCNumberInputDialog();
            } else {
                onImportFinished(null);
            }
        } else if (requestCode == REQUEST_IMPORT_ERROR) {
            setResult(resultCode);
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionUtil.REQUEST_EXTERNAL_STORAGE_PERMISSION) {
            if (PermissionUtil.verifyPermissions(grantResults)) {
                showGCNumberInputDialog();
            } else {
                NoExternalStoragePermissionErrorDialogFragment.newInstance(true).show(getFragmentManager(), NoExternalStoragePermissionErrorDialogFragment.FRAGMENT_TAG);
            }
        }
    }


    @Override
    public void onImportFinished(@Nullable Intent intent) {
        Timber.d("onImportFinished result: %b", (intent != null));
        setResult(intent != null ? RESULT_OK : RESULT_CANCELED);

        if (intent != null) {
            startActivity(intent);
        }
        finish();
    }

    @Override
    public void onImportError(@NonNull Intent intent) {
        Timber.d("onImportError called");
        startActivityForResult(intent, REQUEST_IMPORT_ERROR);
    }


}
