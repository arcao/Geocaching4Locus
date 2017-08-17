package com.arcao.geocaching4locus.download_rectangle;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.base.AbstractActionBarActivity;
import com.arcao.geocaching4locus.base.util.LocusTesting;
import com.arcao.geocaching4locus.base.util.PermissionUtil;
import com.arcao.geocaching4locus.download_rectangle.fragment.DownloadRectangleDialogFragment;
import com.arcao.geocaching4locus.error.ErrorActivity;
import com.arcao.geocaching4locus.error.fragment.NoExternalStoragePermissionErrorDialogFragment;
import com.arcao.geocaching4locus.import_gc.fragment.ImportDialogFragment;
import com.arcao.geocaching4locus.live_map.model.LastLiveMapData;

import locus.api.objects.extra.Location;

public class DownloadRectangleActivity extends AbstractActionBarActivity implements
    DownloadRectangleDialogFragment.DialogListener {
  private static final int REQUEST_SIGN_ON = 1;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (!LocusTesting.isLocusInstalled(this)) {
      LocusTesting.showLocusMissingError(this);
      return;
    }

    // test if user is logged in
    if (App.get(this).getAccountManager().requestSignOn(this, REQUEST_SIGN_ON)) {
      return;
    }

    if (savedInstanceState != null)
      return;

    if (PermissionUtil.requestExternalStoragePermission(this))
      showDownloadDialog();
  }

  private void showDownloadDialog() {
    if (!LastLiveMapData.getInstance().isValid()) {
      startActivity(new ErrorActivity.IntentBuilder(this).message(R.string.error_enable_livemap_first).build());
      finish();
      return;
    }

    DownloadRectangleDialogFragment.newInstance().show(getFragmentManager(), ImportDialogFragment.FRAGMENT_TAG);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    // restart update process after log in
    if (requestCode == REQUEST_SIGN_ON) {
      if (resultCode == RESULT_OK) {
        if (PermissionUtil.requestExternalStoragePermission(this))
          showDownloadDialog();
      } else {
        onDownloadFinished(null);
      }
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    if (requestCode == PermissionUtil.REQUEST_EXTERNAL_STORAGE_PERMISSION) {
      if (PermissionUtil.verifyPermissions(grantResults)) {
        showDownloadDialog();
      } else {
        NoExternalStoragePermissionErrorDialogFragment.newInstance(true).show(getFragmentManager(), NoExternalStoragePermissionErrorDialogFragment.FRAGMENT_TAG);
      }
    }
  }

  @Override
  public void onDownloadFinished(Intent intent) {
    setResult(intent != null ? RESULT_OK : RESULT_CANCELED);
    if (intent != null) {
      startActivity(intent);
    }
    finish();
  }

  @Override
  public void onDownloadError(Intent errorIntent) {
    setResult(RESULT_CANCELED);
    if (errorIntent != null) {
      startActivity(errorIntent);
    }
    finish();
  }
}
