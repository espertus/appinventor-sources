// -*- mode: java; c-basic-offset: 2; -*-
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package edu.mills.envsensor;
/*
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
*/
import com.google.appinventor.components.runtime.*;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;

import java.util.List;

public abstract class EnvSensor extends AndroidNonvisibleComponent
    implements OnStopListener, OnResumeListener, SensorComponent, SensorEventListener, Deleteable {
  private static final int BUFFER_SIZE = 10; // number of values to average
  private final SensorManager sensorManager;
  // Must be a value of Sensor.Type_* corresponding to a single-value sensor
  private final int sensorType;
  private Sensor sensor;
  protected boolean enabled;
  private AveragingBuffer buffer; // for buffering sensor values

  protected EnvSensor(ComponentContainer container, int sensorType) {
    super(container.$form());
    form.registerForOnResume(this);
    form.registerForOnStop(this);

    enabled = true;
    sensorManager = (SensorManager) container.$context().getSystemService(Context.SENSOR_SERVICE);
    sensor = sensorManager.getDefaultSensor(sensorType);
    this.sensorType = sensorType;
    buffer = new AveragingBuffer(BUFFER_SIZE);
    startListening();
  }

  protected boolean isAvailable() {
    List<Sensor> sensors = sensorManager.getSensorList(sensorType);
    return sensors.size() > 0;
  }

  protected float getAverageValue() {
    return buffer.getAverage();
  }

  private void startListening() {
    // If faster is needed, change to SENSOR_DELAY_FASTEST
    sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
  }

  private void stopListening() {
    sensorManager.unregisterListener(this);
  }
  protected void setEnabled(boolean enabled) {
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

  protected abstract void valueChanged(float value);

  // SensorListener implementation
  @Override
  public void onSensorChanged(SensorEvent sensorEvent) {
    if (enabled && sensorEvent.sensor.getType() == sensorType) {
      final float[] values = sensorEvent.values;
      buffer.insert(values[0]);
      valueChanged(values[0]);
    }
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    
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
