package com.arcao.geocaching4locus.fragment;

import java.lang.ref.WeakReference;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import com.arcao.geocaching4locus.R;

public class DownloadProgressDialogFragment extends CustomDialogFragment {
	private static final String TAG = DownloadProgressDialogFragment.class.getName();
	
	protected static final String PARAM_MESSAGE_ID = "MESSAGE_ID";
	protected static final String PARAM_COUNT = "COUNT";
	protected static final String PARAM_CURRENT = "CURRENT";
	
	protected WeakReference<Cancellable<DownloadProgressDialogFragment>> listenerRef;
	
	public static DownloadProgressDialogFragment newInstance(int messageId, Cancellable<DownloadProgressDialogFragment> listener) {
		return newInstance(messageId, listener, -1, -1);
	}
	
	public static DownloadProgressDialogFragment newInstance(int messageId, Cancellable<DownloadProgressDialogFragment> listener, int count, int current) {
		DownloadProgressDialogFragment frag = new DownloadProgressDialogFragment();
		frag.listenerRef = new WeakReference<Cancellable<DownloadProgressDialogFragment>>(listener);
    
		Bundle args = new Bundle();
    args.putInt(PARAM_MESSAGE_ID, messageId);
    args.putInt(PARAM_COUNT, count);
    args.putInt(PARAM_CURRENT, current);
    frag.setArguments(args);
    
    return frag;
	}
	
	public void setProgress(int progress) {
		ProgressDialog pd = (ProgressDialog) getDialog();
		if (pd != null)
			pd.setProgress(progress);
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Bundle arguments = getArguments();
		
		ProgressDialog pd = new ProgressDialog(getActivity());
		pd.setCancelable(false);
		
		if (listenerRef.get() != null) {
			pd.setButton(ProgressDialog.BUTTON_NEGATIVE, getText(R.string.cancel_button), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Cancellable<DownloadProgressDialogFragment> listener = listenerRef.get();
					if (listener != null) {
						listener.onCancel(DownloadProgressDialogFragment.this);
					}
				}
			});
		}
		
		int count = arguments.getInt(PARAM_COUNT);
		int current = arguments.getInt(PARAM_CURRENT);
		
		if (count < 0) {
			pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		} else {
			pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			pd.setMax(count);
			pd.setProgress(current);
		}
		pd.setMessage(getText(arguments.getInt(PARAM_MESSAGE_ID)));

		Log.d(TAG, "Creating ProgressDialog; count:" + count + "; current:" + current);
		
		return pd;
	}
}
