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

import static junit.framework.Assert.assertEquals;

import android.app.Application;
import android.graphics.Matrix;
import android.util.Log;
import com.facebook.drawee.backends.pipeline.DraweeConfig;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imageformat.DefaultImageFormats;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.decoder.ImageDecoderConfig;
import com.hippo.fresco.large.FrescoLarge;
import com.hippo.fresco.large.FrescoLargeConfig;
import com.hippo.fresco.large.ImageRegionDecoderFactory;
import com.hippo.fresco.large.LargeDrawableFactory;
import com.hippo.fresco.large.decoder.skia.SkiaImageRegionDecoderFactory;

public class DemoApp extends Application {

  @Override
  public void onCreate() {
    super.onCreate();

    FrescoLargeConfig.Builder builder = FrescoLargeConfig.newBuilder();
    builder.setThresholdSize(256, 256);
    ImageRegionDecoderFactory decoderFactory = new SkiaImageRegionDecoderFactory();
    builder.addDecoder(DefaultImageFormats.JPEG, decoderFactory);
    builder.addDecoder(DefaultImageFormats.PNG, decoderFactory);

    ImageDecoderConfig.Builder decoderConfigBuilder = ImageDecoderConfig.newBuilder();
    DraweeConfig.Builder draweeConfigBuilder = DraweeConfig.newBuilder();
    FrescoLarge.config(decoderConfigBuilder, draweeConfigBuilder, builder.build());

    ImagePipelineConfig imagePipelineConfig = ImagePipelineConfig
        .newBuilder(this)
        .setImageDecoderConfig(decoderConfigBuilder.build())
        .build();

    Fresco.initialize(this, imagePipelineConfig, draweeConfigBuilder.build());
  }
}
