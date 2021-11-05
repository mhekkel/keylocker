package com.hekkelman.keylocker.utilities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.View;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.hekkelman.keylocker.R;

public class Tools {
    public static void copyToClipboard(Context context, View view, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(context.getString(R.string.label_clipboard_content), text);
        clipboard.setPrimaryClip(clip);

        Snackbar.make(view, R.string.toast_copied_to_clipboard, BaseTransientBottomBar.LENGTH_SHORT).show();
    }
}
