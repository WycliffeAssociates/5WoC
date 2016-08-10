package wycliffeassociates.recordingapp.ProjectManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.widgets.FourStepImageView;

/**
 * Created by leongv on 8/9/2016.
 */
public class CheckingDialogFragment extends DialogFragment {

    public interface DialogListener {
        public void onPositiveClick(CheckingDialogFragment dialog);
        public void onNegativeClick(CheckingDialogFragment dialog);
    }

    private int mCheckingLevel;
    DialogListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // NOTE: Code to check current rating here. Maybe?
        // mCheckingLevel = ??
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();

        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Rate this take")
                .setView(inflater.inflate(R.layout.dialog_checking, null))
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onPositiveClick(CheckingDialogFragment.this);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onNegativeClick(CheckingDialogFragment.this);
                    }
                })
                .create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                AlertDialog alertDialog= (AlertDialog) dialog;

                // Button positiveBtn = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                // positiveBtn.setBackground(getResources().getDrawable(R.drawable.delete));

                final FourStepImageView levelOne = (FourStepImageView) alertDialog.findViewById(R.id.check_level_one);
                final FourStepImageView levelTwo = (FourStepImageView) alertDialog.findViewById(R.id.check_level_two);
                final FourStepImageView levelThree = (FourStepImageView) alertDialog.findViewById(R.id.check_level_three);

                levelOne.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        System.out.println("Level one");
                        levelOne.setStep(1);
                        levelTwo.setStep(0);
                        levelThree.setStep(0);
                        mCheckingLevel = 1;
                    }
                });

                levelTwo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        System.out.println("Level two");
                        levelOne.setStep(2);
                        levelTwo.setStep(2);
                        levelThree.setStep(0);
                        mCheckingLevel = 2;
                    }
                });

                levelThree.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        System.out.println("Level three");
                        levelOne.setStep(3);
                        levelTwo.setStep(3);
                        levelThree.setStep(3);
                        mCheckingLevel = 3;
                    }
                });
            }
        });

        return alertDialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (DialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement CheckingDialogListener");
        }
    }

    public int getCheckingLevel() {
        return mCheckingLevel;
    }
}