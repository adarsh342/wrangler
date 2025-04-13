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
 * Tests for {@link ByteSizeList} class.
 */
public class ByteSizeListTest {

  @Test
  public void testByteSizeList() {
    ByteSize byteSize1 = new ByteSize("10MB");
    ByteSize byteSize2 = new ByteSize("1.5GB");
    ByteSize byteSize3 = new ByteSize("500KB");
    
    List<ByteSize> byteSizes = Arrays.asList(byteSize1, byteSize2, byteSize3);
    ByteSizeList byteSizeList = new ByteSizeList(byteSizes);
    
    // Test getByteSizes
    List<ByteSize> retrievedByteSizes = byteSizeList.getByteSizes();
    Assert.assertEquals(3, retrievedByteSizes.size());
    Assert.assertEquals(byteSize1, retrievedByteSizes.get(0));
    Assert.assertEquals(byteSize2, retrievedByteSizes.get(1));
    Assert.assertEquals(byteSize3, retrievedByteSizes.get(2));
    
    // Test value
    @SuppressWarnings("unchecked")
    List<ByteSize> valueList = (List<ByteSize>) byteSizeList.value();
    Assert.assertEquals(byteSizes, valueList);
    
    // Test type
    Assert.assertEquals(TokenType.BYTE_SIZE_LIST, byteSizeList.type());
    
    // Test iterator
    Iterator<ByteSize> iterator = byteSizeList.iterator();
    Assert.assertTrue(iterator.hasNext());
    Assert.assertEquals(byteSize1, iterator.next());
    Assert.assertTrue(iterator.hasNext());
    Assert.assertEquals(byteSize2, iterator.next());
    Assert.assertTrue(iterator.hasNext());
    Assert.assertEquals(byteSize3, iterator.next());
    Assert.assertFalse(iterator.hasNext());
  }
  
  @Test
  public void testToJson() {
    ByteSize byteSize1 = new ByteSize("10MB");
    ByteSize byteSize2 = new ByteSize("1.5GB");
    
    List<ByteSize> byteSizes = Arrays.asList(byteSize1, byteSize2);
    ByteSizeList byteSizeList = new ByteSizeList(byteSizes);
    
    JsonElement json = byteSizeList.toJson();
    Assert.assertTrue(json.isJsonObject());
    
    JsonObject jsonObject = json.getAsJsonObject();
    Assert.assertEquals("BYTE_SIZE_LIST", jsonObject.get("type").getAsString());
    
    JsonArray valueArray = jsonObject.getAsJsonArray("value");
    Assert.assertEquals(2, valueArray.size());
    
    JsonObject firstItem = valueArray.get(0).getAsJsonObject();
    Assert.assertEquals("BYTE_SIZE", firstItem.get("type").getAsString());
    Assert.assertEquals("10MB", firstItem.get("value").getAsString());
    
    JsonObject secondItem = valueArray.get(1).getAsJsonObject();
    Assert.assertEquals("BYTE_SIZE", secondItem.get("type").getAsString());
    Assert.assertEquals("1.5GB", secondItem.get("value").getAsString());
  }
  
  @Test
  public void testEquality() {
    ByteSize byteSize1 = new ByteSize("10MB");
    ByteSize byteSize2 = new ByteSize("1.5GB");
    
    ByteSizeList list1 = new ByteSizeList(Arrays.asList(byteSize1, byteSize2));
    ByteSizeList list2 = new ByteSizeList(Arrays.asList(byteSize1, byteSize2));
    ByteSizeList list3 = new ByteSizeList(Arrays.asList(byteSize2, byteSize1)); // Different order
    
    Assert.assertEquals(list1, list2);
    Assert.assertNotEquals(list1, list3);
    Assert.assertEquals(list1.hashCode(), list2.hashCode());
    Assert.assertNotEquals(list1.hashCode(), list3.hashCode());
  }
  
  @Test
  public void testToString() {
    ByteSize byteSize1 = new ByteSize("10MB");
    ByteSize byteSize2 = new ByteSize("1.5GB");
    
    ByteSizeList byteSizeList = new ByteSizeList(Arrays.asList(byteSize1, byteSize2));
    Assert.assertEquals("[10MB, 1.5GB]", byteSizeList.toString());
  }
  
  @Test
  public void testEmptyList() {
    ByteSizeList emptyList = new ByteSizeList(new ArrayList<>());
    
    Assert.assertTrue(emptyList.getByteSizes().isEmpty());
    Assert.assertFalse(emptyList.iterator().hasNext());
    Assert.assertEquals("[]", emptyList.toString());
    
    JsonElement json = emptyList.toJson();
    JsonObject jsonObject = json.getAsJsonObject();
    JsonArray valueArray = jsonObject.getAsJsonArray("value");
    Assert.assertEquals(0, valueArray.size());
  }
}