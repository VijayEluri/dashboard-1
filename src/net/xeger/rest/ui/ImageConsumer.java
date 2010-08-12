package net.xeger.rest.ui;

import android.graphics.Bitmap;

public interface ImageConsumer {
    public void consumeImage(Bitmap bitmap, String tag);
    public void consumeImageError(Throwable error, String tag);
}
