// -*- mode: java; c-basic-offset: 2; -*-
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0


package edu.mills.lightsensor;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.*;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.os.Handler;

import android.view.Surface;
import android.view.WindowManager;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Physical world component that can measure the light level.
 * It is implemented using
 * android.hardware.SensorListener
 * (http://developer.android.com/reference/android/hardware/SensorListener.html).
 */
@DesignerComponent(version = 1,
                   description = "Non-visible component that can measure the light level. Icon made by Freepik from www.flaticon.com",
    category = ComponentCategory.EXTENSION,
    nonVisible = true,
    iconName = "aiwebres/brightness.png")
@SimpleObject(external=true)
public class LightSensor extends AndroidNonvisibleComponent
    implements OnStopListener, OnResumeListener, SensorComponent, SensorEventListener, Deleteable {

  // Backing for sensor values
  private AveragingBuffer buffer;
  private static final int BUFFER_SIZE = 10;

  private int accuracy;

  private final SensorManager sensorManager;

  private final WindowManager windowManager;

  // Indicates whether the sensor should generate events
  private boolean enabled;

  private Sensor sensor;

  /**
   * Creates a new LightSensor component.
   *
   * @param container  ignored (because this is a non-visible component)
   */
  public LightSensor(ComponentContainer container) {
    super(container.$form());
    form.registerForOnResume(this);
    form.registerForOnStop(this);

    enabled = true;
    windowManager = (WindowManager) container.$context().getSystemService(Context.WINDOW_SERVICE);
    sensorManager = (SensorManager) container.$context().getSystemService(Context.SENSOR_SERVICE);
    sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
    buffer = new AveragingBuffer(BUFFER_SIZE);
    startListening();
  }


  /**
   * Indicates the light level changed.
   */
  @SimpleEvent
  public void LightChanged(float lux) {
    EventDispatcher.dispatchEvent(this, "LightChanged", lux);
  }

  /**
   * Available property getter method (read-only property).
   *
   * @return {@code true} indicates that a light sensor is available,
   *         {@code false} that it isn't
   */
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR)
  public boolean Available() {
    List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_LIGHT);
    return sensors.size() > 0;
  }

  /**
   * If true, the sensor will generate events.  Otherwise, no events
   * are generated.
   *
   * @return {@code true} indicates that the sensor generates events,
   *         {@code false} that it doesn't
   */
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR)
  public boolean Enabled() {
    return enabled;
  }

  // Assumes that sensorManager has been initialized, which happens in constructor
  private void startListening() {
      sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
  }

  // Assumes that sensorManager has been initialized, which happens in constructor
  private void stopListening() {
    sensorManager.unregisterListener(this);
  }

  /**
   * Specifies whether the sensor should generate events.  If true,
   * the sensor will generate events.  Otherwise, no events are
   * generated.
   *
   * @param enabled  {@code true} enables sensor event generation,
   *                 {@code false} disables it
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty
  public void Enabled(boolean enabled) {
    if (this.enabled == enabled) {
      return;
    }
    this.enabled = enabled;
    if (enabled) {
      startListening();
    } else {
      stopListening();
    }
  }

  /**
   * Returns the average of the last ten lux values.
   * The sensor must be enabled to return meaningful values.
   *
   * @return lux
   */
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR)
  public float Lux() {
      return buffer.getAverage();
  }

  // SensorListener implementation
  @Override
  public void onSensorChanged(SensorEvent sensorEvent) {
    if (enabled && sensorEvent.sensor.getType() == Sensor.TYPE_LIGHT) {
      accuracy = sensorEvent.accuracy;
      final float[] values = sensorEvent.values;
      buffer.insert(values[0]);
      LightChanged(values[0]);
    }
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {
  }
    
  @Override
  public void onResume() {
    if (enabled) {
      startListening();
    }
  }

  @Override
  public void onStop() {
    if (enabled) {
      stopListening();
    }
  }

  @Override
  public void onDelete() {
    if (enabled) {
      stopListening();
    }
  }

  private class AveragingBuffer {
    private Float[] data;
    private int next;

    private AveragingBuffer(int size) {
      data = new Float[size];
      next = 0;
    }

    private void insert(Float datum) {
      data[next++] = datum;
      if (next == data.length) {
        next = 0;
      }
    }

    private float getAverage() {
      double sum = 0;
      int count = 0;

      for (int i = 0; i < data.length; i++) {
        if (data[i] != null) {
          sum += data[i];
          count++;
        }
      }

      return (float) (count == 0 ? sum : sum / count);
    }
  }
}
