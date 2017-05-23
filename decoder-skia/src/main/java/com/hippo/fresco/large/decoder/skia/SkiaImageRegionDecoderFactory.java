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

import com.facebook.imagepipeline.common.ImageDecodeOptions;
import com.facebook.imagepipeline.image.EncodedImage;
import com.facebook.imagepipeline.image.QualityInfo;
import com.hippo.fresco.large.ImageRegionDecoder;
import com.hippo.fresco.large.ImageRegionDecoderFactory;
import javax.annotation.Nullable;

public class SkiaImageRegionDecoderFactory implements ImageRegionDecoderFactory {

  @Nullable
  @Override
  public ImageRegionDecoder createImageRegionDecoder(EncodedImage encodedImage, int length,
      QualityInfo qualityInfo, ImageDecodeOptions options) {
    return SkipImageRegionDecoder.create(encodedImage);
  }
}
