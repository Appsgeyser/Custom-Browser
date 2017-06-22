package acr.browser.lightning.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.InputStream;

/**
 * Created by roma on 11.06.2017.
 */

public class ImageLoader {

    private LruCache<String, Bitmap> imageCache;

    private static ImageLoader INSTANCE;

    public static ImageLoader getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ImageLoader();
        }
        return INSTANCE;
    }

    private ImageLoader() {
        int cacheSize = 5 * 1024 * 1024; // 4MiB
        imageCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount() / 1024;
            }
        };
    }
    //------------------------------------------------------

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public synchronized void loadImage(final String url, final ImageLoadedListener imageLoadedListener) {
        final Bitmap bitmap;
        if ((bitmap = imageCache.get(url)) != null) {
            imageLoadedListener.onImageLoaded(bitmap);
        } else {
            new AsyncTask<String, Void, Bitmap>() {

                protected Bitmap doInBackground(String... urls) {
                    String urldisplay = urls[0];
                    Bitmap mIcon11 = null;
                    if ((mIcon11 = imageCache.get(url)) != null) {
                        return mIcon11;
                    }
                    InputStream in = null;
                    try {
                        in = new java.net.URL(urldisplay).openStream();
                        final BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeStream(in, null, options);
                        options.inSampleSize = calculateInSampleSize(options, 200, 200);
                        options.inJustDecodeBounds = false;
                        in.close();
                        in = new java.net.URL(urldisplay).openStream();
                        mIcon11 = BitmapFactory.decodeStream(in, null, options);
                        in.close();
                    } catch (Exception e) {
                        Log.e("Error", e.getMessage());
                        e.printStackTrace();
                    }
                    return mIcon11;
                }

                protected void onPostExecute(Bitmap result) {
                    if (result != null) {
                        imageLoadedListener.onImageLoaded(result);
                        imageCache.put(url, result);
                    }
                }
            }.execute(url);
        }
    }

    public interface ImageLoadedListener {
        void onImageLoaded(Bitmap b);
    }
}
