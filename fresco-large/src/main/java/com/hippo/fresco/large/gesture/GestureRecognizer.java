/*
 * Copyright 2017 Hippo Seven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hippo.fresco.large.gesture;

/*
 * Created by Hippo on 5/26/2017.
 */

import android.content.Context;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

public class GestureRecognizer implements ScaleGestureDetector.OnScaleGestureListener,
    RotationGestureDetector.OnRotateGestureListener {

  private static final float SCALE_SLOP = 0.015f;
  private static final float ROTATE_SLOP = 0.5f;

  private Listener listener;

  private ScaleGestureDetector scaleGestureDetector;
  private RotationGestureDetector rotationGestureDetector;

  private boolean isScaling;
  private boolean isRotating;

  private float scaling;
  private float rotating;

  public GestureRecognizer(Context context, Listener listener) {
    this.listener = listener;

    scaleGestureDetector = new ScaleGestureDetector(context, this);
    rotationGestureDetector = new RotationGestureDetector(this);
  }

  public boolean onTouchEvent(MotionEvent event) {
    scaleGestureDetector.onTouchEvent(event);
    rotationGestureDetector.onTouchEvent(event);
    return true;
  }

  ///////////////////////////////////////////////////////////////////////////
  // Scale
  ///////////////////////////////////////////////////////////////////////////

  @Override
  public boolean onScale(ScaleGestureDetector detector) {
    scaling = detector.getScaleFactor();
    if (scaling < 1.0f) {
      scaling = 1.0f / scaling;
    }
    scaling -= 1.0f;

    if (isRotating) {
      if (rotating < ROTATE_SLOP && scaling > SCALE_SLOP) {
        // Switch from rotating to scaling
        isRotating = false;
        isScaling = true;
      }
    } else if (!isScaling) {
      isScaling = true;
    }

    if (isScaling) {
      listener.onScale(detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY());
    }

    return true;
  }

  @Override
  public boolean onScaleBegin(ScaleGestureDetector detector) {
    if (!isRotating) {
      isScaling = true;
    }
    return true;
  }

  @Override
  public void onScaleEnd(ScaleGestureDetector detector) {
    isScaling = false;
  }

  ///////////////////////////////////////////////////////////////////////////
  // Rotation
  ///////////////////////////////////////////////////////////////////////////

  @Override
  public boolean onRotate(float angle, float x, float y) {
    rotating = Math.abs(angle);

    if (isScaling) {
      if (scaling < SCALE_SLOP && rotating > ROTATE_SLOP) {
        // Switch from scaling to rotating
        isScaling = false;
        isRotating = true;
      }
    } else if (!isRotating) {
      isRotating = true;
    }

    if (isRotating) {
      listener.onRotate(angle, x, y);
    }

    return true;
  }

  @Override
  public boolean onRotateBegin() {
    if (!isScaling) {
      isRotating = true;
    }
    return true;
  }

  @Override
  public void onRotateEnd() {
    isRotating = false;
  }

  public interface Listener {

    void onScale(float factor, float x, float y);

    void onRotate(float angle, float x, float y);
  }
}
