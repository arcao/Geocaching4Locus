package com.arcao.geocaching4locus;

import org.acra.ErrorReporter;
import org.acra.ErrorReporterEx;
import org.acra.ReportingInteractionMode;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.arcao.geocaching4locus.authentication.AccountAuthenticator;

public class SendErrorActivity extends Activity {
	public static final String ACTION_SEND_ERROR = "com.arcao.geocaching4locus.intent.action.SEND_ERROR";
	public static final String PARAM_EXCEPTION = "EXCEPTION";

	protected EditText commentEditText;

	protected Throwable exception;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(0); // TODO

		commentEditText = (EditText) findViewById(0); // TODO

		if (ACTION_SEND_ERROR.equals(getIntent().getAction())) {
			exception = (Throwable) getIntent().getSerializableExtra(PARAM_EXCEPTION);
		} else {
			finish();
		}
	}

	public void onClickSend(View view) {
		String comment = commentEditText.getText().toString();
		
		if (AccountAuthenticator.hasAccount(this)) { 
			ErrorReporter.getInstance().putCustomData("userName", AccountAuthenticator.getAccount(this).name);
		}
		
		if (comment.length() > 0) {
			ErrorReporterEx.storeUserComment(comment);
		}
		
		ErrorReporterEx.handleException(exception, ReportingInteractionMode.TOAST);
		finish();
	}

	public void onClickCancel(View view) {
		finish();
	}
}
