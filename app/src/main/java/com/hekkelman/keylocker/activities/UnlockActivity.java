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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.hekkelman.keylocker.KeyLockerApp;
import com.hekkelman.keylocker.R;
import com.hekkelman.keylocker.databinding.ActivityUnlockBinding;
import com.hekkelman.keylocker.datamodel.KeyLockerFile;
import com.hekkelman.keylocker.tasks.TaskResult;
import com.hekkelman.keylocker.tasks.UnlockTask;
import com.hekkelman.keylocker.utilities.AppContainer;
import com.hekkelman.keylocker.utilities.Settings;

import java.util.Objects;

/**
 * A login screen that offers login via pin/password.
 */
public class UnlockActivity extends AppCompatActivity
		implements EditText.OnEditorActionListener, View.OnClickListener {

	public final static int SHOW_RESET_AT_RETRY_COUNT	= 3;
	public final static int RESET_KEY_LOCKER_FILE_RESULT = 13;

	private UnlockTask unlockTask;

	// UI references.
	private Settings mSettings;
	private AppContainer mAppContainer;
	private TextInputLayout mPasswordLayout;
	private TextInputEditText mPasswordInput;
	private SwitchCompat mPINSwitch;
	private Button mUnlockButton;
	private Button mResetButton;
	private int mRetryCount = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.mSettings = new Settings(this);

		super.onCreate(savedInstanceState);

		mAppContainer = ((KeyLockerApp) getApplication()).mAppContainer;
		this.unlockTask = new UnlockTask(mAppContainer.executorService, mAppContainer.mainThreadHandler);

		setTitle(R.string.activity_unlock_title);

		ActivityUnlockBinding binding = ActivityUnlockBinding.inflate(getLayoutInflater());
		View view = binding.getRoot();

		setContentView(view);

		Toolbar toolbar = binding.containerToolbar;
		toolbar.setNavigationIcon(null);
		setSupportActionBar(toolbar);

		initPasswordViews(binding);
	}

	private void initPasswordViews(ActivityUnlockBinding binding) {
		mPINSwitch = binding.numericCheckBox;
		mPasswordLayout = binding.passwordLayout;
		mPasswordInput = binding.password;

		initPasswordPinSwitch();
		initPasswordLayoutView();
		initPasswordInputView();
		initButtonViews(binding);
	}

	private void initPasswordPinSwitch() {
		mPINSwitch.setOnCheckedChangeListener(
				(buttonView, isChecked) -> {
					mPasswordInput.setText("");

					if (isChecked) {
						mPasswordInput.setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD | InputType.TYPE_CLASS_NUMBER);
						mPasswordLayout.setHint(getString(R.string.unlock_hint_pin));
					} else {
						mPasswordInput.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
						mPasswordLayout.setHint(getString(R.string.unlock_hint_password));
					}
				}
		);

		boolean usePin = mSettings.getUnlockKeyboard() == Settings.UnlockKeyboardMode.DIGITS;
		mPINSwitch.setChecked(usePin);
	}

	private void initPasswordLayoutView() {
		int hintResId = (mPINSwitch.isChecked()) ? R.string.unlock_hint_pin :  R.string.unlock_hint_password;
		mPasswordLayout.setHint(getString(hintResId));
		if (mSettings.getBlockAccessibility()) {
			mPasswordLayout.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
		}
		if (mSettings.getBlockAutofill()) {
			mPasswordLayout.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS);
		}
	}

	private void initPasswordInputView() {
		int inputType = (mPINSwitch.isChecked())
				? (InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD)
				: (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		mPasswordInput.setInputType(inputType);
		mPasswordInput.setTransformationMethod(new PasswordTransformationMethod());
		mPasswordInput.setOnEditorActionListener(this);
	}

	private void initButtonViews(ActivityUnlockBinding binding) {
		mUnlockButton = binding.signInButton;
		mUnlockButton.setOnClickListener(this);

		mResetButton = binding.replaceLocker;
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
		finishWithReset();
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
		String password = Objects.requireNonNull(mPasswordInput.getText()).toString();

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
			unlockTask.unlock(mAppContainer, password, result -> {
				if (result instanceof TaskResult.Success) {
					mAppContainer.locked.setValue(false);
					finishWithResult(true);
				} else {
					mPasswordInput.setText("");
					if (++mRetryCount >= SHOW_RESET_AT_RETRY_COUNT) {
						mResetButton.setVisibility(View.VISIBLE);
					}

					mPasswordInput.setError(getString(R.string.error_incorrect_password));
					mPasswordInput.requestFocus();
				}
			});
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

	private void finishWithReset() {
		Intent data = new Intent();
		setResult(RESET_KEY_LOCKER_FILE_RESULT, data);
		finish();
	}

	private void finishWithResult(boolean success) {
		Intent data = new Intent();
		if (success)
			setResult(RESULT_OK, data);
		finish();
	}
}

