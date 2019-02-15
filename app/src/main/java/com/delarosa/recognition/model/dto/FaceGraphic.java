/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.delarosa.recognition.model.dto;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.delarosa.recognition.controller.GraphicOverlay;
import com.google.android.gms.vision.face.Face;


import static java.lang.Math.abs;

/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
public class FaceGraphic extends GraphicOverlay.Graphic {
    private static final float FACE_POSITION_RADIUS = 10.0f;
    private static final float ID_TEXT_SIZE = 40.0f;
    private static final float ID_Y_OFFSET = 50.0f;
    private static final float ID_X_OFFSET = -50.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;

    private static final int COLOR_CHOICES[] = {
            Color.BLUE,
            Color.CYAN,
            Color.GREEN,
            Color.MAGENTA,
            Color.RED,
            Color.WHITE,
            Color.YELLOW
    };
    private static int mCurrentColorIndex = 0;

    private Paint mFacePositionPaint;
    private Paint mIdPaint;
    private Paint mBoxPaint;

    private volatile Face mFace;
    private int mFaceId;
    private Context context;
    private float mFaceHappiness;
    public static final String MY_PREFS_NAME = "FACE";

    public FaceGraphic(GraphicOverlay overlay, Context context) {
        super(overlay);
        this.context = context;
        mCurrentColorIndex = (mCurrentColorIndex + 1) % COLOR_CHOICES.length;
        final int selectedColor = COLOR_CHOICES[mCurrentColorIndex];

        mFacePositionPaint = new Paint();
        mFacePositionPaint.setColor(selectedColor);

        mIdPaint = new Paint();
        mIdPaint.setColor(selectedColor);
        mIdPaint.setTextSize(ID_TEXT_SIZE);

        mBoxPaint = new Paint();
        mBoxPaint.setColor(selectedColor);
        mBoxPaint.setStyle(Paint.Style.STROKE);
        mBoxPaint.setStrokeWidth(BOX_STROKE_WIDTH);
    }

    public void setId(int id) {
        mFaceId = id;
    }


    /**
     * Updates the face instance from the detection of the most recent frame.  Invalidates the
     * relevant portions of the overlay to trigger a redraw.
     */
    public void updateFace(Face face) {
        mFace = face;
        postInvalidate();
    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        Face face = mFace;
        if (face == null) {
            return;
        }

        // Draws a circle at the position of the detected face, with the face's track id below.
        float x = translateX(face.getPosition().x + face.getWidth() / 2);
        float y = translateY(face.getPosition().y + face.getHeight() / 2);
        canvas.drawCircle(x, y, FACE_POSITION_RADIUS, mFacePositionPaint);
        canvas.drawText("ID: " + mFaceId, x + ID_X_OFFSET, y + ID_Y_OFFSET, mIdPaint);
        String happiness = String.format("%.2f", face.getIsSmilingProbability());
        canvas.drawText("Felicidad: " + happiness, x - ID_X_OFFSET, y - ID_Y_OFFSET, mIdPaint);
        //canvas.drawText("right eye: " + String.format("%.2f", face.getIsRightEyeOpenProbability()), x + ID_X_OFFSET * 2, y + ID_Y_OFFSET * 2, mIdPaint);
        //canvas.drawText("left eye: " + String.format("%.2f", face.getIsLeftEyeOpenProbability()), x - ID_X_OFFSET * 2, y - ID_Y_OFFSET * 2, mIdPaint);
        //canvas.drawText("Landmarks: " + String.format("%.2f", face.getLandmarks()), x - ID_X_OFFSET * 2, y - ID_Y_OFFSET * 2, mIdPaint);


        //StringBuilder sb = new StringBuilder();
        //int count = 1;
        //for (Landmark u: face.getLandmarks()) {
        //sb.append("" + count++ + ": " + u.getPosition() + " " + u.getType() + "\n");
        //sb.append(u.getType() + u.toString() + "\n");
        //}

        //canvas.drawText("Landmarks: " + sb, x - ID_X_OFFSET * 2, y - ID_Y_OFFSET * 2, mIdPaint);


        // Draws a bounding box around the face.
        //
        float xOffset = scaleX(face.getWidth() / 3.0f);
        float yOffset = scaleY(face.getHeight() / 3.0f);
        float left = x - xOffset;
        float top = y - yOffset;
        float right = x + xOffset;
        float bottom = y + yOffset;
        int id = mFaceId;

        SharedPreferences.Editor editor = context.getSharedPreferences(MY_PREFS_NAME, context.MODE_PRIVATE).edit();
        editor.putFloat("left", left);
        editor.putFloat("top", top);
        editor.putFloat("width", abs(right - left));
        editor.putFloat("height", abs(top - bottom));
        editor.putInt("id", id);
        editor.putString("happiness", happiness);

        editor.apply();
        canvas.drawRect(left, top, right, bottom, mBoxPaint);
    }
}
