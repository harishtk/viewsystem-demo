package com.example.viewsystem.commons.util;

import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.viewsystem.commons.util.logging.Log;


public class WakeLockUtil {

  private static final String TAG = Log.tag(WakeLockUtil.class);

  /**
   * Run a runnable with a wake lock. Ensures that the lock is safely acquired and released.
   *
   * @param tag will be prefixed with "signal:" if it does not already start with it.
   */
  public static void runWithLock(@NonNull Context context, int lockType, long timeout, @NonNull String tag, @NonNull Runnable task) {
    WakeLock wakeLock = null;
    try {
      wakeLock = acquire(context, lockType, timeout, tag);
      task.run();
    } finally {
      if (wakeLock != null) {
        release(wakeLock, tag);
      }
    }
  }

  /**
   * @param tag will be prefixed with "signal:" if it does not already start with it.
   */
  public static WakeLock acquire(@NonNull Context context, int lockType, long timeout, @NonNull String tag) {
    tag = prefixTag(tag);
    try {
      PowerManager powerManager = ServiceUtil.getPowerManager(context);
      WakeLock wakeLock     = powerManager.newWakeLock(lockType, tag);

      wakeLock.acquire(timeout);

      return wakeLock;
    } catch (Exception e) {
      android.util.Log.w(TAG, "Failed to acquire wakelock with tag: " + tag, e);
      return null;
    }
  }

  /**
   * @param tag will be prefixed with "signal:" if it does not already start with it.
   */
  public static void release(@Nullable WakeLock wakeLock, @NonNull String tag) {
    tag = prefixTag(tag);
    try {
      if (wakeLock == null) {
        android.util.Log.d(TAG, "Wakelock was null. Skipping. Tag: " + tag);
      } else if (wakeLock.isHeld()) {
        wakeLock.release();
      } else {
        android.util.Log.d(TAG, "Wakelock wasn't held at time of release: " + tag);
      }
    } catch (Exception e) {
      android.util.Log.w(TAG, "Failed to release wakelock with tag: " + tag, e);
    }
  }

  private static String prefixTag(@NonNull String tag) {
    return tag.startsWith("signal:") ? tag : "signal:" + tag;
  }
}
