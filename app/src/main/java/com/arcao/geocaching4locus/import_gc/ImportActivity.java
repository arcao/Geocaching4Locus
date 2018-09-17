package com.arcao.geocaching4locus.import_gc;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;

import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.base.util.AnalyticsUtil;
import com.arcao.geocaching4locus.base.util.LocusMapUtil;
import com.arcao.geocaching4locus.base.util.PermissionUtil;
import com.arcao.geocaching4locus.error.fragment.NoExternalStoragePermissionErrorDialogFragment;
import com.arcao.geocaching4locus.import_gc.fragment.ImportDialogFragment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

public class ImportActivity extends AppCompatActivity implements ImportDialogFragment.DialogListener {
    public final static Pattern CACHE_CODE_PATTERN = Pattern.compile("(GC[A-HJKMNPQRTV-Z0-9]+)", Pattern.CASE_INSENSITIVE);
    private final static Pattern GUID_PATTERN = Pattern.compile("guid=([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})", Pattern.CASE_INSENSITIVE);

    private static final int REQUEST_SIGN_ON = 1;

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
            showImportDialog();
    }

    private void showImportDialog() {
        if (getIntent().getDataString() == null) {
            Timber.e("Data uri is null!!!");
            finish();
            return;
        }

        String url = getIntent().getDataString();

        Matcher m = CACHE_CODE_PATTERN.matcher(url);
        if (!m.find()) {
            m = GUID_PATTERN.matcher(url);
            if (!m.find()) {
                Timber.e("Cache code / guid not found in url: %s", url);
                Toast.makeText(this, "Cache code or GUID isn't found in URL: " + url, Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }

        String cacheId = m.group(1);

        AnalyticsUtil.actionImport(App.get(this).getAccountManager().isPremium());

        Timber.i("source: import;%s", cacheId);
        ImportDialogFragment.newInstance(new String[]{cacheId}).show(getFragmentManager(), ImportDialogFragment.FRAGMENT_TAG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // restart update process after log in
        if (requestCode == REQUEST_SIGN_ON) {
            if (resultCode == RESULT_OK) {
                if (PermissionUtil.requestExternalStoragePermission(this))
                    showImportDialog();
            } else {
                onImportFinished(null);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionUtil.REQUEST_EXTERNAL_STORAGE_PERMISSION) {
            if (PermissionUtil.verifyPermissions(grantResults)) {
                showImportDialog();
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
        setResult(RESULT_CANCELED);
        startActivity(intent);
        finish();
    }
}
