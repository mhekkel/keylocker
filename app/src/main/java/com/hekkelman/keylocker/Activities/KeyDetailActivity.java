package com.hekkelman.keylocker.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.android.material.navigation.NavigationView;
import com.hekkelman.keylocker.R;
import com.hekkelman.keylocker.datamodel.Key;
import com.hekkelman.keylocker.datamodel.KeyDb;
import com.hekkelman.keylocker.datamodel.KeyDbException;

import java.io.Serializable;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class KeyDetailActivity extends BaseActivity
		implements ActivityResultCallback<ActivityResult> {

	public static long MAX_INACTIVITY_TIME = 10 * 1000;

	private Key key;
	private boolean textChanged = false;

	protected EditText nameField;
	protected EditText userField;
	protected EditText passwordField;
	protected EditText urlField;
	protected TextView lastModified;

	private ActivityResultLauncher<Intent> unlockResult;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_key_detail);

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
			WindowManager.LayoutParams.FLAG_SECURE);

		nameField = findViewById(R.id.keyNameField);
		userField = findViewById(R.id.keyUserField);
		passwordField = findViewById(R.id.keyPasswordField);
		urlField = findViewById(R.id.keyURLField);
		lastModified = findViewById(R.id.lastModifiedCaption);

		unlockResult = registerForActivityResult(
				new ActivityResultContracts.StartActivityForResult(), result -> {
					if (result.getResultCode() != RESULT_OK)
						finish();
				});

		Intent intent = getIntent();

		String keyID = intent.getStringExtra("key-id");

		if (keyID == null) {
			lastModified.setVisibility(View.INVISIBLE);
			setKey(new Key());
		} else {
			Key key = KeyDb.getKey(keyID);
			if (key == null) {
				new AlertDialog.Builder(KeyDetailActivity.this)
					.setTitle(R.string.dlog_missing_key_title)
					.setMessage(R.string.dlog_missing_key_msg)
					.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					})
					.setIcon(android.R.drawable.ic_dialog_alert)
					.show();
				return;
			}

			setKey(key);
		}

		TextWatcher listener = new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				KeyDetailActivity.this.textChanged = true;
			}
		};

		nameField.addTextChangedListener(listener);
		passwordField.addTextChangedListener(listener);
		userField.addTextChangedListener(listener);
		urlField.addTextChangedListener(listener);
	}

	private void setKey(Key key) {
		this.key = key;

		String name = key.getName();
		if (name != null) {
			nameField.setText(name);
		}

		String password = key.getPassword();
		if (password != null) {
			passwordField.setText(password);
		}

		String user = key.getUser();
		if (user != null) {
			userField.setText(user);
		}

		String url = key.getUrl();
		if (url != null) {
			urlField.setText(url);
		}

		String lastModified = key.getTimestamp();
		if (lastModified != null) {
			this.lastModified.setText(String.format(getString(R.string.lastModifiedTemplate), lastModified));
		}
	}

	@Override
	public void onActivityResult(ActivityResult result) {
		if (result.getResultCode() == Activity.RESULT_CANCELED)
			finish();
	}

	@Override
	public void onBackPressed() {
		if (textChanged) {
			new AlertDialog.Builder(KeyDetailActivity.this)
				.setTitle(R.string.dlog_discard_changes_title)
				.setMessage(R.string.dlog_discard_changes_msg)
				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						textChanged = false; // do not store this key again, please
						finish();
					}
				})
				.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// do nothing
					}
				})
				.setIcon(android.R.drawable.ic_dialog_alert)
				.show();
		} else {
			finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.keymenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_save) {
			return saveKey();
		} else if (id == android.R.id.home) {
			onBackPressed();
			return true;
		} else
			return super.onOptionsItemSelected(item);
	}

	private boolean saveKey() {
		boolean result = false;

		String name = nameField.getText().toString();

		if (name.length() == 0) {
			nameField.setError(getString(R.string.keyNameIsRequired));
		} else {
			key.setName(name);
			key.setUser(userField.getText().toString());
			key.setPassword(passwordField.getText().toString());
			key.setUrl(urlField.getText().toString());

			try {
				KeyDb.setKey(key);
				textChanged = false;
				Toast.makeText(this, R.string.save_successful, Toast.LENGTH_SHORT).show();
				result = true;
			} catch (KeyDbException e) {
				new AlertDialog.Builder(KeyDetailActivity.this)
					.setTitle(R.string.dlog_save_failed_title)
					.setMessage(getString(R.string.dlog_save_failed_msg) + e.getMessage())
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					})
					.setIcon(android.R.drawable.ic_dialog_alert)
					.show();
			}
		}

		return result;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

//	@Override
//	protected void onPause() {
//
//		// store the data in case we're about to disappear
//		if (textChanged) {
//			String name = nameField.getText().toString();
//			String user = userField.getText().toString();
//			String password = passwordField.getText().toString();
//			String url = urlField.getText().toString();
//
//			KeyDb.storeCachedKey(keyID, name, user, password, url);
//		}
//
//		super.onPause();
//	}
//
	@Override
	public void onResume() {
		super.onResume();

		if (! KeyDb.isUnlocked()) {
			Intent authIntent = new Intent(this, UnlockActivity.class);
			unlockResult.launch(authIntent);
		}
	}

//	@Override
//	protected void onStart() {
//		super.onStart();
////		if (KeyDb.getInstance() == null) {
////			startActivity(new Intent(this, UnlockActivity.class));
////			finish();
////		} else {
////			KeyDb.reference();
////		}
//	}

	public void onClickRenewPassword(View v) {

		// get preferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(KeyDetailActivity.this);
		int length = Integer.parseInt(prefs.getString("password-length", "8"));
		boolean noAmbiguous = prefs.getBoolean("password-no-ambiguous", true);
		boolean includeCapitals = prefs.getBoolean("password-include-capitals", true);
		boolean includeDigits = prefs.getBoolean("password-include-digits", true);
		boolean includeSymbols = prefs.getBoolean("password-include-symbols", true);

		String pw = generatePassword(length, noAmbiguous, includeCapitals, includeDigits, includeSymbols);

		passwordField.setText(pw);
	}

	public void onClickVisitURL(View v) {
		String url = urlField.getText().toString();
		if (url.startsWith("http://") == false && url.startsWith("https://") == false)
			url = "http://" + url;

		try {
			Uri uri = Uri.parse(url);

			onCopyPassword(null);

			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(uri);
			startActivity(intent);
		} catch (Exception e) {
			Toast.makeText(KeyDetailActivity.this, R.string.visitFailed, Toast.LENGTH_SHORT).show();
		}
	}

//	@OnClick(R.id.fab)
	public void onCopyPassword(View view) {
		ClipboardManager clipboard = (ClipboardManager)
			getSystemService(Context.CLIPBOARD_SERVICE);

		ClipData clip = ClipData.newPlainText("password", passwordField.getText().toString());
		clipboard.setPrimaryClip(clip);
	}

	private String generatePassword(int length, boolean noAmbiguous, boolean includeCapitals, boolean includeDigits, boolean includeSymbols) {
		final String kAmbiguous = "B8G6I1l0OQDS5Z2";

		String[] vowels = getString(R.string.vowels).split(";");
		String[] consonants = getString(R.string.consonants).split(";");
		String result = "";

		Random rng = new Random();

		boolean vowel = rng.nextBoolean();
		boolean wasVowel = false, hasDigits = false, hasSymbols = false, hasCapitals = false;

		for (; ; ) {
			if (result.length() >= length) {
				if (result.length() > length ||
					includeDigits != hasDigits ||
					includeSymbols != hasSymbols ||
					includeCapitals != hasCapitals) {
					result = "";
					hasDigits = hasSymbols = hasCapitals = false;
					continue;
				}

				break;
			}

			String s;
			if (vowel) {
				do
					s = vowels[rng.nextInt(vowels.length)];
				while (wasVowel && s.length() > 1);
			} else
				s = consonants[rng.nextInt(consonants.length)];

			if (s.length() + result.length() > length)
				continue;

			if (noAmbiguous && kAmbiguous.contains(s))
				continue;

			if (includeCapitals && (result.length() == s.length() || vowel == false) && rng.nextInt(10) < 2) {
				result += s.toUpperCase(Locale.ROOT);
				hasCapitals = true;
			} else
				result += s;

			if (vowel && (wasVowel || s.length() > 1 || rng.nextInt(10) > 3))
				vowel = false;
			else
				vowel = true;

			if (hasDigits == false && includeDigits && rng.nextInt(10) < 3) {
				String ch;
				do ch = Character.valueOf((char) (rng.nextInt(10) + '0')).toString();
				while (noAmbiguous && kAmbiguous.contains(ch));

				result += ch;
				hasDigits = true;
			} else if (hasSymbols == false && includeSymbols && rng.nextInt(10) < 2) {
				char[] kSymbols =
					{
						'!', '"', '#', '$', '%', '&', '\'', '(', ')', '*', '+',
						',', '-', '.', '/', ':', ';', '<', '=', '>', '?', '@',
						'[', '\\', ']', '^', '_', '`', '{', '|', '}', '~',
					};

				result += kSymbols[rng.nextInt(kSymbols.length)];
				hasSymbols = true;
			}
		}

		return result;
	}

}
