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
import com.google.gson.JsonPrimitive;
import io.cdap.wrangler.api.annotations.PublicEvolving;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents a byte size value with associated unit (B, KB, MB, GB, TB, PB).
 * It parses string representations like "10KB", "1.5MB", etc. and provides methods to
 * retrieve the canonical value in bytes.
 */
@PublicEvolving
public class ByteSize implements Token {
  private static final Pattern PATTERN = Pattern.compile("^([0-9]+\\.?[0-9]*)\\s*([kKmMgGtTpP]?[bB])$");
  
  private final String rawValue;
  private final double value;
  private final String unit;
  private final long bytes;

  /**
   * Constructor for creating a ByteSize token from a string representation.
   *
   * @param value String representation of byte size (e.g., "10KB", "1.5MB", "5B")
   * @throws IllegalArgumentException if the string does not match expected format
   */
  public ByteSize(String value) {
    this.rawValue = value;
    
    Matcher matcher = PATTERN.matcher(value.trim());
    if (!matcher.matches()) {
      throw new IllegalArgumentException(
        "Invalid byte size format. Expected format: number followed by unit (B, KB, MB, GB, TB, PB)");
    }
    
    this.value = Double.parseDouble(matcher.group(1));
    this.unit = matcher.group(2).toUpperCase();
    this.bytes = convertToBytes(this.value, this.unit);
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
   * @return unit string (B, KB, MB, GB, TB, or PB)
   */
  public String getUnit() {
    return unit;
  }

  /**
   * Returns the canonical representation in bytes.
   *
   * @return value converted to bytes
   */
  public long getBytes() {
    return bytes;
  }

  /**
   * Converts the given value with unit to bytes.
   *
   * @param value numeric value
   * @param unit  string unit (B, KB, MB, GB, TB, or PB)
   * @return the value in bytes
   */
  private long convertToBytes(double value, String unit) {
    switch (unit.toUpperCase()) {
      case "B":
        return (long) value;
      case "KB":
        return (long) (value * 1024);
      case "MB":
        return (long) (value * 1024 * 1024);
      case "GB":
        return (long) (value * 1024 * 1024 * 1024);
      case "TB":
        return (long) (value * 1024 * 1024 * 1024 * 1024);
      case "PB":
        return (long) (value * 1024 * 1024 * 1024 * 1024 * 1024);
      default:
        throw new IllegalArgumentException("Unsupported byte size unit: " + unit);
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
   * @return BYTE_SIZE token type
   */
  @Override
  public TokenType type() {
    return TokenType.BYTE_SIZE;
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
    object.addProperty("bytes", bytes);
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
    ByteSize byteSize = (ByteSize) o;
    return Double.compare(byteSize.value, value) == 0 &&
           bytes == byteSize.bytes &&
           Objects.equals(rawValue, byteSize.rawValue) &&
           Objects.equals(unit, byteSize.unit);
  }

  @Override
  public int hashCode() {
    return Objects.hash(rawValue, value, unit, bytes);
  }

  @Override
  public String toString() {
    return rawValue;
  }
}
