package com.skyline.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.skyline.app.databinding.BottomSheetPassengerBinding;

public class PassengerBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetPassengerBinding binding;
    private int adults = 1, children = 0;
    private OnPassengerSelectedListener listener;

    public interface OnPassengerSelectedListener {
        void onSelected(int adults, int children);
    }

    public PassengerBottomSheet(int adults, int children, OnPassengerSelectedListener listener) {
        this.adults = adults;
        this.children = children;
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetPassengerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateUI();

        binding.btnPlusAdults.setOnClickListener(v -> { adults++; updateUI(); });
        binding.btnMinusAdults.setOnClickListener(v -> { if (adults > 1) { adults--; updateUI(); } });

        binding.btnPlusChildren.setOnClickListener(v -> { children++; updateUI(); });
        binding.btnMinusChildren.setOnClickListener(v -> { if (children > 0) { children--; updateUI(); } });

        binding.btnDone.setOnClickListener(v -> {
            listener.onSelected(adults, children);
            dismiss();
        });
    }

    private void updateUI() {
        binding.tvAdultsCount.setText(String.valueOf(adults));
        binding.tvChildrenCount.setText(String.valueOf(children));
    }
}
