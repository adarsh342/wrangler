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

import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.DirectiveExecutionException;
import io.cdap.wrangler.api.DirectiveParseException;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.TransientStore;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.Text;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;
import io.cdap.wrangler.executor.EmptyTransientStore;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tests for {@link AggregateStats} directive.
 */
public class AggregateStatsTest {

  /**
   * Test the directive definition.
   */
  @Test
  public void testDefine() {
    AggregateStats directive = new AggregateStats();
    UsageDefinition definition = directive.define();
    
    Assert.assertEquals("aggregate-stats", definition.name());
    Assert.assertEquals(4, definition.tokens().size());
    
    Assert.assertEquals(TokenType.TEXT, definition.tokens().get(0).type());
    Assert.assertEquals("type", definition.tokens().get(0).name());
    
    Assert.assertEquals(TokenType.COLUMN_NAME, definition.tokens().get(1).type());
    Assert.assertEquals("column", definition.tokens().get(1).name());
    
    Assert.assertEquals(TokenType.TEXT, definition.tokens().get(2).type());
    Assert.assertEquals("variable", definition.tokens().get(2).name());
    
    Assert.assertEquals(TokenType.TEXT, definition.tokens().get(3).type());
    Assert.assertEquals("formats", definition.tokens().get(3).name());
    Assert.assertTrue(definition.tokens().get(3).optional());
  }
  
  /**
   * Test initialization with an invalid type parameter.
   */
  @Test(expected = DirectiveParseException.class)
  public void testInitializeInvalidType() throws DirectiveParseException {
    AggregateStats directive = new AggregateStats();
    
    Arguments args = Mockito.mock(Arguments.class);
    Mockito.when(args.value("type")).thenReturn(new Text("invalid-type"));
    Mockito.when(args.value("column")).thenReturn(new ColumnName("sizes"));
    Mockito.when(args.value("variable")).thenReturn(new Text("stats"));
    Mockito.when(args.contains("formats")).thenReturn(false);
    
    directive.initialize(args);
  }
  
  /**
   * Test byte size aggregation.
   */
  @Test
  public void testByteSizeAggregation() throws DirectiveParseException, DirectiveExecutionException {
    AggregateStats directive = new AggregateStats();
    
    // Initialize directive
    Arguments args = Mockito.mock(Arguments.class);
    Mockito.when(args.value("type")).thenReturn(new Text("byte-size"));
    Mockito.when(args.value("column")).thenReturn(new ColumnName("sizes"));
    Mockito.when(args.value("variable")).thenReturn(new Text("size_stats"));
    Mockito.when(args.contains("formats")).thenReturn(true);
    Mockito.when(args.value("formats")).thenReturn(new Text("KB,MB"));
    
    directive.initialize(args);
    
    // Create test data
    List<Row> rows = new ArrayList<>();
    Row row1 = new Row();
    row1.add("sizes", "10KB");
    rows.add(row1);
    
    Row row2 = new Row();
    row2.add("sizes", "1MB");
    rows.add(row2);
    
    Row row3 = new Row();
    row3.add("sizes", "500KB");
    rows.add(row3);
    
    // Create mock executor context
    TransientStore store = new EmptyTransientStore();
    ExecutorContext context = Mockito.mock(ExecutorContext.class);
    Mockito.when(context.getTransientStore()).thenReturn(store);
    
    // Execute directive
    directive.execute(rows, context);
    
    // Verify results
    @SuppressWarnings("unchecked")
    Map<String, Object> results = (Map<String, Object>) store.get("size_stats");
    Assert.assertNotNull(results);
    
    Assert.assertEquals(3, results.get("count"));
    Assert.assertEquals(10 * 1024L, results.get("min"));
    Assert.assertEquals(1024 * 1024L, results.get("max"));
    Assert.assertEquals((10 * 1024L) + (1024 * 1024L) + (500 * 1024L), results.get("sum"));
    Assert.assertEquals(((10 * 1024L) + (1024 * 1024L) + (500 * 1024L)) / 3, results.get("avg"));
    
    @SuppressWarnings("unchecked")
    Map<String, Object> formatted = (Map<String, Object>) results.get("formatted");
    Assert.assertNotNull(formatted);
    
    Assert.assertEquals(10.0, formatted.get("min_KB"));
    Assert.assertEquals(1024.0, formatted.get("max_KB"));
    Assert.assertEquals((10.0 + 1024.0 + 500.0), formatted.get("sum_KB"));
    Assert.assertEquals((10.0 + 1024.0 + 500.0) / 3, formatted.get("avg_KB"));
    
    Assert.assertEquals(10.0 / 1024, formatted.get("min_MB"));
    Assert.assertEquals(1.0, formatted.get("max_MB"));
    Assert.assertEquals((10.0 / 1024) + 1.0 + (500.0 / 1024), formatted.get("sum_MB"));
    Assert.assertEquals(((10.0 / 1024) + 1.0 + (500.0 / 1024)) / 3, formatted.get("avg_MB"));
  }
  
  /**
   * Test time duration aggregation.
   */
  @Test
  public void testTimeDurationAggregation() throws DirectiveParseException, DirectiveExecutionException {
    AggregateStats directive = new AggregateStats();
    
    // Initialize directive
    Arguments args = Mockito.mock(Arguments.class);
    Mockito.when(args.value("type")).thenReturn(new Text("time-duration"));
    Mockito.when(args.value("column")).thenReturn(new ColumnName("durations"));
    Mockito.when(args.value("variable")).thenReturn(new Text("time_stats"));
    Mockito.when(args.contains("formats")).thenReturn(true);
    Mockito.when(args.value("formats")).thenReturn(new Text("ms,s"));
    
    directive.initialize(args);
    
    // Create test data
    List<Row> rows = new ArrayList<>();
    Row row1 = new Row();
    row1.add("durations", "100ms");
    rows.add(row1);
    
    Row row2 = new Row();
    row2.add("durations", "2s");
    rows.add(row2);
    
    Row row3 = new Row();
    row3.add("durations", "500ms");
    rows.add(row3);
    
    // Create mock executor context
    TransientStore store = new EmptyTransientStore();
    ExecutorContext context = Mockito.mock(ExecutorContext.class);
    Mockito.when(context.getTransientStore()).thenReturn(store);
    
    // Execute directive
    directive.execute(rows, context);
    
    // Verify results
    @SuppressWarnings("unchecked")
    Map<String, Object> results = (Map<String, Object>) store.get("time_stats");
    Assert.assertNotNull(results);
    
    Assert.assertEquals(3, results.get("count"));
    Assert.assertEquals(100 * 1_000_000L, results.get("min"));
    Assert.assertEquals(2 * 1_000_000_000L, results.get("max"));
    Assert.assertEquals((100 * 1_000_000L) + (2 * 1_000_000_000L) + (500 * 1_000_000L), results.get("sum"));
    Assert.assertEquals(((100 * 1_000_000L) + (2 * 1_000_000_000L) + (500 * 1_000_000L)) / 3, results.get("avg"));
    
    @SuppressWarnings("unchecked")
    Map<String, Object> formatted = (Map<String, Object>) results.get("formatted");
    Assert.assertNotNull(formatted);
    
    Assert.assertEquals(100.0, formatted.get("min_ms"));
    Assert.assertEquals(2000.0, formatted.get("max_ms"));
    Assert.assertEquals(100.0 + 2000.0 + 500.0, formatted.get("sum_ms"));
    Assert.assertEquals((100.0 + 2000.0 + 500.0) / 3, formatted.get("avg_ms"));
    
    Assert.assertEquals(0.1, formatted.get("min_s"));
    Assert.assertEquals(2.0, formatted.get("max_s"));
    Assert.assertEquals(0.1 + 2.0 + 0.5, formatted.get("sum_s"));
    Assert.assertEquals((0.1 + 2.0 + 0.5) / 3, formatted.get("avg_s"));
  }
  
  /**
   * Test handling of empty inputs.
   */
  @Test
  public void testEmptyInputs() throws DirectiveParseException, DirectiveExecutionException {
    AggregateStats directive = new AggregateStats();
    
    // Initialize directive
    Arguments args = Mockito.mock(Arguments.class);
    Mockito.when(args.value("type")).thenReturn(new Text("byte-size"));
    Mockito.when(args.value("column")).thenReturn(new ColumnName("sizes"));
    Mockito.when(args.value("variable")).thenReturn(new Text("stats"));
    Mockito.when(args.contains("formats")).thenReturn(false);
    
    directive.initialize(args);
    
    // Empty rows
    List<Row> emptyRows = new ArrayList<>();
    
    // Create mock executor context
    TransientStore store = new EmptyTransientStore();
    ExecutorContext context = Mockito.mock(ExecutorContext.class);
    Mockito.when(context.getTransientStore()).thenReturn(store);
    
    // Execute directive
    directive.execute(emptyRows, context);
    
    // Verify results
    @SuppressWarnings("unchecked")
    Map<String, Object> results = (Map<String, Object>) store.get("stats");
    Assert.assertNotNull(results);
    Assert.assertEquals(0, results.get("count"));
  }
  
  /**
   * Test handling of invalid values.
   */
  @Test(expected = DirectiveExecutionException.class)
  public void testInvalidValues() throws DirectiveParseException, DirectiveExecutionException {
    AggregateStats directive = new AggregateStats();
    
    // Initialize directive
    Arguments args = Mockito.mock(Arguments.class);
    Mockito.when(args.value("type")).thenReturn(new Text("byte-size"));
    Mockito.when(args.value("column")).thenReturn(new ColumnName("sizes"));
    Mockito.when(args.value("variable")).thenReturn(new Text("stats"));
    Mockito.when(args.contains("formats")).thenReturn(false);
    
    directive.initialize(args);
    
    // Create test data with invalid value
    List<Row> rows = new ArrayList<>();
    Row row = new Row();
    row.add("sizes", "not-a-byte-size");
    rows.add(row);
    
    // Create mock executor context
    ExecutorContext context = Mockito.mock(ExecutorContext.class);
    Mockito.when(context.getTransientStore()).thenReturn(new EmptyTransientStore());
    
    // Execute directive - should throw exception
    directive.execute(rows, context);
  }
  
  /**
   * Test invalid format specifier.
   */
  @Test(expected = DirectiveExecutionException.class)
  public void testInvalidFormat() throws DirectiveParseException, DirectiveExecutionException {
    AggregateStats directive = new AggregateStats();
    
    // Initialize directive
    Arguments args = Mockito.mock(Arguments.class);
    Mockito.when(args.value("type")).thenReturn(new Text("byte-size"));
    Mockito.when(args.value("column")).thenReturn(new ColumnName("sizes"));
    Mockito.when(args.value("variable")).thenReturn(new Text("stats"));
    Mockito.when(args.contains("formats")).thenReturn(true);
    Mockito.when(args.value("formats")).thenReturn(new Text("invalid-format"));
    
    directive.initialize(args);
    
    // Create test data
    List<Row> rows = new ArrayList<>();
    Row row = new Row();
    row.add("sizes", "10MB");
    rows.add(row);
    
    // Create mock executor context
    ExecutorContext context = Mockito.mock(ExecutorContext.class);
    Mockito.when(context.getTransientStore()).thenReturn(new EmptyTransientStore());
    
    // Execute directive - should throw exception
    directive.execute(rows, context);
  }
  
  /**
   * A simple implementation of TransientStore for testing.
   */
  private static class EmptyTransientStore implements TransientStore {
    private final Map<String, Object> store = new HashMap<>();
    
    @Override
    public void reset() {
      store.clear();
    }
    
    @Override
    public void set(String name, Object value) {
      store.put(name, value);
    }
    
    @Override
    public Object get(String name) {
      return store.get(name);
    }
  }
}