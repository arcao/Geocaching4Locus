package com.arcao.geocaching4locus;

import org.acra.ACRA;
import org.acra.ErrorReporter;
import org.acra.ErrorReporterEx;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.arcao.geocaching4locus.authentication.AccountAuthenticator;

public class SendErrorActivity extends Activity {
	public static final String ACTION_SEND_ERROR = "com.arcao.geocaching4locus.intent.action.SEND_ERROR";
	public static final String PARAM_EXCEPTION = "EXCEPTION";

	private SharedPreferences prefs = null;
	private EditText userComment = null;
	private EditText userEmail = null;

	protected Throwable exception;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (ACTION_SEND_ERROR.equals(getIntent().getAction())) {
			exception = (Throwable) getIntent().getSerializableExtra(PARAM_EXCEPTION);
		} else {
			finish();
		}
		
		requestWindowFeature(Window.FEATURE_LEFT_ICON);

		LinearLayout root = new LinearLayout(this);
		root.setOrientation(LinearLayout.VERTICAL);
		root.setPadding(10, 10, 10, 10);
		root.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

		ScrollView scroll = new ScrollView(this);
		root.addView(scroll, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1.0f));

		// Add an optional prompt for user comments
		int commentPromptId = ACRA.getConfig().resDialogCommentPrompt();
		if (commentPromptId != 0) {
			TextView label = new TextView(this);
			label.setText(getText(commentPromptId));

			label.setPadding(label.getPaddingLeft(), 10, label.getPaddingRight(), label.getPaddingBottom());
			root.addView(label, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

			userComment = new EditText(this);

			userComment.setLines(2);
			root.addView(userComment,
					new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		}

		// Add an optional user email field
		int emailPromptId = ACRA.getConfig().resDialogEmailPrompt();
		if (emailPromptId != 0) {
			TextView label = new TextView(this);
			label.setText(getText(emailPromptId));

			label.setPadding(label.getPaddingLeft(), 10, label.getPaddingRight(), label.getPaddingBottom());
			root.addView(label, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

			userEmail = new EditText(this);
			userEmail.setSingleLine();
			userEmail.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

			prefs = getSharedPreferences(ACRA.getConfig().sharedPreferencesName(), ACRA.getConfig()
					.sharedPreferencesMode());
			userEmail.setText(prefs.getString(ACRA.PREF_USER_EMAIL_ADDRESS, ""));

			root.addView(userEmail, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		}

		LinearLayout buttons = new LinearLayout(this);
		buttons.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		buttons.setPadding(buttons.getPaddingLeft(), 10, buttons.getPaddingRight(), buttons.getPaddingBottom());

		Button yes = new Button(this);
		yes.setText(android.R.string.yes);
		yes.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (AccountAuthenticator.hasAccount(SendErrorActivity.this)) {
					ErrorReporter.getInstance().putCustomData("userName", AccountAuthenticator.getAccount(SendErrorActivity.this).name);
				}

				// Retrieve user comment
				if (userComment != null) {
					ErrorReporterEx.storeUserComment(userComment.getText().toString());
				}

				// Store the user email
				if (prefs != null && userEmail != null) {
					String usrEmail = userEmail.getText().toString();
					Editor prefEditor = prefs.edit();
					prefEditor.putString(ACRA.PREF_USER_EMAIL_ADDRESS, usrEmail);
					prefEditor.commit();

					ErrorReporterEx.storeUserEmail(usrEmail);
				}

				// Optional Toast to thank the user
				int toastId = ACRA.getConfig().resDialogOkToast();
				if (toastId != 0) {
					Toast.makeText(SendErrorActivity.this, toastId, Toast.LENGTH_LONG).show();
				}

				ErrorReporter.getInstance().handleSilentException(exception);
				finish();
			}
		});
		
		buttons.addView(yes, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1.0f));
		Button no = new Button(this);
		no.setText(android.R.string.no);
		no.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}

		});
		buttons.addView(no, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1.0f));
		root.addView(buttons, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

		setContentView(root);

		setTitle(R.string.error_report_error);
		
		getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, ACRA.getConfig().resDialogIcon());
	}
}
