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
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imageformat.DefaultImageFormats;
import com.facebook.imageformat.ImageFormat;
import com.facebook.imagepipeline.common.ImageDecodeOptions;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.decoder.DefaultImageDecoder;
import com.facebook.imagepipeline.decoder.ImageDecoder;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.image.EncodedImage;
import com.facebook.imagepipeline.image.QualityInfo;
import java.lang.reflect.Field;
import java.util.Map;
import javax.annotation.Nullable;

public class LargeImageDecoder implements ImageDecoder {

  private DefaultImageDecoder defaultImageDecoder;
  private boolean hasCheckedDefaultImageDecoder;
  private final Object checkingDefaultImageDecoderLock = new Object();

  private final ImageSizeDecoder defaultSizeDecoder;

  @Nullable
  private final Map<ImageFormat, ImageSizeDecoder> sizeDecoderMap;
  private final Map<ImageFormat, ImageRegionDecoderFactory> regionDecoderFactoryMap;
  @Nullable
  private final Map<ImageFormat, ImageDecoder> imageDecoderMap;
  private final int thresholdWidth;
  private final int thresholdHeight;

  public LargeImageDecoder(
      Map<ImageFormat, ImageSizeDecoder> sizeDecoderMap,
      Map<ImageFormat, ImageRegionDecoderFactory> regionDecoderFactoryMap,
      Map<ImageFormat, ImageDecoder> imageDecoderMap,
      int thresholdWidth,
      int thresholdHeight) {
    this.sizeDecoderMap = sizeDecoderMap;
    this.regionDecoderFactoryMap = regionDecoderFactoryMap;
    this.imageDecoderMap = imageDecoderMap;
    this.thresholdWidth = thresholdWidth;
    this.thresholdHeight = thresholdHeight;

    this.defaultSizeDecoder = new DefaultImageSizeDecoder();
  }

  private DefaultImageDecoder requireDefaultImageDecoder() {
    if (!hasCheckedDefaultImageDecoder) {
      synchronized (checkingDefaultImageDecoderLock) {
        // Double check
        if (!hasCheckedDefaultImageDecoder) {
          hasCheckedDefaultImageDecoder = true;

          try {
            Field field = ImagePipelineFactory.class.getDeclaredField("mImageDecoder");
            field.setAccessible(true);

            ImagePipelineFactory factory = Fresco.getImagePipelineFactory();
            ImageDecoder imageDecoder = (ImageDecoder) field.get(factory);
            if (imageDecoder instanceof DefaultImageDecoder) {
              defaultImageDecoder = (DefaultImageDecoder) imageDecoder;
            }
          } catch (Exception e) {
            // Ignore
          }
        }
      }
    }
    return defaultImageDecoder;
  }

  private boolean isLargeEnough(int width, int height) {
    return width > thresholdWidth || height > thresholdHeight;
  }

  @Override
  public CloseableImage decode(EncodedImage encodedImage, int length, QualityInfo qualityInfo,
      ImageDecodeOptions options) {
    ImageFormat imageFormat = encodedImage.getImageFormat();

    ImageSizeDecoder sizeDecoder = null;
    if (sizeDecoderMap != null) {
      sizeDecoder = sizeDecoderMap.get(imageFormat);
    }
    if (sizeDecoder == null) {
      sizeDecoder = this.defaultSizeDecoder;
    }

    Pair<Integer, Integer> size = sizeDecoder.decode(encodedImage, length);
    if (size != null && isLargeEnough(size.first, size.second)) {
      ImageRegionDecoderFactory factory = regionDecoderFactoryMap.get(imageFormat);
      if (factory != null) {
        ImageRegionDecoder decoder =
            factory.createImageRegionDecoder(encodedImage, length, qualityInfo, options);
        if (decoder != null) {
          return new ClosableLargeImage(decoder);
        }
      }
    }

    if (imageDecoderMap != null) {
      ImageDecoder imageDecoder = imageDecoderMap.get(imageFormat);
      if (imageDecoder != null) {
        return imageDecoder.decode(encodedImage, length, qualityInfo, options);
      }
    }

    DefaultImageDecoder defaultImageDecoder = requireDefaultImageDecoder();
    if (defaultImageDecoder != null) {
      if (imageFormat == DefaultImageFormats.JPEG) {
        return defaultImageDecoder.decodeJpeg(encodedImage, length, qualityInfo, options);
      } else if (imageFormat == DefaultImageFormats.GIF) {
        return defaultImageDecoder.decodeGif(encodedImage, options);
      } else if (imageFormat == DefaultImageFormats.WEBP_ANIMATED) {
        return defaultImageDecoder.decodeAnimatedWebp(encodedImage, options);
      }
      return defaultImageDecoder.decodeStaticImage(encodedImage, options);
    }

    return null;
  }
}
