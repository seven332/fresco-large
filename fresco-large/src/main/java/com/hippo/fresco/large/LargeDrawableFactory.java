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
 * Created by Hippo on 5/23/2017.
 */

import android.graphics.drawable.Drawable;
import com.facebook.drawee.backends.pipeline.DrawableFactory;
import com.facebook.imagepipeline.image.CloseableImage;
import javax.annotation.Nullable;

public abstract class LargeDrawableFactory implements DrawableFactory {

  @Override
  public final boolean supportsImageType(CloseableImage image) {
    return image instanceof ClosableLargeImage;
  }

  @Nullable
  @Override
  public final Drawable createDrawable(CloseableImage image) {
    return createLargeDrawable((ClosableLargeImage) image);
  }

  public abstract Drawable createLargeDrawable(ClosableLargeImage image);
}
