package org.wycliffeassociates.translationrecorder.Playback.overlays;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by Parrot on 11/22/16.
 */

public class MinimapLayer extends View {

    private Bitmap mBitmap = null;
    private boolean initialized = false;
    private Paint mPaint = new Paint();
    private MinimapDrawDelegator mMinimapDrawDelegator;

    public interface MinimapDrawDelegator {
        boolean onDelegateMinimapDraw(Canvas canvas, Paint paint);
    }

    public static MinimapLayer newInstance(Context context, MinimapDrawDelegator drawDelegator) {
        MinimapLayer ml = new MinimapLayer(context);
        ml.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        ml.setMinimapDrawDelegator(drawDelegator);
        return ml;
    }

    public MinimapLayer(Context context) {
        super(context);
        mPaint = new Paint();
        mPaint.setColor(Color.GRAY);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(1f);
    }

    public void setMinimapDrawDelegator(MinimapDrawDelegator minimapDrawDelegator) {
        mMinimapDrawDelegator = minimapDrawDelegator;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        if (initialized) {
//            canvas.drawBitmap(mBitmap, 0, 0, mPaint);
//        } else {
            mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(mBitmap);
            Drawable background = getBackground();
            if(background != null){
                background.draw(c);
            }
            else {
                c.drawColor(Color.TRANSPARENT);
            }
            if (mMinimapDrawDelegator.onDelegateMinimapDraw(c, mPaint)) {
                setBackground(background);
                initialized = true;
            }
        canvas.drawBitmap(mBitmap, 0, 0, mPaint);

    }
}


