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

package com.hippo.fresco.large.demo;

/*
 * Created by Hippo on 5/23/2017.
 */

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import com.facebook.common.references.CloseableReference;
import com.facebook.drawable.base.DrawableWithCaches;
import com.hippo.fresco.large.ClosableLargeImage;
import com.hippo.fresco.large.ImageRegionDecoder;
import com.hippo.fresco.large.LargeDrawableFactory;

public class BoringLargeDrawableFactory extends LargeDrawableFactory {

  @Override
  public Drawable createLargeDrawable(ClosableLargeImage image) {
    CloseableReference<ImageRegionDecoder> reference = image.getDecoder();
    if (reference != null) {
      try {
        ImageRegionDecoder decoder = reference.get();
        Rect rect = new Rect();
        rect.set(0, 0, decoder.getWidth(), decoder.getHeight());
        Bitmap bitmap = decoder.decode(rect, null);
        if (bitmap != null) {
          return new RecyclableBitmapDrawable(bitmap);
        }
      } finally {
        reference.close();
      }
    }
    return null;
  }

  public static class RecyclableBitmapDrawable extends BitmapDrawable implements DrawableWithCaches {

    private Bitmap bitmap;

    @SuppressWarnings("deprecation")
    public RecyclableBitmapDrawable(Bitmap bitmap) {
      super(bitmap);
      this.bitmap = bitmap;
    }

    @Override
    public void dropCaches() {
      if (bitmap != null) {
        bitmap.recycle();
        bitmap = null;
      }
    }
  }
}
