package com.example.workflowly;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class DoubleClickListener implements View.OnTouchListener {
    private GestureDetector gestureDetector;

    public DoubleClickListener(Context context, GestureDetector.SimpleOnGestureListener listener) {
        gestureDetector = new GestureDetector(context, listener);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }
}
