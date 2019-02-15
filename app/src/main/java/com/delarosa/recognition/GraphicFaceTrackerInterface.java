package com.delarosa.recognition;

import android.graphics.Bitmap;

public interface GraphicFaceTrackerInterface {
    void getData(Bitmap croppedBitmap, Bitmap completeBitmap, int id);
}
