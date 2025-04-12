/*
 * Copyright © 2023 Cask Data, Inc.
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

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class <code>ByteSize</code> represents a byte size token in the recipe.
 * It supports standard size units (B, KB, MB, GB, TB, PB) with conversion between them.
 */
public class ByteSize extends Token<Double> {
  // Regex pattern for parsing byte size values with units
  private static final Pattern PATTERN = Pattern.compile("^(\\d+(?:\\.\\d+)?)\\s*([KkMmGgTtPp]?[Bb])$");
  
  // Conversion factors (using binary prefixes: 1KB = 1024B)
  private static final int KB_TO_B = 1024;
  private static final int MB_TO_B = 1024 * KB_TO_B;
  private static final int GB_TO_B = 1024 * MB_TO_B;
  private static final long TB_TO_B = 1024L * GB_TO_B;
  private static final long PB_TO_B = 1024L * TB_TO_B;
  
  // Original string representation
  private final String originalValue;
  
  // Converted value in bytes
  private final double bytes;
  
  // Unit used in the original value
  private final String unit;
  
  /**
   * Constructor for a byte size token.
   *
   * @param value the byte size expression (e.g., "10KB", "2.5MB", "1GB")
   * @throws SyntaxError if the value cannot be parsed as a byte size
   */
  public ByteSize(String value) throws SyntaxError {
    super(value);
    this.originalValue = value;
    
    // Parse the value and unit
    Matcher matcher = PATTERN.matcher(value.trim());
    if (!matcher.matches()) {
      throw new SyntaxError(
        String.format("Invalid byte size format '%s'. Expected format: <number><unit> (e.g., 10KB, 2.5MB)", value));
    }
    
    // Extract the numeric value and unit
    double numericValue = Double.parseDouble(matcher.group(1));
    String unitStr = matcher.group(2).toUpperCase(); // Normalize to uppercase
    this.unit = unitStr;
    
    // Convert to bytes based on the unit
    this.bytes = convertToBytes(numericValue, unitStr);
  }
  
  /**
   * Constructor for a byte size from a raw byte value.
   * Used internally for conversions.
   *
   * @param bytes the byte value
   * @param unit the unit to display
   */
  private ByteSize(double bytes, String unit) throws SyntaxError {
    super(bytes + "B");
    this.originalValue = bytes + unit;
    this.bytes = bytes;
    this.unit = unit;
  }
  
  /**
   * Convert a value with a unit to bytes.
   *
   * @param value the numeric value
   * @param unit the unit string (B, KB, MB, GB, TB, PB)
   * @return the value in bytes
   * @throws SyntaxError if the unit is not recognized
   */
  private double convertToBytes(double value, String unit) throws SyntaxError {
    switch (unit.toUpperCase()) {
      case "B":
        return value;
      case "KB":
        return value * KB_TO_B;
      case "MB":
        return value * MB_TO_B;
      case "GB":
        return value * GB_TO_B;
      case "TB":
        return value * TB_TO_B;
      case "PB":
        return value * PB_TO_B;
      default:
        throw new SyntaxError(String.format("Unknown byte size unit: %s", unit));
    }
  }
  
  /**
   * Convert the byte size to a specified unit.
   *
   * @param targetUnit the target unit (B, KB, MB, GB, TB, PB)
   * @return the value in the target unit
   * @throws SyntaxError if the target unit is not recognized
   */
  public double convertTo(String targetUnit) throws SyntaxError {
    switch (targetUnit.toUpperCase()) {
      case "B":
        return bytes;
      case "KB":
        return bytes / KB_TO_B;
      case "MB":
        return bytes / MB_TO_B;
      case "GB":
        return bytes / GB_TO_B;
      case "TB":
        return bytes / TB_TO_B;
      case "PB":
        return bytes / PB_TO_B;
      default:
        throw new SyntaxError(String.format("Unknown byte size unit: %s", targetUnit));
    }
  }
  
  /**
   * @return the original string value
   */
  public String getOriginalValue() {
    return originalValue;
  }
  
  /**
   * @return the unit used in the original value
   */
  public String getUnit() {
    return unit;
  }
  
  /**
   * @return the value in bytes
   */
  @Override
  public Double value() {
    return bytes;
  }
  
  /**
   * @return token type BYTE_SIZE
   */
  @Override
  public TokenType type() {
    return TokenType.BYTE_SIZE;
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ByteSize that = (ByteSize) o;
    return Double.compare(that.bytes, bytes) == 0;
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(bytes);
  }
  
  @Override
  public String toString() {
    return originalValue;
  }
}