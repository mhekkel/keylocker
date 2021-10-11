package com.hekkelman.keylocker.View;

import android.content.Context;
import android.graphics.ColorFilter;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.hekkelman.keylocker.R;
import com.hekkelman.keylocker.Utilities.Tools;

public class KeyCardHolder extends RecyclerView.ViewHolder {
    private final Context context;
    protected CardView card;
    protected TextView nameView;
    protected TextView userView;
    protected ImageButton copyButton;
    protected ImageButton menuButton;
    protected TextView passwordView;

    private Callback callback;

    public interface Callback {
        void onMoveEventStart();
        void onMoveEventStop();

        void onMenuButtonClicked(View parentView, int position);
        void onCopyButtonClicked(String text, int position);

        void onCardSingleClicked(int position, String text);
        void onCardDoubleClicked(int position, String text);
//
//        void onCounterClicked(int position);
//        void onCounterLongPressed(int position);
    }

    public KeyCardHolder(Context context, View itemView, boolean tapToReveal) {
        super(itemView);

        this.context = context;

        // Style the buttons in the current theme colors
        ColorFilter colorFilter = Tools.getThemeColorFilter(context, android.R.attr.textColorSecondary);

        menuButton.getDrawable().setColorFilter(colorFilter);
        copyButton.getDrawable().setColorFilter(colorFilter);

        setupOnClickListeners(menuButton, copyButton);

//        setTapToReveal(tapToReveal);

//
//
//        itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Key key = mKeys.get(getAdapterPosition());
//
//                Intent intent = new Intent(MainActivity.this, KeyDetailActivity.class);
//                intent.putExtra("keyId", key.getId());
//                MainActivity.this.startActivity(intent);
//            }
//        });
    }

    private void setTapToReveal(boolean tapToReveal) {

    }

    @FunctionalInterface
    private interface AdapterPositionSafeCallbackConsumer {
        /** The specified {@link Callback} is guaranteed to be non-null, and adapterPosition is
         * guaranteed to be a valid position. */
        void accept(@NonNull Callback callback, int adapterPosition);
    }

    private void adapterPositionSafeCallback(AdapterPositionSafeCallbackConsumer safeCallback) {
        int clickedPosition = getAdapterPosition();
        if (callback != null && clickedPosition != RecyclerView.NO_POSITION) {
            safeCallback.accept(callback, clickedPosition);
        }
    }

    private void setupOnClickListeners(ImageButton menuButton, ImageButton copyButton) {
        menuButton.setOnClickListener(view ->
                adapterPositionSafeCallback((callback, adapterPosition) ->
                        callback.onMenuButtonClicked(view, adapterPosition)
                )
        );

        copyButton.setOnClickListener(view ->
                adapterPositionSafeCallback((callback, adapterPosition) ->
                        callback.onCopyButtonClicked(passwordView.getTag().toString(), adapterPosition)
                )
        );

//        counterLayout.setOnClickListener(view ->
//                adapterPositionSafeCallback(Callback::onCounterClicked)
//        );
//
//        counterLayout.setOnLongClickListener(view -> {
//            adapterPositionSafeCallback(Callback::onCounterLongPressed);
//            return false;
//        });

        card.setOnClickListener(new SimpleDoubleClickListener() {
            @Override
            public void onSingleClick(View v) {
                adapterPositionSafeCallback((callback, adapterPosition) ->
                        callback.onCardSingleClicked(adapterPosition, passwordView.getTag().toString())
                );
            }

            @Override
            public void onDoubleClick(View v) {
                adapterPositionSafeCallback((callback, adapterPosition) ->
                        callback.onCardDoubleClicked(adapterPosition, passwordView.getTag().toString())
                );
            }
        });
    }

    public void setCallback(Callback cb) {
        this.callback = cb;
    }
}