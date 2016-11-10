package wycliffeassociates.recordingapp.widgets;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by sarabiaj on 11/10/2016.
 */

public class VerseMarker extends DraggableMarker {

    public VerseMarker(VerseMarkerView view, int color){
        super(view, configurePaint(color));
    }

    private static Paint configurePaint(int color){
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8.f);
        return paint;
    }

    @Override
    public void drawMarkerLine(Canvas canvas) {
        canvas.drawLine(mView.getMarkerX(), 0, mView.getMarkerX(), canvas.getHeight(), mMarkerLinePaint);
    }
}
