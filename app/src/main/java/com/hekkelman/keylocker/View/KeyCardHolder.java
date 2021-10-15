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
import com.hekkelman.keylocker.datamodel.Key;

public class KeyCardHolder extends RecyclerView.ViewHolder {
    protected Key key;
    protected CardView card;
    protected TextView nameView;
    protected TextView userView;
    protected ImageButton copyButton;
    protected ImageButton menuButton;

    private Callback callback;

    public interface Callback {
        void onMenuButtonClicked(View view, int position);
        void onCopyButtonClicked(String password);
        void onCardSingleClicked(String keyID);
        void onCardDoubleClicked(String keyID);
    }

    public KeyCardHolder(Context context, View itemView) {
        super(itemView);

        // Style the buttons in the current theme colors
        ColorFilter colorFilter = Tools.getThemeColorFilter(context, android.R.attr.textColorSecondary);

        card = (CardView) itemView;
        nameView = itemView.findViewById(R.id.itemCaption);
        userView = itemView.findViewById(R.id.itemUser);
        menuButton = itemView.findViewById(R.id.menuButton);
        copyButton = itemView.findViewById(R.id.copyButton);

        menuButton.getDrawable().setColorFilter(colorFilter);
        copyButton.getDrawable().setColorFilter(colorFilter);

        menuButton.setOnClickListener(view ->
                adapterPositionSafeCallback((callback, adapterPosition) ->
                        callback.onMenuButtonClicked(view, adapterPosition)
                )
        );

        copyButton.setOnClickListener(view ->
                adapterPositionSafeCallback((callback, adapterPosition) ->
                        callback.onCopyButtonClicked(key.getPassword())
                )
        );

        card.setOnClickListener(new SimpleDoubleClickListener() {
            @Override
            public void onSingleClick(View v) {
                adapterPositionSafeCallback((callback, adapterPosition) ->
                        callback.onCardSingleClicked(key.getId())
                );
            }

            @Override
            public void onDoubleClick(View v) {
                adapterPositionSafeCallback((callback, adapterPosition) ->
                        callback.onCardDoubleClicked(key.getId())
                );
            }
        });
    }

    protected void setKey(Key key) {
        this.key = key;

        nameView.setText(key.getName());
        userView.setText(key.getUser());
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

    public void setCallback(Callback cb) {
        this.callback = cb;
    }
}