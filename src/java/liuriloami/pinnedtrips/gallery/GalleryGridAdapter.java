package liuriloami.pinnedtrips.gallery;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import liuriloami.pinnedtrips.R;

public class GalleryGridAdapter extends BaseAdapter {
    ArrayList<String> images;
    protected LayoutInflater inflater;


    public GalleryGridAdapter(Context context, ArrayList<String> images)
    {
        inflater = LayoutInflater.from(context);
        this.images = images;
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public String getItem(int i)
    {
        return images.get(i);
    }

    //Insted of displaying the original image, a thumbnail is generated
    //In this way, the gallery is displayed faster
    @Override
    public long getItemId(int position) {
        return position;
    }

    private Bitmap getThumbnail (String path) {
        Bitmap thumbnail = null;
        try {
            final int THUMBNAIL_SIZE = 256;

            FileInputStream fis = new FileInputStream(path);
            thumbnail = BitmapFactory.decodeStream(fis);

            int width = thumbnail.getWidth();
            int height = thumbnail.getHeight();
            thumbnail= Bitmap.createScaledBitmap(thumbnail,
                    THUMBNAIL_SIZE, (THUMBNAIL_SIZE*height)/width, false);

            ByteArrayOutputStream bytearroutstream = new ByteArrayOutputStream();
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 100,bytearroutstream);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return thumbnail;
    }

    //The image is retrieved from its filename and its thumbnail is displayed on the gallery
    @Override
    public View getView(int i, View view, ViewGroup viewGroup)
    {
        View v = view;
        ImageView image;

        if(v == null) {
            v = inflater.inflate(R.layout.gallery_image, viewGroup, false);
            v.setTag(R.id.image, v.findViewById(R.id.image));
        }

        image = (ImageView) v.getTag(R.id.image);

        try {
            image.setImageBitmap(getThumbnail(getItem(i)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return v;
    }
}
