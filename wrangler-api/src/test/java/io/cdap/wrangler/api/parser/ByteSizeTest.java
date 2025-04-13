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
 * Tests for {@link ByteSize} class.
 */
public class ByteSizeTest {

  @Test
  public void testValidByteSizes() {
    // Test bytes
    ByteSize byteSize = new ByteSize("10B");
    Assert.assertEquals(10, byteSize.getBytes());
    Assert.assertEquals(10.0, byteSize.getValue(), 0.001);
    Assert.assertEquals("B", byteSize.getUnit());
    
    // Test kilobytes
    byteSize = new ByteSize("1.5KB");
    Assert.assertEquals(1536, byteSize.getBytes()); // 1.5 * 1024
    Assert.assertEquals(1.5, byteSize.getValue(), 0.001);
    Assert.assertEquals("KB", byteSize.getUnit());
    
    // Test megabytes
    byteSize = new ByteSize("2MB");
    Assert.assertEquals(2 * 1024 * 1024, byteSize.getBytes());
    Assert.assertEquals(2.0, byteSize.getValue(), 0.001);
    Assert.assertEquals("MB", byteSize.getUnit());
    
    // Test gigabytes
    byteSize = new ByteSize("3.75GB");
    Assert.assertEquals((long)(3.75 * 1024 * 1024 * 1024), byteSize.getBytes());
    Assert.assertEquals(3.75, byteSize.getValue(), 0.001);
    Assert.assertEquals("GB", byteSize.getUnit());
    
    // Test terabytes
    byteSize = new ByteSize("1TB");
    Assert.assertEquals(1024L * 1024 * 1024 * 1024, byteSize.getBytes());
    Assert.assertEquals(1.0, byteSize.getValue(), 0.001);
    Assert.assertEquals("TB", byteSize.getUnit());
    
    // Test petabytes
    byteSize = new ByteSize("0.5PB");
    Assert.assertEquals((long)(0.5 * 1024 * 1024 * 1024 * 1024 * 1024), byteSize.getBytes());
    Assert.assertEquals(0.5, byteSize.getValue(), 0.001);
    Assert.assertEquals("PB", byteSize.getUnit());
    
    // Test with different spacing
    byteSize = new ByteSize("100 KB");
    Assert.assertEquals(100 * 1024, byteSize.getBytes());
    Assert.assertEquals(100.0, byteSize.getValue(), 0.001);
    Assert.assertEquals("KB", byteSize.getUnit());
    
    // Test with lowercase units
    byteSize = new ByteSize("200kb");
    Assert.assertEquals(200 * 1024, byteSize.getBytes());
    Assert.assertEquals(200.0, byteSize.getValue(), 0.001);
    Assert.assertEquals("KB", byteSize.getUnit());
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInvalidByteSize() {
    new ByteSize("not a byte size");
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInvalidByteSizeUnit() {
    new ByteSize("10XB");
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testMissingUnit() {
    new ByteSize("10");
  }
  
  @Test
  public void testToJson() {
    ByteSize byteSize = new ByteSize("10MB");
    JsonElement json = byteSize.toJson();
    Assert.assertTrue(json.isJsonObject());
    
    JsonObject jsonObject = json.getAsJsonObject();
    Assert.assertEquals("BYTE_SIZE", jsonObject.get("type").getAsString());
    Assert.assertEquals("10MB", jsonObject.get("value").getAsString());
    Assert.assertEquals(10 * 1024 * 1024, jsonObject.get("bytes").getAsLong());
  }
  
  @Test
  public void testTokenType() {
    ByteSize byteSize = new ByteSize("10MB");
    Assert.assertEquals(TokenType.BYTE_SIZE, byteSize.type());
  }
  
  @Test
  public void testValue() {
    ByteSize byteSize = new ByteSize("10MB");
    Assert.assertEquals("10MB", byteSize.value());
  }
  
  @Test
  public void testEquality() {
    ByteSize byteSize1 = new ByteSize("10MB");
    ByteSize byteSize2 = new ByteSize("10MB");
    ByteSize byteSize3 = new ByteSize("10KB");
    
    Assert.assertEquals(byteSize1, byteSize2);
    Assert.assertNotEquals(byteSize1, byteSize3);
    Assert.assertEquals(byteSize1.hashCode(), byteSize2.hashCode());
    Assert.assertNotEquals(byteSize1.hashCode(), byteSize3.hashCode());
  }
  
  @Test
  public void testToString() {
    ByteSize byteSize = new ByteSize("10MB");
    Assert.assertEquals("10MB", byteSize.toString());
  }
}