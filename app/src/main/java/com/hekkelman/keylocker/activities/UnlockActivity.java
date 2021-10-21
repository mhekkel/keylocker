package com.hekkelman.keylocker.activities;

import android.app.AlertDialog;
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

import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.hekkelman.keylocker.R;
import com.hekkelman.keylocker.datamodel.KeyDb;
import com.hekkelman.keylocker.tasks.UnlockTask;
import com.hekkelman.keylocker.tasks.UnlockTask.Result;

import java.io.File;

/**
 * A login screen that offers login via pin/password.
 */
public class UnlockActivity extends BackgroundTaskActivity<UnlockTask.Result>
		implements EditText.OnEditorActionListener, View.OnClickListener {

	public final static String EXTRA_AUTH_PASSWORD_KEY	= "password_key";
	public final static int SHOW_RESET_AT_RETRY_COUNT	= 3;

	// UI references.
	private TextInputLayout mPasswordLayout;
	private TextInputEditText mPasswordInput;
	private SwitchCompat mPINSwitch;
	private Button mUnlockButton;
	private File mKeyFile;
	private Button mResetButton;
	private int mRetryCount = 0;

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
		boolean usePin = settings.getUsePin();

		mPINSwitch.setOnCheckedChangeListener(
			new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					mPasswordInput.setText("");

					if (isChecked) {
						mPasswordInput.setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD | InputType.TYPE_CLASS_NUMBER);
						mPasswordLayout.setHint(getString(R.string.unlock_hint_pin));
					} else {
						mPasswordInput.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
						mPasswordLayout.setHint(getString(R.string.unlock_hint_password));
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
		if (settings.getBlockAccessibility()) {
			mPasswordLayout.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
		}
		if (settings.getBlockAutofill()) {
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
					.setPositiveButton(android.R.string.ok, (dialog, which) -> resetLocker())
					.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
						// do nothing
					})
					.setIcon(android.R.drawable.ic_dialog_alert)
					.show();
		}
	}

	private void resetLocker() {
		if (mKeyFile.exists())
			mKeyFile.delete();
		finishWithResult(false, null);
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
			UnlockTask task = new UnlockTask(mKeyFile, password);
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

//	@Override
//	protected void onStop() {
//		super.onStop();
//		KeyDb.release();
//	}
//
	@Override
	void onTaskResult(Result result) {
		if (result.encryptionKey != null)
			finishWithResult(true, result.encryptionKey);
		else {
			mPasswordInput.setText("");
			if (++mRetryCount >= SHOW_RESET_AT_RETRY_COUNT) {
				mResetButton.setVisibility(View.VISIBLE);
			}

			mPasswordInput.setError(getString(R.string.error_incorrect_password));
			mPasswordInput.requestFocus();
		}
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

