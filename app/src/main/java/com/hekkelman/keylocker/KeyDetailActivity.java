package com.hekkelman.keylocker;

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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.hekkelman.keylocker.datamodel.Key;
import com.hekkelman.keylocker.datamodel.KeyDb;
import com.hekkelman.keylocker.datamodel.KeyDbException;

import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class KeyDetailActivity extends AppCompatActivity {

	public static long MAX_INACTIVITY_TIME = 10 * 1000;

	private KeyLockerApplication app = KeyLockerApplication.getInstance();

	private class EditableKey extends Key {
		private boolean modified = false;

		public EditableKey() {
		}

		public EditableKey(Key key) {
			super(key);
		}

		@Override
		public void setName(String name) {
			super.setName(name);
			modified = true;
		}

		@Override
		public void setUser(String user) {
			super.setUser(user);
			modified = true;
		}

		@Override
		public void setPassword(String password) {
			super.setPassword(password);
			modified = true;
		}

		@Override
		public void setUrl(String url) {
			super.setUrl(url);
			modified = true;
		}

		public boolean isModified() {
			return modified;
		}

		public void setModified(boolean modified) {
			this.modified = modified;
		}
	}

	private EditableKey key;
	private long pausedAt = 0;

	@Bind(R.id.keyNameField)
	protected EditText nameField;
	@Bind(R.id.keyUserField)
	protected EditText userField;
	@Bind(R.id.keyPasswordField)
	protected EditText passwordField;
	@Bind(R.id.keyURLField)
	protected EditText urlField;
	@Bind(R.id.lastModifiedCaption)
	protected TextView lastModified;

	private enum KeyField
	{
		NAME("key name"),
		USER("user name"),
		PASSWORD("password"),
		URL("url");

		KeyField(String name)
		{
			this.name = name;
		}

		private final String name;
	}

	private class KeyFieldTextWatcher implements TextWatcher
	{
		private final Key key;
		private final KeyField field;

		private KeyFieldTextWatcher(Key key, KeyField field) {
			this.key = key;
			this.field = field;
		}

		@Override
		public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

		}

		@Override
		public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

		}

		@Override
		public void afterTextChanged(Editable editable) {
			switch (field)
			{
				case NAME: key.setName(editable.toString()); break;
				case USER: key.setUser(editable.toString()); break;
				case PASSWORD: key.setPassword(editable.toString()); break;
				case URL: key.setUrl(editable.toString());
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_key_detail);

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
			WindowManager.LayoutParams.FLAG_SECURE);

		ButterKnife.bind(this);

        pausedAt = new Date().getTime();

		String keyID = getIntent().getStringExtra("keyId");

		if (keyID == null) {
			this.key = new EditableKey();
			lastModified.setVisibility(View.INVISIBLE);
		} else {
			Key key = app.loadKey(keyID);
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
		}

		setKey(key);

		nameField.addTextChangedListener(new KeyFieldTextWatcher(key, KeyField.NAME));
		passwordField.addTextChangedListener(new KeyFieldTextWatcher(key, KeyField.PASSWORD));
		userField.addTextChangedListener(new KeyFieldTextWatcher(key, KeyField.USER));
		urlField.addTextChangedListener(new KeyFieldTextWatcher(key, KeyField.URL));
	}

	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putByteArray("key-data", app.encryptKey(this.key));

		nameField.setText("");
		passwordField.setText("");
		userField.setText("");
		urlField.setText("");
		lastModified.setText("");

		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		// Restore the key from the bundle
		try {
			byte[] data = savedInstanceState.getByteArray("key-data");
			Key savedKey = app.decryptKey(data);
			if (savedKey != null)
				setKey(new EditableKey(savedKey));
		} catch (KeyDbException e) {
			e.printStackTrace();
		}
	}

	private void setKey(EditableKey key) {
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
	public void onBackPressed() {
		if (key.isModified() == false) {
			finish();
		} else {
			new AlertDialog.Builder(KeyDetailActivity.this)
				.setTitle(R.string.dlog_discard_changes_title)
				.setMessage(R.string.dlog_discard_changes_msg)
				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						key.setModified(false); // do not store this key again, please
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

			key.setName(userField.getText().toString());
			key.setPassword(passwordField.getText().toString());
			key.setUrl(urlField.getText().toString());

			try {
				app.saveKey(key);
				key.setModified(false);

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
	protected void onPause() {
		pausedAt = new Date().getTime();

		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (new Date().getTime() > pausedAt + MAX_INACTIVITY_TIME) {
			startActivity(new Intent(this, UnlockActivity.class));
			finish();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

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

	@OnClick(R.id.fab)
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
