package org.wycliffeassociates.translationrecorder.Playback.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.wycliffeassociates.translationrecorder.Playback.interfaces.MarkerMediator;
import org.wycliffeassociates.translationrecorder.Playback.interfaces.VerseMarkerModeToggler;

import org.wycliffeassociates.translationrecorder.R;

/**
 * Created by sarabiaj on 11/15/2016.
 */

public class MarkerCounterFragment extends Fragment {

    private static String KEY_MARKERS_REMAINING = "markers_remaining";

    private VerseMarkerModeToggler mModeToggleCallback;
    private MarkerMediator mMarkerMediator;
    private TextView mVersesRemainingView;
    private ImageView mEscape;
    private TextView mLeftView;

    public static MarkerCounterFragment newInstance(MarkerMediator mediator){
        MarkerCounterFragment f = new MarkerCounterFragment();
        f.setMarkerMediator(mediator);
        return f;
    }

    private void setMarkerMediator(MarkerMediator mediator){
        mMarkerMediator = mediator;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mModeToggleCallback = (VerseMarkerModeToggler) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_marker_top_bar, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews();
        initViews();
    }

    private void findViews(){
        View view = getView();
        mVersesRemainingView = (TextView) view.findViewById(R.id.verse_marker_count);
        mLeftView = (TextView) view.findViewById(R.id.verse_marker_label);
        mEscape = (ImageView) view.findViewById(R.id.btn_exit_verse_marker_mode);
    }

    private void initViews(){
        mLeftView.setText("Left");
        mVersesRemainingView.setText(String.valueOf(mMarkerMediator.numVersesRemaining()));
        mEscape.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mModeToggleCallback.onDisableVerseMarkerMode();
            }
        });
    }

    public void decrementVersesRemaining(){
        mVersesRemainingView.setText(String.valueOf(mMarkerMediator.numVersesRemaining()));
    }
}
