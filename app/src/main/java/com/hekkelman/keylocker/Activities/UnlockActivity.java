package com.hekkelman.keylocker.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewStub;
import android.view.inputmethod.EditorInfo;
import android.widget.*;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.hekkelman.keylocker.R;
import com.hekkelman.keylocker.datamodel.KeyDb;
import com.hekkelman.keylocker.Tasks.UnlockTask;
import com.hekkelman.keylocker.Tasks.UnlockTask.Result;

import java.io.File;

/**
 * A login screen that offers login via pin/password.
 */
public class UnlockActivity extends BackgroundTaskActivity<UnlockTask.Result>
		implements EditText.OnEditorActionListener, View.OnClickListener {

	public final static String EXTRA_AUTH_PASSWORD_KEY	= "password_key";
	public final static String UNLOCK_RETRY_COUNT		= "unlock_retry_count";
	public final static int SHOW_RESET_AT_RETRY_COUNT	= 3;

	// UI references.
	private TextInputLayout mPasswordLayout;
	private TextInputEditText mPasswordInput;
	private Switch mPINSwitch;
	private Button mUnlockButton;
	private File mKeyFile;
	private Button mResetButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mKeyFile = new File(getFilesDir(), KeyDb.KEY_DB_NAME);

		setTitle(R.string.activity_unlock_title);

		setContentView(R.layout.activity_container);
		initToolbar();
		initPasswordViews();
	}

	private void initToolbar() {
		Toolbar toolbar = findViewById(R.id.container_toolbar);
		toolbar.setNavigationIcon(null);
		setSupportActionBar(toolbar);
	}

	private void initPasswordViews() {
		ViewStub stub = findViewById(R.id.container_stub);
		stub.setLayoutResource(R.layout.content_unlock);
		View v = stub.inflate();

		mPINSwitch = v.findViewById(R.id.numeric_cb);
		mPasswordLayout = v.findViewById(R.id.passwordLayout);
		mPasswordInput = v.findViewById(R.id.password);

		initPasswordLabelView(v);
		initPasswordPinSwitch(v);
		initPasswordLayoutView(v);
		initPasswordInputView(v);
		initButtonViews(v);
	}

	private void initPasswordPinSwitch(View v) {
		boolean usePin = mSettings.getUsePin();

		mPINSwitch.setOnCheckedChangeListener(
			new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					mPasswordInput.setText("");

					if (isChecked) {
						mPasswordInput.setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD | InputType.TYPE_CLASS_NUMBER);
					} else {
						mPasswordInput.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
					}
				}
			}
		);

		mPINSwitch.setChecked(usePin);
	}

	private void initPasswordLabelView(View v) {
//		int labelMsg = getIntent().getIntExtra(Constants.EXTRA_AUTH_MESSAGE, R.string.auth_msg_authenticate);
//		TextView passwordLabel = v.findViewById(R.id.passwordLabel);
//		passwordLabel.setText(labelMsg);
	}

	private void initPasswordLayoutView(View v) {
		int hintResId = (mPINSwitch.isChecked()) ? R.string.unlock_hint_pin :  R.string.unlock_hint_password;
		mPasswordLayout.setHint(getString(hintResId));
		if (mSettings.getBlockAccessibility()) {
			mPasswordLayout.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
		}
		if (mSettings.getBlockAutofill()) {
			mPasswordLayout.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS);
		}
	}

	private void initPasswordInputView(View v) {
		int inputType = (mPINSwitch.isChecked())
				? (InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD)
				: (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		mPasswordInput.setInputType(inputType);
		mPasswordInput.setTransformationMethod(new PasswordTransformationMethod());
		mPasswordInput.setOnEditorActionListener(this);
	}

	private void initButtonViews(View v) {
		mUnlockButton = v.findViewById(R.id.sign_in_button);
		mUnlockButton.setOnClickListener(this);

		mResetButton = v.findViewById(R.id.replace_locker);
		mResetButton.setOnClickListener(this);

		Intent callingIntent = getIntent();
		if (callingIntent != null &&
				callingIntent.hasExtra(UNLOCK_RETRY_COUNT) &&
				callingIntent.getIntExtra(UNLOCK_RETRY_COUNT, 0) >= SHOW_RESET_AT_RETRY_COUNT) {
			resetLocker();
		}
	}

	@Override
	public void onClick(View view)
	{
		if (view == mUnlockButton)
			attemptUnlock();
		else if (view == mResetButton)
		{
			new AlertDialog.Builder(UnlockActivity.this)
					.setTitle(R.string.dlog_delete_locker_title)
					.setMessage(R.string.dlog_delete_locker_msg)
					.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							resetLocker();
						}
					})
					.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							// do nothing
						}
					})
					.setIcon(android.R.drawable.ic_dialog_alert)
					.show();
		}
	}

	private void resetLocker() {
		File keyFile = new File(getFilesDir(), KeyDb.KEY_DB_NAME);
		if (keyFile.exists()) {
			keyFile.delete();
		}

		Intent intent = new Intent(UnlockActivity.this, InitActivity.class);
		startActivity(intent);
		finish();
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	private void attemptUnlock() {
		// Reset errors.
		mPasswordInput.setError(null);

		// Store values at the time of the login attempt.
		String password = mPasswordInput.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password, if the user entered one.
		if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
			mPasswordInput.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordInput;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			UnlockTask task = new UnlockTask(this, mKeyFile, password);
			startBackgroundTask(task);
		}
	}

	private boolean isPasswordValid(String password) {
		return password.length() >= 5;
	}

	@Override
	public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
		if (id == 6543 || id == EditorInfo.IME_NULL) {
			attemptUnlock();
			return true;
		}
		return false;
	}

	@Override
	protected void onStop() {
		super.onStop();
		KeyDb.release();
	}

	@Override
	void onTaskResult(Result result) {
		finishWithResult(result.encryptionKey != null, result.encryptionKey);
	}

	private void finishWithResult(boolean success, char[] encryptionKey) {
		Intent data = new Intent();
		if (encryptionKey != null)
			data.putExtra(EXTRA_AUTH_PASSWORD_KEY, encryptionKey);
		if (success)
			setResult(RESULT_OK, data);
		finish();
	}
}

