package liuriloami.pinnedtrips.gallery;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class GalleryGridView extends GridView {
    public GalleryGridView(Context context) {
        super(context);
    }

    public GalleryGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GalleryGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    //Overriding this method, allows a better scale of the gridView height
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightSpec;

        if (getLayoutParams().height == LayoutParams.WRAP_CONTENT) {
            heightSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        }
        else {
            heightSpec = heightMeasureSpec;
        }

        super.onMeasure(widthMeasureSpec, heightSpec);
    }


}