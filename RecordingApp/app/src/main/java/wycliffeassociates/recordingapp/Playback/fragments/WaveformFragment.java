package wycliffeassociates.recordingapp.Playback.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.Collection;
import java.util.HashMap;

import wycliffeassociates.recordingapp.Playback.MarkerLineLayer;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.widgets.DragableViewFrame;
import wycliffeassociates.recordingapp.widgets.DraggableImageView;
import wycliffeassociates.recordingapp.widgets.DraggableMarker;
import wycliffeassociates.recordingapp.widgets.SectionMarker;
import wycliffeassociates.recordingapp.widgets.SectionMarkerView;
import wycliffeassociates.recordingapp.widgets.VerseMarker;
import wycliffeassociates.recordingapp.widgets.VerseMarkerView;

/**
 * Created by sarabiaj on 11/4/2016.
 */

public class WaveformFragment extends Fragment implements DraggableImageView.PositionChangeMediator, MarkerLineLayer.MarkerLineDrawDelegator{

    DragableViewFrame mDraggableViewFrame;
    MarkerLineLayer mMarkerLineLayer;
    HashMap<Integer, DraggableMarker> mMarkers = new HashMap<>();
    final int START_MARKER_ID = -1;
    final int END_MARKER_ID = -2;

    Paint mMarkerLinePaint;
    FrameLayout mFrame;

    public static WaveformFragment newInstance(){
        WaveformFragment f = new WaveformFragment();
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_waveform, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews();

        mMarkerLineLayer = MarkerLineLayer.newInstance(getActivity(), this);
        mFrame.addView(mMarkerLineLayer);
        addStartMarker();
        addEndMarker();
        addVerseMarker();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    private void findViews(){
        View view = getView();
        mDraggableViewFrame = (DragableViewFrame) view.findViewById(R.id.draggable_view_frame);
        mFrame = (FrameLayout) view.findViewById(R.id.waveform_frame);
    }

    public void addStartMarker(){
        SectionMarkerView div = SectionMarkerView.newInstance(getActivity(), R.drawable.ic_startmarker_cyan, START_MARKER_ID, SectionMarkerView.Orientation.LEFT_MARKER);
        div.setPositionChangeMediator(this);
        mDraggableViewFrame.addView(div);
        mMarkers.put(START_MARKER_ID, new SectionMarker(div, getResources().getColor(R.color.dark_moderate_cyan)));

    }

    public void addEndMarker(){
        SectionMarkerView div = SectionMarkerView.newInstance(getActivity(), R.drawable.ic_endmarker_cyan, Gravity.BOTTOM, END_MARKER_ID, SectionMarkerView.Orientation.RIGHT_MARKER);
        div.setPositionChangeMediator(this);
        mDraggableViewFrame.addView(div);
        mMarkers.put(END_MARKER_ID, new SectionMarker(div, getResources().getColor(R.color.dark_moderate_cyan)));
    }

    public void addVerseMarker(){
        VerseMarkerView div = VerseMarkerView.newInstance(getActivity(), R.drawable.bookmark_add, 1);
        div.setPositionChangeMediator(this);
        mDraggableViewFrame.addView(div);
        mMarkers.put(1, new VerseMarker(div, getResources().getColor(R.color.yellow)));
    }

    @Override
    public float onPositionRequested(int id, float x){
        if(id < 0) {
            if(id == END_MARKER_ID){
                return Math.max(mMarkers.get(START_MARKER_ID).getMarkerX(), x);
            } else {
                return Math.min(mMarkers.get(END_MARKER_ID).getMarkerX() - mMarkers.get(END_MARKER_ID).getWidth(), x);
            }
        } else {
            return x;
        }
    }

    @Override
    public void onPositionChanged(int id, float x) {
        mMarkerLineLayer.postInvalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        Collection<DraggableMarker> markers = mMarkers.values();
        for (DraggableMarker d : markers) {
            d.drawMarkerLine(canvas);
        }
    }
}
