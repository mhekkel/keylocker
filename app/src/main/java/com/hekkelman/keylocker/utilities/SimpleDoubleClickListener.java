package com.hekkelman.keylocker.utilities;

import android.view.View;

public abstract class SimpleDoubleClickListener implements View.OnClickListener {

    private static final long DOUBLE_CLICK_TIME_DELTA = 300; // Milliseconds

    private long lastClickTime = 0;
    private boolean firstTap = true;

    @Override
    public void onClick(View v) {
        long clickTime = System.currentTimeMillis();

        if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
            if (firstTap)
                onClick(v, true);

            firstTap = false;
        } else {
            firstTap = true;

            v.postDelayed(() -> {
                if (firstTap)
                    onClick(v, false);
            }, DOUBLE_CLICK_TIME_DELTA);
        }

        lastClickTime = clickTime;
    }

    public abstract void onClick(View v, boolean doubleClick);
}
