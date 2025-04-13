/*
 * Copyright Â© 2023 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.wrangler.api.parser;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.cdap.wrangler.api.annotations.PublicEvolving;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents a time duration value with associated unit (ms, s, m, h, d).
 * It parses string representations like "150ms", "2.5s", etc. and provides methods to
 * retrieve the canonical value in nanoseconds.
 */
@PublicEvolving
public class TimeDuration implements Token {
  private static final Pattern PATTERN = Pattern.compile("^([0-9]+\\.?[0-9]*)\\s*([mshd]{1,2})$");
  
  private final String rawValue;
  private final double value;
  private final String unit;
  private final long nanoseconds;

  /**
   * Constructor for creating a TimeDuration token from a string representation.
   *
   * @param value String representation of time duration (e.g., "150ms", "2.5s", "1h")
   * @throws IllegalArgumentException if the string does not match expected format
   */
  public TimeDuration(String value) {
    this.rawValue = value;
    
    Matcher matcher = PATTERN.matcher(value.trim());
    if (!matcher.matches()) {
      throw new IllegalArgumentException(
        "Invalid time duration format. Expected format: number followed by unit (ms, s, m, h, d)");
    }
    
    this.value = Double.parseDouble(matcher.group(1));
    this.unit = matcher.group(2);
    this.nanoseconds = convertToNanos(this.value, this.unit);
  }

  /**
   * Returns the numeric value from the parsed string.
   *
   * @return numeric value without units
   */
  public double getValue() {
    return value;
  }

  /**
   * Returns the unit part from the parsed string.
   *
   * @return unit string (ms, s, m, h, or d)
   */
  public String getUnit() {
    return unit;
  }

  /**
   * Returns the canonical representation in nanoseconds.
   *
   * @return value converted to nanoseconds
   */
  public long getNanoseconds() {
    return nanoseconds;
  }

  /**
   * Returns the time duration in milliseconds.
   *
   * @return value in milliseconds
   */
  public long getMilliseconds() {
    return nanoseconds / 1_000_000;
  }

  /**
   * Returns the time duration in seconds.
   *
   * @return value in seconds
   */
  public double getSeconds() {
    return nanoseconds / 1_000_000_000.0;
  }

  /**
   * Converts the given value with unit to nanoseconds.
   *
   * @param value numeric value
   * @param unit  string unit (ms, s, m, h, or d)
   * @return the value in nanoseconds
   */
  private long convertToNanos(double value, String unit) {
    switch (unit) {
      case "ms":
        return (long) (value * 1_000_000);
      case "s":
        return (long) (value * 1_000_000_000);
      case "m":
        return (long) (value * 60 * 1_000_000_000);
      case "h":
        return (long) (value * 60 * 60 * 1_000_000_000);
      case "d":
        return (long) (value * 24 * 60 * 60 * 1_000_000_000);
      default:
        throw new IllegalArgumentException("Unsupported time duration unit: " + unit);
    }
  }

  /**
   * Returns the raw string representation of this token.
   *
   * @return raw string value
   */
  @Override
  public Object value() {
    return rawValue;
  }

  /**
   * Returns the token type.
   *
   * @return TIME_DURATION token type
   */
  @Override
  public TokenType type() {
    return TokenType.TIME_DURATION;
  }

  /**
   * Converts this token to a JSON representation.
   *
   * @return JSON representation of this token
   */
  @Override
  public JsonElement toJson() {
    JsonObject object = new JsonObject();
    object.addProperty("type", type().name());
    object.addProperty("value", rawValue);
    object.addProperty("nanoseconds", nanoseconds);
    return object;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TimeDuration that = (TimeDuration) o;
    return Double.compare(that.value, value) == 0 &&
           nanoseconds == that.nanoseconds &&
           Objects.equals(rawValue, that.rawValue) &&
           Objects.equals(unit, that.unit);
  }

  @Override
  public int hashCode() {
    return Objects.hash(rawValue, value, unit, nanoseconds);
  }

  @Override
  public String toString() {
    return rawValue;
  }
}
