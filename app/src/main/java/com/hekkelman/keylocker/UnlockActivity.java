package com.hekkelman.keylocker;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.hekkelman.keylocker.datamodel.InvalidPasswordException;
import com.hekkelman.keylocker.datamodel.KeyDb;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * A login screen that offers login via email/password.
 */
public class UnlockActivity extends AppCompatActivity {

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;

	// UI references.
	private EditText mPasswordView;
	private View mProgressView;
	private View mLoginFormView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		// First see if this is the first run

		File keyFile = new File(getFilesDir(), KeyDb.KEY_DB_NAME);
		if (keyFile.exists() == false) {
			Intent intent = new Intent(UnlockActivity.this, InitActivity.class);
			startActivity(intent);
			finish();
			return;
		}

		// Set up the login form.
		KeyDb.setInstance(null);

		mPasswordView = findViewById(R.id.password);
		mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
				if (id == 6543 || id == EditorInfo.IME_NULL) {
					attemptLogin();
					return true;
				}
				return false;
			}
		});

		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		boolean numericPassword = prefs.getBoolean("numeric-password", true);

		final Switch sw = findViewById(R.id.numeric_cb);
		sw.setOnCheckedChangeListener(
			new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					mPasswordView.setText("");

					if (isChecked) {
						mPasswordView.setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD | InputType.TYPE_CLASS_NUMBER);
					} else {
						mPasswordView.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
					}

					prefs.edit().putBoolean("numeric-password", isChecked).apply();
				}
			}
		);

		sw.setChecked(numericPassword);

		Button signInButton = findViewById(R.id.sign_in_button);
		signInButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				attemptLogin();
			}
		});

		Button resetButton = findViewById(R.id.replace_locker);
		resetButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

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
		});

		mLoginFormView = findViewById(R.id.login_form);
		mProgressView = findViewById(R.id.login_progress);
	}

	private void resetLocker() {
		if (mAuthTask != null) {
			return;
		}

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
	private void attemptLogin() {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		String password = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password, if the user entered one.
		if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			showProgress(true);
			mAuthTask = new UserLoginTask(password, this);
			mAuthTask.execute((Void) null);
		}
	}

	private boolean isPasswordValid(String password) {
		return password.length() >= 5;
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
            show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
            show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

	enum UnlockResult {VALID, INVALID_FILE, INVALID_PASSWORD, ACTIVITY_GONE}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public static class UserLoginTask extends AsyncTask<Void, Void, UnlockResult> {

		private final String mPassword;
		private final WeakReference<UnlockActivity> unlockActivityWeakReference;

		UserLoginTask(String password, UnlockActivity unlockActivity) {
			mPassword = password;
            this.unlockActivityWeakReference = new WeakReference<>(unlockActivity);
        }

		@Override
		protected UnlockResult doInBackground(Void... params) {
			try {
                UnlockActivity unlockActivity = unlockActivityWeakReference.get();
                if (unlockActivity == null)
                    return UnlockResult.ACTIVITY_GONE;

                KeyDb keyDb = new KeyDb(mPassword.toCharArray(),
                    new File(unlockActivity.getFilesDir(), KeyDb.KEY_DB_NAME));

				KeyDb.setInstance(keyDb);

				return UnlockResult.VALID;
			} catch (InvalidPasswordException ex) {
				return UnlockResult.INVALID_PASSWORD;
			} catch (Exception ex) {
				return UnlockResult.INVALID_FILE;
			}
		}

		@Override
		protected void onPostExecute(final UnlockResult result) {
            UnlockActivity unlockActivity = unlockActivityWeakReference.get();
            if (unlockActivity != null)
                unlockActivity.taskFinished(result);
		}

		@Override
		protected void onCancelled() {
            UnlockActivity unlockActivity = unlockActivityWeakReference.get();
            if (unlockActivity != null)
                unlockActivity.taskCancelled();
		}
	}

    protected void taskFinished(final UnlockResult result) {
        mAuthTask = null;
        showProgress(false);

        switch (result) {
            case INVALID_FILE:
                Toast.makeText(UnlockActivity.this, R.string.toast_corrupt_file, Toast.LENGTH_LONG).show();
                break;

            case INVALID_PASSWORD:
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();

                Button resetButton = (Button) findViewById(R.id.replace_locker);
                resetButton.setVisibility(View.VISIBLE);
                break;

            case VALID:
                Intent intent = new Intent(UnlockActivity.this, MainActivity.class);
                intent.putExtra("unlocked", true);
                startActivity(intent);
                finish();
                break;
        }
    }

    protected void taskCancelled() {
        mAuthTask = null;
        showProgress(false);
    }

	@Override
	protected void onStop() {
		super.onStop();

		KeyDb.release();
	}
}

