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

package com.hippo.fresco.large.decoder.skia;

/*
 * Created by Hippo on 5/22/2017.
 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import com.facebook.imagepipeline.image.EncodedImage;
import com.hippo.fresco.large.ImageRegionDecoder;
import java.io.IOException;
import java.io.InputStream;
import javax.annotation.Nullable;

public class SkipImageRegionDecoder implements ImageRegionDecoder {

  private BitmapRegionDecoder decoder;

  public SkipImageRegionDecoder(BitmapRegionDecoder decoder) {
    this.decoder = decoder;
  }

  @Override
  public int getWidth() {
    return decoder.getWidth();
  }

  @Override
  public int getHeight() {
    return decoder.getHeight();
  }

  @Nullable
  @Override
  public Bitmap decode(Rect rect) {
    return decoder.decodeRegion(rect, null);
  }

  @Override
  public void close() {
    decoder.recycle();
  }

  @Nullable
  public static SkipImageRegionDecoder create(EncodedImage encodedImage) {
    InputStream is = encodedImage.getInputStream();
    if (is != null) {
      try {
        BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(is, false);
        if (decoder != null) {
          return new SkipImageRegionDecoder(decoder);
        }
      } catch (IOException e) {
        // Ignore
      }
    }
    return null;
  }
}
