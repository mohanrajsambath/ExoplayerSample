package com.example.innobot_linux_2.exoplayersample;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by Obaro on 01/08/2016.
 */
public class SpeedDialogFragment extends BottomSheetDialogFragment {
    int[] checkBoxArray = {R.id.chkBx_speed1, R.id.chkBx_speed2, R.id.chkBx_speed3, R.id.chkBx_speed4, R.id.chkBx_speed5, R.id.chkBx_speed6, R.id.chkBx_speed7};
    int[] textViewArray = {R.id.txtVw_speed1, R.id.txtVw_speed2, R.id.txtVw_speed3, R.id.txtVw_speed4, R.id.txtVw_speed5, R.id.txtVw_speed6, R.id.txtVw_speed7};
    boolean[] selectionState;
    ArrayList<AppCompatCheckBox> CheckboxList = new ArrayList<>();
    ArrayList<AppCompatTextView> textViewList = new ArrayList<>();
    String speed = "";

    public static SpeedDialogFragment newInstance(String speed) {
        SpeedDialogFragment f = new SpeedDialogFragment();
        Bundle args = new Bundle();
        args.putString("speed", speed);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        speed = getArguments().getString("speed");
    }

    private BottomSheetBehavior.BottomSheetCallback
            mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }
    };

    @Override
    public void setupDialog(final Dialog dialog, int style) {
        View contentView = View.inflate(getContext(), R.layout.fragment_speed_dialog, null);
        selectionState = new boolean[checkBoxArray.length];
        for (int i = 0; i < checkBoxArray.length; i++) {
            CheckboxList.add(castCheckBox(contentView, checkBoxArray[i]));
            textViewList.add(castTextView(contentView, textViewArray[i]));
        }
        for (int i = 0; i < textViewList.size(); i++) {
            final AppCompatTextView compatTextView = textViewList.get(i);
            if (compatTextView.getText().toString().trim().equalsIgnoreCase(speed.trim())) {
                selectionState[i] = true;
            } else {
                selectionState[i] = false;
            }
            compatTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity trayActivitySkip = (MainActivity) getActivity();
                    trayActivitySkip.speedSelected(compatTextView.getText().toString().trim());
                    getDialog().dismiss();
                }
            });

        }

        for (int i = 0; i < CheckboxList.size(); i++) {
            AppCompatCheckBox compatCheckBox = CheckboxList.get(i);
            if (selectionState[i]) {
                compatCheckBox.setButtonDrawable(getActivity().getDrawable(R.drawable.ic_done_black_24px));
            } else {
                compatCheckBox.setButtonDrawable(null);
            }

            compatCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

        }

        dialog.setContentView(contentView);
    }

    public AppCompatCheckBox castCheckBox(View view, int id) {
        return (AppCompatCheckBox) view.findViewById(id);
    }

    public AppCompatTextView castTextView(View view, int id) {
        return (AppCompatTextView) view.findViewById(id);
    }
}
