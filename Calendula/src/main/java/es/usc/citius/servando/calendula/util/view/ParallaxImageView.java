package es.usc.citius.servando.calendula.util.view;


import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;


public class ParallaxImageView extends ImageView {

    private int mCurrentTranslation;

    public ParallaxImageView(Context context) {
        super(context);
    }

    public ParallaxImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ParallaxImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void updateParallax(){
        int[] ls = new int[2];
        getLocationOnScreen(ls);
        float top = (float)ls[1];
        if(top > 0) {
            mCurrentTranslation = -(int)(top*1.5);
            invalidate();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.save();
        canvas.translate(0, mCurrentTranslation);
        super.draw(canvas);
        canvas.restore();
    }
}