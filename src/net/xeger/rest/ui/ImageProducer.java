package net.xeger.rest.ui;

import net.xeger.rest.RestException;
import android.graphics.Bitmap;

public interface ImageProducer {
    public Bitmap produceImage(String tag) throws RestException;
}
