// -*- mode: java; c-basic-offset: 2; -*-
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0


package edu.mills.lightsensor;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.*;

import edu.mills.envsensor.EnvSensor;

import android.content.Context;
import android.hardware.Sensor;

import java.util.List;

/**
 * Physical world component that can measure the light level.
 * It is implemented using android.hardware.SensorListener
 * (http://developer.android.com/reference/android/hardware/SensorListener.html).
 */
@DesignerComponent(version = 1,
                   description = "Non-visible component that can measure the light level. Icon made by Freepik from www.flaticon.com",
    category = ComponentCategory.EXTENSION,
    nonVisible = true,
    iconName = "aiwebres/brightness.png")
@SimpleObject(external=true)
public class LightSensor extends EnvSensor {
  /**
   * Creates a new LightSensor component.
   *
   * @param container  ignored (because this is a non-visible component)
   */
  public LightSensor(ComponentContainer container) {
    super(container.$form(), Sensor.TYPE_LIGHT);
  }

  /**
   * If true, the sensor will generate events.  Otherwise, no events
   * are generated.
   *
   * @return {@code true} indicates that the sensor generates events,
   *         {@code false} that it doesn't
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public boolean Enabled() {
    return enabled;
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
    setEnabled(enabled);
  }

  /**
   * Indicates whether the sensor is available.
   *
   * @return {@code true} if available, {@code false} otherwise
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public boolean Available() {
    return isAvailable();
  }

  @Override
  protected void valueChanged(float value) {
    LightChanged(value);
  }

  /**
   * Indicates the light level changed.
   */
  @SimpleEvent
  public void LightChanged(float lux) {
    EventDispatcher.dispatchEvent(this, "LightChanged", lux);
  }

  /**
   * Returns the average of the last ten lux values.
   * The sensor must be enabled to return meaningful values.
   *
   * @return lux
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public float Lux() {
    return getAverageValue();
  }
}
