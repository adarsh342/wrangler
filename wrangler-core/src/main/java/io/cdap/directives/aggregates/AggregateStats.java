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

package io.cdap.directives.aggregates;

import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.DirectiveExecutionException;
import io.cdap.wrangler.api.DirectiveParseException;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Optional;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.annotations.Categories;
import io.cdap.wrangler.api.lineage.Lineage;
import io.cdap.wrangler.api.lineage.Many;
import io.cdap.wrangler.api.lineage.Mutation;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.Text;
import io.cdap.wrangler.api.parser.TimeDuration;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A directive that computes aggregate statistics on columns with byte size or time duration values.
 * 
 * This directive provides functionality to compute:
 * - Min, max, sum, and average for ByteSize values (such as "10MB", "1.5GB")
 * - Min, max, sum, and average for TimeDuration values (such as "150ms", "2.5s")
 * 
 * The statistics are stored in a transient variable that can be accessed by subsequent directives.
 * 
 * Usage:
 * - For byte sizes: aggregate-stats byte-size :column output-variable [formats]
 * - For time durations: aggregate-stats time-duration :column output-variable [formats]
 * 
 * Optional formats:
 * - For byte sizes: B, KB, MB, GB, TB, PB
 * - For time durations: ms, s, m, h, d
 */
@Plugin(type = Directive.TYPE)
@Name("aggregate-stats")
@Categories(categories = {"aggregate"})
@Description("Computes statistics on columns with byte size or time duration values.")
public class AggregateStats implements Directive {
  public static final String BYTE_SIZE_TYPE = "byte-size";
  public static final String TIME_DURATION_TYPE = "time-duration";
  
  private String column;
  private String type;
  private String variable;
  private String[] formats;
  
  @Override
  public UsageDefinition define() {
    UsageDefinition.Builder builder = UsageDefinition.builder("aggregate-stats");
    builder.define("type", TokenType.TEXT, "Type of values to aggregate (byte-size or time-duration)");
    builder.define("column", TokenType.COLUMN_NAME, "Column containing values to aggregate");
    builder.define("variable", TokenType.TEXT, "Name of the transient variable to store results");
    builder.define("formats", TokenType.TEXT, Optional.TRUE, "Output formats for results");
    return builder.build();
  }
  
  @Override
  public void initialize(Arguments args) throws DirectiveParseException {
    this.type = ((Text) args.value("type")).value().toString();
    this.column = ((ColumnName) args.value("column")).value().toString();
    this.variable = ((Text) args.value("variable")).value().toString();
    
    if (!BYTE_SIZE_TYPE.equals(type) && !TIME_DURATION_TYPE.equals(type)) {
      throw new DirectiveParseException(
        String.format("Type '%s' not supported. Supported types are 'byte-size' and 'time-duration'.", type));
    }
    
    if (args.contains("formats")) {
      this.formats = ((Text) args.value("formats")).value().toString().split(",");
    } else {
      this.formats = new String[0];
    }
  }
  
  @Override
  public List<Row> execute(List<Row> rows, ExecutorContext context) throws DirectiveExecutionException {
    if (rows.isEmpty()) {
      return rows;
    }
    
    // Stats to compute
    long min = Long.MAX_VALUE;
    long max = Long.MIN_VALUE;
    long sum = 0;
    int count = 0;
    
    // Process rows
    for (Row row : rows) {
      int idx = row.find(column);
      if (idx == -1) {
        continue;
      }
      
      Object object = row.getValue(idx);
      if (object == null) {
        continue;
      }
      
      try {
        long value;
        if (BYTE_SIZE_TYPE.equals(type)) {
          // Convert to bytes
          ByteSize byteSize = new ByteSize(object.toString());
          value = byteSize.getBytes();
        } else { // TIME_DURATION_TYPE
          // Convert to nanoseconds
          TimeDuration timeDuration = new TimeDuration(object.toString());
          value = timeDuration.getNanoseconds();
        }
        
        // Update stats
        min = Math.min(min, value);
        max = Math.max(max, value);
        sum += value;
        count++;
      } catch (Exception e) {
        throw new DirectiveExecutionException(
          String.format("Failed to parse %s value: %s", type, object.toString()), e);
      }
    }
    
    // Calculate results
    Map<String, Object> results = new HashMap<>();
    if (count > 0) {
      results.put("min", min);
      results.put("max", max);
      results.put("sum", sum);
      results.put("avg", sum / count);
      results.put("count", count);
      
      // Format results if formats specified
      if (formats.length > 0) {
        Map<String, Object> formattedResults = new HashMap<>();
        
        for (String format : formats) {
          if (BYTE_SIZE_TYPE.equals(type)) {
            formattedResults.put("min_" + format, formatBytes(min, format));
            formattedResults.put("max_" + format, formatBytes(max, format));
            formattedResults.put("sum_" + format, formatBytes(sum, format));
            formattedResults.put("avg_" + format, formatBytes(sum / count, format));
          } else { // TIME_DURATION_TYPE
            formattedResults.put("min_" + format, formatDuration(min, format));
            formattedResults.put("max_" + format, formatDuration(max, format));
            formattedResults.put("sum_" + format, formatDuration(sum, format));
            formattedResults.put("avg_" + format, formatDuration(sum / count, format));
          }
        }
        
        results.put("formatted", formattedResults);
      }
    } else {
      // No valid values found
      results.put("count", 0);
    }
    
    // Store results in transient store
    context.getTransientStore().set(variable, results);
    
    return rows;
  }
  
  /**
   * Formats a byte value into the specified unit.
   *
   * @param bytes The value in bytes
   * @param format The target format (B, KB, MB, GB, TB, PB)
   * @return The formatted value as a double
   * @throws DirectiveExecutionException If the format is not supported
   */
  private double formatBytes(long bytes, String format) throws DirectiveExecutionException {
    switch (format.toUpperCase()) {
      case "B":
        return bytes;
      case "KB":
        return bytes / 1024.0;
      case "MB":
        return bytes / (1024.0 * 1024);
      case "GB":
        return bytes / (1024.0 * 1024 * 1024);
      case "TB":
        return bytes / (1024.0 * 1024 * 1024 * 1024);
      case "PB":
        return bytes / (1024.0 * 1024 * 1024 * 1024 * 1024);
      default:
        throw new DirectiveExecutionException(
          String.format("Unsupported byte size format: %s. Supported formats are: B, KB, MB, GB, TB, PB", format));
    }
  }
  
  /**
   * Formats a duration value in nanoseconds into the specified unit.
   *
   * @param nanos The value in nanoseconds
   * @param format The target format (ms, s, m, h, d)
   * @return The formatted value as a double
   * @throws DirectiveExecutionException If the format is not supported
   */
  private double formatDuration(long nanos, String format) throws DirectiveExecutionException {
    switch (format) {
      case "ms":
        return nanos / 1_000_000.0;
      case "s":
        return nanos / 1_000_000_000.0;
      case "m":
        return nanos / (60.0 * 1_000_000_000);
      case "h":
        return nanos / (60.0 * 60 * 1_000_000_000);
      case "d":
        return nanos / (24.0 * 60 * 60 * 1_000_000_000);
      default:
        throw new DirectiveExecutionException(
          String.format("Unsupported time duration format: %s. Supported formats are: ms, s, m, h, d", format));
    }
  }
  
  @Lineage(
    inputs = @Mutation(name = "column", type = Many.class),
    outputs = @Mutation(name = "variable")
  )
  public String lineage() {
    return String.format("Computed aggregate statistics on %s column '%s' and stored in transient variable '%s'", 
                         type, column, variable);
  }
}