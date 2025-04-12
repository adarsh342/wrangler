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
 * This class <code>TimeDuration</code> represents a time duration token in the recipe.
 * It supports standard time units (ns, μs, ms, s, m, h, d) with conversion between them.
 */
public class TimeDuration extends Token<Double> {
  // Regex pattern for parsing time duration values with units
  private static final Pattern PATTERN = Pattern.compile("^(\\d+(?:\\.\\d+)?)\\s*([nμm]?[smhd])$");
  
  // Conversion factors to nanoseconds (base unit)
  private static final long NS_TO_NS = 1L;
  private static final long US_TO_NS = 1_000L;
  private static final long MS_TO_NS = 1_000_000L;
  private static final long S_TO_NS = 1_000_000_000L;
  private static final long M_TO_NS = 60 * S_TO_NS;
  private static final long H_TO_NS = 60 * M_TO_NS;
  private static final long D_TO_NS = 24 * H_TO_NS;
  
  // Original string representation
  private final String originalValue;
  
  // Converted value in nanoseconds
  private final double nanoseconds;
  
  // Unit used in the original value
  private final String unit;
  
  /**
   * Constructor for a time duration token.
   *
   * @param value the time duration expression (e.g., "10ms", "2.5s", "1h")
   * @throws SyntaxError if the value cannot be parsed as a time duration
   */
  public TimeDuration(String value) throws SyntaxError {
    super(value);
    this.originalValue = value;
    
    // Parse the value and unit
    Matcher matcher = PATTERN.matcher(value.trim());
    if (!matcher.matches()) {
      throw new SyntaxError(
        String.format("Invalid time duration format '%s'. Expected format: <number><unit> (e.g., 10ms, 2.5s)", value));
    }
    
    // Extract the numeric value and unit
    double numericValue = Double.parseDouble(matcher.group(1));
    String unitStr = matcher.group(2);
    this.unit = unitStr;
    
    // Convert to nanoseconds based on the unit
    this.nanoseconds = convertToNanoseconds(numericValue, unitStr);
  }
  
  /**
   * Convert a value with a unit to nanoseconds.
   *
   * @param value the numeric value
   * @param unit the unit string (ns, μs, ms, s, m, h, d)
   * @return the value in nanoseconds
   * @throws SyntaxError if the unit is not recognized
   */
  private double convertToNanoseconds(double value, String unit) throws SyntaxError {
    switch (unit) {
      case "ns":
        return value * NS_TO_NS;
      case "μs":
        return value * US_TO_NS;
      case "ms":
        return value * MS_TO_NS;
      case "s":
        return value * S_TO_NS;
      case "m":
        return value * M_TO_NS;
      case "h":
        return value * H_TO_NS;
      case "d":
        return value * D_TO_NS;
      default:
        throw new SyntaxError(String.format("Unknown time duration unit: %s", unit));
    }
  }
  
  /**
   * Convert the time duration to a specified unit.
   *
   * @param targetUnit the target unit (ns, μs, ms, s, m, h, d)
   * @return the value in the target unit
   * @throws SyntaxError if the target unit is not recognized
   */
  public double convertTo(String targetUnit) throws SyntaxError {
    switch (targetUnit) {
      case "ns":
        return nanoseconds;
      case "μs":
        return nanoseconds / US_TO_NS;
      case "ms":
        return nanoseconds / MS_TO_NS;
      case "s":
        return nanoseconds / S_TO_NS;
      case "m":
        return nanoseconds / M_TO_NS;
      case "h":
        return nanoseconds / H_TO_NS;
      case "d":
        return nanoseconds / D_TO_NS;
      default:
        throw new SyntaxError(String.format("Unknown time duration unit: %s", targetUnit));
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
   * @return the value in nanoseconds
   */
  @Override
  public Double value() {
    return nanoseconds;
  }
  
  /**
   * @return token type TIME_DURATION
   */
  @Override
  public TokenType type() {
    return TokenType.TIME_DURATION;
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
    return Double.compare(that.nanoseconds, nanoseconds) == 0;
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(nanoseconds);
  }
  
  @Override
  public String toString() {
    return originalValue;
  }
}