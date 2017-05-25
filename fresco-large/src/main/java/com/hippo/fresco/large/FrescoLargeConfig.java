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

import com.facebook.imageformat.ImageFormat;
import com.facebook.imagepipeline.decoder.ImageDecoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FrescoLargeConfig {

  private final Set<ImageFormat> imageFormatSet;
  private final Map<ImageFormat, ImageFormat.FormatChecker> formatCheckerMap;
  private final Map<ImageFormat, ImageSizeDecoder> sizeDecoderMap;
  private final Map<ImageFormat, ImageRegionDecoderFactory> regionDecoderFactoryMap;
  private final Map<ImageFormat, ImageDecoder> imageDecoderMap;
  private LargeDrawableFactory largeDrawableFactory;
  private final int thresholdWidth;
  private final int thresholdHeight;

  public FrescoLargeConfig(Builder builder) {
    imageFormatSet = builder.imageFormatSet;
    formatCheckerMap = builder.formatCheckerMap;
    sizeDecoderMap = builder.sizeDecoderMap;
    regionDecoderFactoryMap = builder.regionDecoderFactoryMap;
    imageDecoderMap = builder.imageDecoderMap;
    largeDrawableFactory = builder.largeDrawableFactory;
    thresholdWidth = builder.thresholdWidth;
    thresholdHeight = builder.thresholdHeight;
  }

  public Set<ImageFormat> getImageFormatSet() {
    return imageFormatSet;
  }

  public Map<ImageFormat, ImageFormat.FormatChecker> getImageFormatCheckerMap() {
    return formatCheckerMap;
  }

  public Map<ImageFormat, ImageSizeDecoder> getImageSizeDecoderMap() {
    return sizeDecoderMap;
  }

  public Map<ImageFormat, ImageRegionDecoderFactory> getImageRegionDecoderFactoryMap() {
    return regionDecoderFactoryMap;
  }

  public Map<ImageFormat, ImageDecoder> getImageDecoderMap() {
    return imageDecoderMap;
  }

  public LargeDrawableFactory getLargeDrawableFactory() {
    if (largeDrawableFactory == null) {
      largeDrawableFactory = new SubsamplingDrawableFactory();
    }
    return largeDrawableFactory;
  }

  public int getThresholdWidth() {
    return thresholdWidth;
  }

  public int getThresholdHeight() {
    return thresholdHeight;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private Set<ImageFormat> imageFormatSet;
    private Map<ImageFormat, ImageFormat.FormatChecker> formatCheckerMap;
    private Map<ImageFormat, ImageSizeDecoder> sizeDecoderMap;
    private Map<ImageFormat, ImageRegionDecoderFactory> regionDecoderFactoryMap;
    private Map<ImageFormat, ImageDecoder> imageDecoderMap;
    private LargeDrawableFactory largeDrawableFactory;
    private int thresholdWidth;
    private int thresholdHeight;

    public FrescoLargeConfig.Builder addDecoder(@Nonnull ImageFormat imageFormat,
        @Nonnull ImageRegionDecoderFactory imageRegionDecoderFactory) {
      return addDecoder(imageFormat, null, null, imageRegionDecoderFactory, null);
    }

    public FrescoLargeConfig.Builder addDecoder(@Nonnull ImageFormat imageFormat,
        @Nullable ImageFormat.FormatChecker imageFormatChecker,
        @Nullable ImageSizeDecoder imageSizeDecoder,
        @Nonnull ImageRegionDecoderFactory imageRegionDecoderFactory,
        @Nullable ImageDecoder imageDecoder) {
      if (imageFormatSet == null) {
        imageFormatSet = new HashSet<>();
      }
      imageFormatSet.add(imageFormat);

      if (imageFormatChecker != null) {
        if (formatCheckerMap == null) {
          formatCheckerMap = new HashMap<>();
        }
        formatCheckerMap.put(imageFormat, imageFormatChecker);
      }

      if (imageSizeDecoder != null) {
        if (sizeDecoderMap == null) {
          sizeDecoderMap = new HashMap<>();
        }
        sizeDecoderMap.put(imageFormat, imageSizeDecoder);
      }

      if (regionDecoderFactoryMap == null) {
        regionDecoderFactoryMap = new HashMap<>();
      }
      regionDecoderFactoryMap.put(imageFormat, imageRegionDecoderFactory);

      if (imageDecoder != null) {
        if (imageDecoderMap == null) {
          imageDecoderMap = new HashMap<>();
        }
        imageDecoderMap.put(imageFormat, imageDecoder);
      }

      return this;
    }

    public FrescoLargeConfig.Builder setLargeDrawableFactory(LargeDrawableFactory factory) {
      largeDrawableFactory = factory;
      return this;
    }

    public FrescoLargeConfig.Builder setThresholdSize(int width, int height) {
      thresholdWidth = width;
      thresholdHeight = height;
      return this;
    }

    public FrescoLargeConfig build() {
      return new FrescoLargeConfig(this);
    }
  }
}
