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

import com.facebook.common.logging.FLog;
import com.facebook.drawee.backends.pipeline.DraweeConfig;
import com.facebook.imageformat.ImageFormat;
import com.facebook.imagepipeline.decoder.ImageDecoderConfig;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

public final class FrescoLarge {
  private FrescoLarge() {}

  private static final Class<?> TAG = FrescoLarge.class;

  public static void config(@Nonnull ImageDecoderConfig.Builder decoderConfigBuilder,
      @Nonnull DraweeConfig.Builder draweeConfigBuilder, @Nonnull FrescoLargeConfig config) {
    Set<ImageFormat> imageFormatSet = config.getImageFormatSet();
    if (imageFormatSet == null || imageFormatSet.isEmpty()) {
      FLog.w(TAG, "No ImageFormat");
      return;
    }

    Map<ImageFormat, ImageRegionDecoderFactory> regionDecoderFactoryMap =
        config.getImageRegionDecoderFactoryMap();
    if (regionDecoderFactoryMap == null || regionDecoderFactoryMap.isEmpty()) {
      FLog.w(TAG, "No ImageRegionDecoderFactory");
      return;
    }

    LargeDrawableFactory factory = config.getLargeDrawableFactory();
    if (factory == null) {
      FLog.w(TAG, "No LargeDrawableFactory");
      return;
    }

    Map<ImageFormat, ImageFormat.FormatChecker> imageFormatCheckerMap =
        config.getImageFormatCheckerMap();
    if (imageFormatCheckerMap != null) {
      for (Map.Entry<ImageFormat, ImageFormat.FormatChecker> entry :
          config.getImageFormatCheckerMap().entrySet()) {
        // Apply image format checker.
        // Pass null for ImageDecoder, it should be override by LargeImageDecoder
        decoderConfigBuilder.addDecodingCapability(entry.getKey(), entry.getValue(), null);
      }
    }

    LargeImageDecoder largeImageDecoder = new LargeImageDecoder(config.getImageSizeDecoderMap(),
        config.getImageRegionDecoderFactoryMap(), config.getImageDecoderMap(),
        config.getThresholdWidth(), config.getThresholdHeight());
    for (ImageFormat imageFormat : config.getImageFormatSet()) {
      decoderConfigBuilder.overrideDecoder(imageFormat, largeImageDecoder);
    }

    draweeConfigBuilder.addCustomDrawableFactory(config.getLargeDrawableFactory());
  }
}
