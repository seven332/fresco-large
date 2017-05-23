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

import android.util.Pair;
import com.facebook.imagepipeline.image.EncodedImage;
import javax.annotation.Nullable;

public class DefaultImageSizeDecoder implements ImageSizeDecoder {

  @Nullable
  @Override
  public Pair<Integer, Integer> decode(EncodedImage encodedImage, int length) {
    encodedImage.parseMetaData();

    int width = encodedImage.getWidth();
    int height = encodedImage.getHeight();

    if (width >= 0 && height >= 0) {
      return new Pair<>(width, height);
    } else {
      return null;
    }
  }
}
