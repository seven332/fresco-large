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

package com.hippo.fresco.large;

/*
 * Created by Hippo on 5/22/2017.
 */

import com.facebook.common.references.CloseableReference;
import com.facebook.imagepipeline.image.CloseableImage;
import javax.annotation.Nullable;

public class ClosableLargeImage extends CloseableImage {

  private ImageRegionDecoder decoder;
  private CloseableReference<ImageRegionDecoder> decoderReference;
  private int width;
  private int height;

  public ClosableLargeImage(ImageRegionDecoder decoder) {
    this.decoder = decoder;
    this.decoderReference = CloseableReference.of(decoder);
    this.width = decoder.getWidth();
    this.height = decoder.getHeight();
  }

  @Nullable
  public CloseableReference<ImageRegionDecoder> getDecoder() {
    return decoderReference.cloneOrNull();
  }

  @Override
  public int getSizeInBytes() {
    return 0;
  }

  @Override
  public void close() {
    CloseableReference<ImageRegionDecoder> reference = detachDecoderReference();
    if (reference != null) {
      reference.close();
    }
  }

  private synchronized CloseableReference<ImageRegionDecoder> detachDecoderReference() {
    CloseableReference<ImageRegionDecoder> reference = decoderReference;
    decoderReference = null;
    decoder = null;
    return reference;
  }

  @Override
  public boolean isClosed() {
    return decoder == null;
  }

  @Override
  public int getWidth() {
    return width;
  }

  @Override
  public int getHeight() {
    return height;
  }
}
