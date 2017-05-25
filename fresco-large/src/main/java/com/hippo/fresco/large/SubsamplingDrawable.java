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
 * Created by Hippo on 5/24/2017.
 */

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.SparseArray;
import com.facebook.common.references.CloseableReference;
import com.facebook.drawable.base.DrawableWithCaches;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class SubsamplingDrawable extends Drawable implements DrawableWithCaches {

  private static final boolean DEBUG = true;

  private ImageRegionDecoder decoder;
  private DecoderReleaser releaser;

  private final int width;
  private final int height;
  private final Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);

  private final Matrix matrix = new Matrix();
  private final float[] matrixValue = new float[9];
  private boolean matrixValueDirty = true;
  private final Matrix invertedMatrix = new Matrix();
  private boolean invertedMatrixDirty = true;
  private final Matrix tempMatrix = new Matrix();

  private int windowWidth;
  private int windowHeight;
  private int windowOffsetX;
  private int windowOffsetY;

  // The visible rect of the image
  private RectF visibleRectF = new RectF();
  private Rect visibleRect = new Rect();

  // The max width and height for tile
  private int maxTileSize = 512;

  // Sample for current rendered image
  private int currentSample;
  // Sample for image fill windows
  private int fullSample;
  private final SparseArray<List<Tile>> tilesMap = new SparseArray<>();

  private RectF debugRectF;
  private Paint debugPaint;

  public SubsamplingDrawable(CloseableReference<ImageRegionDecoder> decoderReference) {
    this.decoder = decoderReference.get();

    releaser = new DecoderReleaser(decoderReference);
    releaser.obtain();

    width = decoder.getWidth();
    height = decoder.getHeight();

    if (DEBUG) {
      debugRectF = new RectF();
      debugPaint = new Paint();
      debugPaint.setStyle(Paint.Style.STROKE);
      debugPaint.setStrokeWidth(3);
      debugPaint.setColor(Color.RED);
    }
  }

  @Override
  protected void onBoundsChange(Rect bounds) {
    windowWidth = bounds.width();
    windowHeight = bounds.height();
    windowOffsetX = bounds.left;
    windowOffsetY = bounds.top;
    ensureFullTiles();
  }

  private static int calculateSample(int scaleX, int scaleY) {
    int sample = Math.max(scaleX, scaleY);
    sample = Math.max(1, sample);
    return prevPow2(sample);
  }

  private void ensureFullTiles() {
    if (windowWidth <= 0 || windowHeight <= 0 || maxTileSize <= 0) {
      // Not ready
      return;
    }

    // Get the sample to fill window
    int fullSample = calculateSample(width / windowWidth, height / windowHeight);
    if (this.fullSample == fullSample) {
      // full sample is still the same
      return;
    }
    this.fullSample = fullSample;

    // Get the tile list to fill window
    List<Tile> fullTileList = tilesMap.get(fullSample);
    if (fullTileList == null) {
      fullTileList = createTileList(fullSample);
      tilesMap.put(fullSample, fullTileList);
    }

    // Ensure the fill-window tiles list loaded
    for (Tile tile : fullTileList) {
      tile.load();
    }

    gc();
  }

  private void gc() {
    for (int i = 0, len = tilesMap.size(); i < len; i++) {
      final int sample = tilesMap.keyAt(i);
      final List<Tile> list = tilesMap.valueAt(i);
      if (list == null) {
        continue;
      }

      if (sample == fullSample) {
        // Always keep it
      } else if (sample == currentSample) {
        // Only recycle invisible tiles for current sample
        for (Tile tile : list) {
          if (!tile.isVisible()) {
            tile.recycle();
          }
        }
      } else {
        // Recycle all tiles for all the other samples
        for (Tile tile : list) {
          tile.recycle();
        }
      }
    }
  }

  // Creates a tile list for the sample, the rect of each tile is filled
  private List<Tile> createTileList(int sample) {
    int step = maxTileSize * sample;
    List<Tile> list = new ArrayList<>(ceilDiv(width, step) * ceilDiv(height, step));

    for (int y = 0; y < height; y += step) {
      for (int x = 0; x < width; x += step) {
        int w = Math.min(step, width - x);
        int h = Math.min(step, height - y);
        Rect rect = new Rect(x, y, x + w, y + h);
        Tile tile = new Tile(sample, rect);
        list.add(tile);
      }
    }

    return list;
  }

  private float[] getMatrixValue() {
    if (matrixValueDirty) {
      matrixValueDirty = false;
      matrix.getValues(matrixValue);
    }
    return matrixValue;
  }

  private Matrix getInvertedMatrix() {
    if (invertedMatrixDirty) {
      invertedMatrixDirty = false;
      matrix.invert(invertedMatrix);
    }
    return invertedMatrix;
  }

  private int getCurrentSample() {
    float[] matrixValue = getMatrixValue();
    float scale = Math.min(matrixValue[Matrix.MSCALE_X], matrixValue[Matrix.MSCALE_Y]);
    int scaleInt = Math.max(1, (int) scale);
    currentSample = prevPow2(scaleInt);
    return currentSample;
  }

  private Rect getVisibleRect() {
    visibleRectF.set(windowOffsetX, windowOffsetY,
        windowOffsetX + windowWidth, windowOffsetY + windowHeight);
    Matrix matrix = getInvertedMatrix();
    matrix.mapRect(visibleRectF);

    visibleRectF.roundOut(visibleRect);
    if (!visibleRect.intersect(0, 0, width, height)) {
      visibleRect.setEmpty();
    }
    return visibleRect;
  }

  private void drawCurrentTiles(Canvas canvas) {

    // TODO Draw the tile in full tile list if it is not loaded.

    // Get current sample
    int currentSample = getCurrentSample();

    // Get tile list for current sample
    List<Tile> currentTileList = tilesMap.get(currentSample);
    if (currentTileList == null) {
      currentTileList = createTileList(currentSample);
      tilesMap.put(currentSample, currentTileList);
    }

    // Get visible rect in the image
    Rect visibleRect = getVisibleRect();

    for (Tile tile : currentTileList) {
      if (tile.updateVisibility(visibleRect)) {
        tile.load();
        tile.draw(canvas, paint, matrix, tempMatrix);
      }
    }

    gc();
  }

  @Override
  public void draw(@Nonnull Canvas canvas) {

    // TODO Only draw it if the full tile list is decode

    if (windowWidth <= 0 || windowHeight <= 0 || maxTileSize <= 0) {
      // Not ready
      return;
    }

    drawCurrentTiles(canvas);
  }

  @Override
  public int getIntrinsicWidth() {
    return width;
  }

  @Override
  public int getIntrinsicHeight() {
    return height;
  }

  @Override
  public void setAlpha(int alpha) {
    paint.setAlpha(alpha);
  }

  @Override
  public void setColorFilter(ColorFilter colorFilter) {
    paint.setColorFilter(colorFilter);
  }

  @Override
  public int getOpacity() {
    // Always return PixelFormat.TRANSLUCENT
    return PixelFormat.TRANSLUCENT;
  }

  @Override
  public void dropCaches() {
    close();
  }

  public void close() {
    releaser.release();

    for (int i = 0, len = tilesMap.size(); i < len; i++) {
      final List<Tile> list = tilesMap.valueAt(i);
      if (list == null) {
        continue;
      }

      // Close all tiles
      for (Tile tile : list) {
        tile.close();
      }
    }
  }

  private static class DecoderReleaser {

    private CloseableReference<ImageRegionDecoder> decoderReference;
    private int reference;

    private DecoderReleaser(CloseableReference<ImageRegionDecoder> decoderReference) {
      this.decoderReference = decoderReference;
    }

    private void obtain() {
      ++reference;
    }

    private void release() {
      if (--reference == 0) {
        decoderReference.close();
      }
    }
  }

  private class Tile {
    public int sample;
    public Rect rect;
    public Bitmap bitmap;
    // The task to decode image
    private LoadingTask task;
    //
    private boolean visible;
    // True if can't decode the source
    // Check this flag to avoid infinity loading
    private boolean failed;

    public Tile(int sample, Rect rect) {
      this.sample = sample;
      this.rect = rect;

      releaser.obtain();
    }

    /**
     * Update the visibility according to the visible rect in the image.
     * Returns {@code true} if it's visible.
     */
    public boolean updateVisibility(Rect visibleRect) {
      visible = Rect.intersects(visibleRect, rect);
      return visible;
    }

    /**
     * Returns {@code true} if it's visible.
     */
    public boolean isVisible() {
      return visible;
    }

    /**
     * Starts a task to decode the image.
     */
    public void load() {
      if (bitmap == null && task == null && !failed && releaser != null) {
        task = new LoadingTask();
        task.execute();
      }
    }

    /**
     * Draws the tile.
     */
    public void draw(Canvas canvas, Paint paint, Matrix matrix, Matrix temp) {
      if (bitmap != null) {
        temp.set(matrix);
        temp.preTranslate(rect.left, rect.top);
        canvas.drawBitmap(bitmap, temp, paint);

        if (DEBUG) {
          temp.preTranslate(-rect.left, -rect.top);
          debugRectF.set(rect);
          temp.mapRect(debugRectF);
          canvas.drawRect(debugRectF, debugPaint);
        }
      }
    }

    /**
     * Cancels loading task, recycles the bitmap.
     */
    public void recycle() {
      if (task != null) {
        task.cancel(false);
        task = null;
      }
      if (bitmap != null) {
        bitmap.recycle();
        bitmap = null;
      }
    }

    /**
     * Calls {@link #recycle()}. {@link #load()} will not work anymore.
     */
    public void close() {
      recycle();
      releaser.release();
    }

    private void onLoaded(Bitmap bitmap) {
      this.bitmap = bitmap;
      this.task = null;
      this.failed = bitmap == null;

      if (bitmap != null && visible && sample == currentSample) {
        invalidateSelf();
      }
    }

    private class LoadingTask extends AsyncTask<Void, Void, Bitmap> {
      @Override
      protected void onPreExecute() {
        releaser.obtain();
      }

      @Override
      protected Bitmap doInBackground(Void... params) {
        if (!isCancelled()) {
          return decoder.decode(rect);
        } else {
          return null;
        }
      }

      @Override
      protected void onPostExecute(Bitmap bitmap) {
        releaser.release();
        onLoaded(bitmap);
      }

      @Override
      protected void onCancelled(Bitmap bitmap) {
        releaser.release();
        // The cleanup task is done in recycle(), just recycle the bitmap
        if (bitmap != null) {
          bitmap.recycle();
        }
      }
    }
  }

  private static int ceilDiv(int a, int b) {
    return (a + b - 1) / b;
  }

  private static int prevPow2(int n) {
    n |= n >> 1;
    n |= n >> 2;
    n |= n >> 4;
    n |= n >> 8;
    n |= n >> 16;
    return n - (n >> 1);
  }
}
