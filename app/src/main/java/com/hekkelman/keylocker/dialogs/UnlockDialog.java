package com.hekkelman.keylocker.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.hekkelman.keylocker.KeyLockerApp;
import com.hekkelman.keylocker.R;
import com.hekkelman.keylocker.activities.UnlockActivity;
import com.hekkelman.keylocker.datamodel.KeyDbModel;
import com.hekkelman.keylocker.tasks.TaskResult;
import com.hekkelman.keylocker.tasks.UnlockTask;
import com.hekkelman.keylocker.utilities.AppContainer;
import com.hekkelman.keylocker.utilities.Settings;

import java.io.File;

public class UnlockDialog extends DialogFragment {

    public static String TAG = "UnlockDialog";
    public final static int SHOW_RESET_AT_RETRY_COUNT	= 3;
    public final static int DISMISS_AT_RETRY_COUNT      = 6;

    private Settings settings;
    private TextInputLayout mPasswordLayout;
    private TextInputEditText mPasswordInput;
    private SwitchCompat mPINSwitch;
    private Button mUnlockButton;
    private Button mResetButton;
    private int mRetryCount = 0;
    private UnlockTask unlockTask;

    public interface UnlockDialogListener {
        public void onUnlockDone(TaskResult<Void> result);
    }

    private UnlockDialogListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        KeyLockerApp app = (KeyLockerApp) getActivity().getApplication();

        AppContainer appContainer = app.appContainer;
        this.unlockTask = new UnlockTask(appContainer.getExecutorService(), appContainer.getMainThreadHandler());

        this.settings = new Settings(app);

        Dialog result = super.onCreateDialog(savedInstanceState);

        result.setCanceledOnTouchOutside(false);

        return result;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View result = inflater.inflate(R.layout.dialog_ask_password2, container, false);

        initPasswordViews(result);
//        Button button = result.findViewById(R.id.sign_in_button);
//        assert (button != null);
//        button.setOnClickListener(view -> {
//            listener.onUnlockDone(new TaskResult.Success(null));
//            dismiss();
//        });

        return result;
    }


    private void initPasswordViews(View v) {
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
        mPasswordInput.setOnEditorActionListener(this::onEditorAction);
    }

    private void initButtonViews(View v) {
        mUnlockButton = v.findViewById(R.id.sign_in_button);
        mUnlockButton.setOnClickListener(this::onClick);

        mResetButton = v.findViewById(R.id.replace_locker);
        mResetButton.setOnClickListener(this::onClick);
    }

    public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
        if (id == EditorInfo.IME_ACTION_DONE) {
            attemptUnlock();
            return true;
        }
        return false;
    }

    public void onClick(View view)
    {
        if (view == mUnlockButton)
            attemptUnlock();
        else if (view == mResetButton)
        {
            new AlertDialog.Builder(getActivity())
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
    }

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

            KeyDbModel keyDbModel = new ViewModelProvider(getActivity()).get(KeyDbModel.class);

            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            unlockTask.unlock(keyDbModel, password.toCharArray(), result -> {
                if (result instanceof TaskResult.Success) {
                    keyDbModel.loadKeys();
                    listener.onUnlockDone(new TaskResult.Success<>(null));
                    dismiss();
                } else {
                    mPasswordInput.setText("");
                    if (++mRetryCount >= SHOW_RESET_AT_RETRY_COUNT)
                        mResetButton.setVisibility(View.VISIBLE);
                    else if (mRetryCount >= DISMISS_AT_RETRY_COUNT)
                    {
                        listener.onUnlockDone(new TaskResult.Error<>(null));
                        dismiss();
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
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (UnlockDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    //    public UnlockDialog() {
//        super(R.layout.dialog_ask_password);
//    }
}
