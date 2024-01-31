package com.example.viewsystem.view.animation;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import androidx.annotation.NonNull;

public class ResizeAnimation extends Animation {
  private static final String TAG = ResizeAnimation.class.getSimpleName();

  private final View target;
  private final int  targetWidthPx;
  private final int  targetHeightPx;

  private int startWidth;
  private int startHeight;

  public ResizeAnimation(@NonNull View target, int targetWidthPx, int targetHeightPx) {
    this.target         = target;
    this.targetWidthPx  = targetWidthPx;
    this.targetHeightPx = targetHeightPx;
    Log.d(TAG, "ResizeAnimation() called with: target = [" + target + "], targetWidthPx = [" + targetWidthPx + "], targetHeightPx = [" + targetHeightPx + "]");
  }

  @Override
  protected void applyTransformation(float interpolatedTime, Transformation t) {
    int newWidth  = (int) (startWidth + (targetWidthPx - startWidth) * interpolatedTime);
    int newHeight = (int) (startHeight + (targetHeightPx - startHeight) * interpolatedTime);

    ViewGroup.LayoutParams params = target.getLayoutParams();

    params.width  = newWidth;
    params.height = newHeight;

    target.setLayoutParams(params);
  }

  @Override
  public void initialize(int width, int height, int parentWidth, int parentHeight) {
    super.initialize(width, height, parentWidth, parentHeight);
    Log.d(TAG, "initialize() called with: width = [" + width + "], height = [" + height + "], parentWidth = [" + parentWidth + "], parentHeight = [" + parentHeight + "]");

    this.startWidth  = width;
    this.startHeight = height;
  }

  @Override
  public boolean willChangeBounds() {
    return true;
  }
}
