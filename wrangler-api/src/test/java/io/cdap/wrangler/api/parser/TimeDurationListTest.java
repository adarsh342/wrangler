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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Tests for {@link TimeDurationList} class.
 */
public class TimeDurationListTest {

  @Test
  public void testTimeDurationList() {
    TimeDuration duration1 = new TimeDuration("150ms");
    TimeDuration duration2 = new TimeDuration("2.5s");
    TimeDuration duration3 = new TimeDuration("1h");
    
    List<TimeDuration> durations = Arrays.asList(duration1, duration2, duration3);
    TimeDurationList durationList = new TimeDurationList(durations);
    
    // Test getTimeDurations
    List<TimeDuration> retrievedDurations = durationList.getTimeDurations();
    Assert.assertEquals(3, retrievedDurations.size());
    Assert.assertEquals(duration1, retrievedDurations.get(0));
    Assert.assertEquals(duration2, retrievedDurations.get(1));
    Assert.assertEquals(duration3, retrievedDurations.get(2));
    
    // Test value
    @SuppressWarnings("unchecked")
    List<TimeDuration> valueList = (List<TimeDuration>) durationList.value();
    Assert.assertEquals(durations, valueList);
    
    // Test type
    Assert.assertEquals(TokenType.TIME_DURATION_LIST, durationList.type());
    
    // Test iterator
    Iterator<TimeDuration> iterator = durationList.iterator();
    Assert.assertTrue(iterator.hasNext());
    Assert.assertEquals(duration1, iterator.next());
    Assert.assertTrue(iterator.hasNext());
    Assert.assertEquals(duration2, iterator.next());
    Assert.assertTrue(iterator.hasNext());
    Assert.assertEquals(duration3, iterator.next());
    Assert.assertFalse(iterator.hasNext());
  }
  
  @Test
  public void testToJson() {
    TimeDuration duration1 = new TimeDuration("150ms");
    TimeDuration duration2 = new TimeDuration("2.5s");
    
    List<TimeDuration> durations = Arrays.asList(duration1, duration2);
    TimeDurationList durationList = new TimeDurationList(durations);
    
    JsonElement json = durationList.toJson();
    Assert.assertTrue(json.isJsonObject());
    
    JsonObject jsonObject = json.getAsJsonObject();
    Assert.assertEquals("TIME_DURATION_LIST", jsonObject.get("type").getAsString());
    
    JsonArray valueArray = jsonObject.getAsJsonArray("value");
    Assert.assertEquals(2, valueArray.size());
    
    JsonObject firstItem = valueArray.get(0).getAsJsonObject();
    Assert.assertEquals("TIME_DURATION", firstItem.get("type").getAsString());
    Assert.assertEquals("150ms", firstItem.get("value").getAsString());
    
    JsonObject secondItem = valueArray.get(1).getAsJsonObject();
    Assert.assertEquals("TIME_DURATION", secondItem.get("type").getAsString());
    Assert.assertEquals("2.5s", secondItem.get("value").getAsString());
  }
  
  @Test
  public void testEquality() {
    TimeDuration duration1 = new TimeDuration("150ms");
    TimeDuration duration2 = new TimeDuration("2.5s");
    
    TimeDurationList list1 = new TimeDurationList(Arrays.asList(duration1, duration2));
    TimeDurationList list2 = new TimeDurationList(Arrays.asList(duration1, duration2));
    TimeDurationList list3 = new TimeDurationList(Arrays.asList(duration2, duration1)); // Different order
    
    Assert.assertEquals(list1, list2);
    Assert.assertNotEquals(list1, list3);
    Assert.assertEquals(list1.hashCode(), list2.hashCode());
    Assert.assertNotEquals(list1.hashCode(), list3.hashCode());
  }
  
  @Test
  public void testToString() {
    TimeDuration duration1 = new TimeDuration("150ms");
    TimeDuration duration2 = new TimeDuration("2.5s");
    
    TimeDurationList durationList = new TimeDurationList(Arrays.asList(duration1, duration2));
    Assert.assertEquals("[150ms, 2.5s]", durationList.toString());
  }
  
  @Test
  public void testEmptyList() {
    TimeDurationList emptyList = new TimeDurationList(new ArrayList<>());
    
    Assert.assertTrue(emptyList.getTimeDurations().isEmpty());
    Assert.assertFalse(emptyList.iterator().hasNext());
    Assert.assertEquals("[]", emptyList.toString());
    
    JsonElement json = emptyList.toJson();
    JsonObject jsonObject = json.getAsJsonObject();
    JsonArray valueArray = jsonObject.getAsJsonArray("value");
    Assert.assertEquals(0, valueArray.size());
  }
}