package com.hekkelman.keylocker.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.hekkelman.keylocker.R;
import com.hekkelman.keylocker.databinding.CardviewKeyItemBinding;
import com.hekkelman.keylocker.databinding.FragmentItemListBinding;
import com.hekkelman.keylocker.datamodel.Key;
import com.hekkelman.keylocker.datamodel.KeyDbModel;
import com.hekkelman.keylocker.datamodel.KeyNote;

import java.util.ArrayList;
import java.util.List;

public class KeyListFragment extends Fragment {

    private FragmentItemListBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentItemListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = binding.recyclerView;
        KeyDbModel keyDbModel = new ViewModelProvider(getActivity()).get(KeyDbModel.class);

        View.OnClickListener onClickListener = itemView -> {
            KeyNote item = (KeyNote) itemView.getTag();
            Bundle arguments = new Bundle();
            arguments.putString("item-id", item.getId());
            if (item instanceof Key) {
                keyDbModel.select((Key)item);
                Navigation.findNavController(itemView).navigate(R.id.show_key_detail, arguments);
            }
        };

        recyclerView.setAdapter(new KeyNoteRecyclerViewAdapter(keyDbModel, onClickListener));
    }

    public static class KeyNoteRecyclerViewAdapter extends RecyclerView.Adapter<KeyNoteRecyclerViewAdapter.ViewHolder> {

        private final KeyDbModel keyDbModel;
        private final View.OnClickListener onClickListener;
        private final List<KeyNote> items = new ArrayList<>();

        public KeyNoteRecyclerViewAdapter(KeyDbModel keyDbModel, View.OnClickListener onClickListener) {
            this.keyDbModel = keyDbModel;
            this.onClickListener = onClickListener;

            items.add(new Key("x", "een", "aap", "", ""));
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            CardviewKeyItemBinding binding =
                    CardviewKeyItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            KeyNote item = items.get(position);

            holder.nameView.setText(item.getName());
            holder.infoView.setText(item.getDescription());

            holder.itemView.setOnClickListener(onClickListener);
            holder.itemView.setTag(item);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            protected final TextView nameView;
            protected final TextView infoView;
            protected final ImageButton copyButton;
            protected final ImageButton menuButton;

            public ViewHolder(CardviewKeyItemBinding binding) {
                super(binding.getRoot());
                nameView = binding.itemName;
                infoView = binding.itemDetail;
                copyButton = binding.copyButton;
                menuButton = binding.menuButton;
            }
        }
    }
}


