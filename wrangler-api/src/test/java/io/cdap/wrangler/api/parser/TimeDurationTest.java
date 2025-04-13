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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link TimeDuration} class.
 */
public class TimeDurationTest {

  @Test
  public void testValidTimeDurations() {
    // Test milliseconds
    TimeDuration duration = new TimeDuration("150ms");
    Assert.assertEquals(150 * 1_000_000, duration.getNanoseconds());
    Assert.assertEquals(150, duration.getMilliseconds());
    Assert.assertEquals(0.15, duration.getSeconds(), 0.001);
    Assert.assertEquals(150.0, duration.getValue(), 0.001);
    Assert.assertEquals("ms", duration.getUnit());
    
    // Test seconds
    duration = new TimeDuration("2.5s");
    Assert.assertEquals((long)(2.5 * 1_000_000_000), duration.getNanoseconds());
    Assert.assertEquals(2500, duration.getMilliseconds());
    Assert.assertEquals(2.5, duration.getSeconds(), 0.001);
    Assert.assertEquals(2.5, duration.getValue(), 0.001);
    Assert.assertEquals("s", duration.getUnit());
    
    // Test minutes
    duration = new TimeDuration("1m");
    Assert.assertEquals(60 * 1_000_000_000L, duration.getNanoseconds());
    Assert.assertEquals(60 * 1000, duration.getMilliseconds());
    Assert.assertEquals(60.0, duration.getSeconds(), 0.001);
    Assert.assertEquals(1.0, duration.getValue(), 0.001);
    Assert.assertEquals("m", duration.getUnit());
    
    // Test hours
    duration = new TimeDuration("1.5h");
    Assert.assertEquals((long)(1.5 * 60 * 60 * 1_000_000_000), duration.getNanoseconds());
    Assert.assertEquals((long)(1.5 * 60 * 60 * 1000), duration.getMilliseconds());
    Assert.assertEquals(1.5 * 60 * 60, duration.getSeconds(), 0.001);
    Assert.assertEquals(1.5, duration.getValue(), 0.001);
    Assert.assertEquals("h", duration.getUnit());
    
    // Test days
    duration = new TimeDuration("2d");
    Assert.assertEquals(2 * 24 * 60 * 60 * 1_000_000_000L, duration.getNanoseconds());
    Assert.assertEquals(2 * 24 * 60 * 60 * 1000, duration.getMilliseconds());
    Assert.assertEquals(2 * 24 * 60 * 60, duration.getSeconds(), 0.001);
    Assert.assertEquals(2.0, duration.getValue(), 0.001);
    Assert.assertEquals("d", duration.getUnit());
    
    // Test with different spacing
    duration = new TimeDuration("100 ms");
    Assert.assertEquals(100 * 1_000_000, duration.getNanoseconds());
    Assert.assertEquals(100, duration.getMilliseconds());
    Assert.assertEquals(0.1, duration.getSeconds(), 0.001);
    Assert.assertEquals(100.0, duration.getValue(), 0.001);
    Assert.assertEquals("ms", duration.getUnit());
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInvalidTimeDuration() {
    new TimeDuration("not a time duration");
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInvalidTimeDurationUnit() {
    new TimeDuration("10xy");
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testMissingUnit() {
    new TimeDuration("10");
  }
  
  @Test
  public void testToJson() {
    TimeDuration duration = new TimeDuration("150ms");
    JsonElement json = duration.toJson();
    Assert.assertTrue(json.isJsonObject());
    
    JsonObject jsonObject = json.getAsJsonObject();
    Assert.assertEquals("TIME_DURATION", jsonObject.get("type").getAsString());
    Assert.assertEquals("150ms", jsonObject.get("value").getAsString());
    Assert.assertEquals(150 * 1_000_000, jsonObject.get("nanoseconds").getAsLong());
  }
  
  @Test
  public void testTokenType() {
    TimeDuration duration = new TimeDuration("150ms");
    Assert.assertEquals(TokenType.TIME_DURATION, duration.type());
  }
  
  @Test
  public void testValue() {
    TimeDuration duration = new TimeDuration("150ms");
    Assert.assertEquals("150ms", duration.value());
  }
  
  @Test
  public void testEquality() {
    TimeDuration duration1 = new TimeDuration("150ms");
    TimeDuration duration2 = new TimeDuration("150ms");
    TimeDuration duration3 = new TimeDuration("200ms");
    
    Assert.assertEquals(duration1, duration2);
    Assert.assertNotEquals(duration1, duration3);
    Assert.assertEquals(duration1.hashCode(), duration2.hashCode());
    Assert.assertNotEquals(duration1.hashCode(), duration3.hashCode());
  }
  
  @Test
  public void testToString() {
    TimeDuration duration = new TimeDuration("150ms");
    Assert.assertEquals("150ms", duration.toString());
  }
}