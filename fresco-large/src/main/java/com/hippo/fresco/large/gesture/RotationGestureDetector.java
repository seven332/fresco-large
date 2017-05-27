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

import android.view.MotionEvent;

public class RotationGestureDetector {

  private static final int INVALID_POINTER_ID = -1;

  private static final float ROTATION_SLOP = 2.0f; // TODO Need a better one

  private int id1 = INVALID_POINTER_ID;
  private int id2 = INVALID_POINTER_ID;
  private boolean rotated;
  private float lastX1, lastY1;
  private float lastX2, lastY2;
  private float lastAngle = Float.NaN;

  private final Listener listener;

  public RotationGestureDetector(Listener listener) {
    this.listener = listener;
  }

  public boolean onTouchEvent(MotionEvent event) {
    switch (event.getActionMasked()) {
      case MotionEvent.ACTION_DOWN: {
        id1 = event.getPointerId(event.getActionIndex());
        id2 = INVALID_POINTER_ID;
        lastAngle = Float.NaN;
        rotated = false;
        break;
      }
      case MotionEvent.ACTION_POINTER_DOWN: {
        int id = event.getPointerId(event.getActionIndex());
        if (id1 == INVALID_POINTER_ID) {
          id1 = id;
        } else if (id2 == INVALID_POINTER_ID) {
          id2 = id;
        } else {
          // Another pointer, ignore
          break;
        }

        if (id1 == INVALID_POINTER_ID || id2 == INVALID_POINTER_ID) {
          // Two pointer has not been caught
          break;
        }

        lastX1 = event.getX(event.findPointerIndex(id1));
        lastY1 = event.getY(event.findPointerIndex(id1));
        lastX2 = event.getX(event.findPointerIndex(id2));
        lastY2 = event.getY(event.findPointerIndex(id2));
        lastAngle = Float.NaN;
        rotated = false;
        break;
      }
      case MotionEvent.ACTION_MOVE: {
        if (id1 == INVALID_POINTER_ID || id2 == INVALID_POINTER_ID) {
          // Two pointer has not been caught
          break;
        }
        int id = event.getPointerId(event.getActionIndex());
        if (id != id1 && id != id2) {
          // The pointer isn't one of the two pointers
          break;
        }

        // Get last angle
        if (Float.isNaN(lastAngle)) {
          lastAngle = (float) Math.atan2(lastY1 - lastY2, lastX1 - lastX2);
        }

        // Get current angle
        float x1 = event.getX(event.findPointerIndex(id1));
        float y1 = event.getY(event.findPointerIndex(id1));
        float x2 = event.getX(event.findPointerIndex(id2));
        float y2 = event.getY(event.findPointerIndex(id2));
        float angle = (float) Math.atan2(y1 - y2, x1 - x2);

        // Get rotation angle
        float rotation = ((float) Math.toDegrees(lastAngle - angle)) % 360.0f;
        if (rotation < -180.f) {
          rotation += 360.0f;
        }
        if (rotation > 180.f) {
          rotation -= 360.0f;
        }

        // Check whether rotated
        boolean firstCallback = false;
        if (!rotated) {
          rotated = Math.abs(rotation) > ROTATION_SLOP;
          if (rotated) {
            firstCallback = true;
          } else {
            break;
          }
        }

        // Ignore the first callback. The value of the first callback is larger than
        // the ROTATION_SLOP. It's a mutation, not friendly.
        if (!firstCallback) {
          // Get intersection of two lines
          float denominator = (lastX1 - lastX2) * (y1 - y2) - (lastY1 - lastY2) * (x1 - x2);
          if (denominator == 0.0f) {
            // Two lines are parallel or coincident
            break;
          }
          float x = ((lastX1 * lastY2 - lastY1 * lastX2) * (x1 - x2) - (lastX1 - lastX2) * (
              x1 * y2 - y1 * x2)) / denominator;
          float y = ((lastX1 * lastY2 - lastY1 * lastX2) * (y1 - y2) - (lastY1 - lastY2) * (
              x1 * y2 - y1 * x2)) / denominator;

          // Callback
          listener.onRotate(rotation, x, y);
        }

        // Update lastX, lastY, lastAngle
        lastX1 = x1;
        lastY1 = y1;
        lastX2 = x2;
        lastY2 = y2;
        lastAngle = angle;
        break;
      }
      case MotionEvent.ACTION_POINTER_UP: {
        int id = event.getPointerId(event.getActionIndex());
        if (id1 == id) {
          id1 = INVALID_POINTER_ID;
          lastAngle = Float.NaN;
          rotated = false;
        } else if (id2 == id) {
          id2 = INVALID_POINTER_ID;
          lastAngle = Float.NaN;
          rotated = false;
        }
        break;
      }
      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_CANCEL: {
        id1 = INVALID_POINTER_ID;
        id2 = INVALID_POINTER_ID;
        lastAngle = Float.NaN;
        rotated = false;
        break;
      }
    }

    return rotated;
  }

  public interface Listener {
    void onRotate(float angle, float x, float y);
  }
}
