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
import io.cdap.wrangler.api.annotations.PublicEvolving;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * This class represents a list of byte size values.
 * It is used to hold multiple ByteSize tokens in a directive.
 */
@PublicEvolving
public class ByteSizeList implements Token, Iterable<ByteSize> {
  private final List<ByteSize> byteSizes;

  /**
   * Constructor to create a ByteSizeList from a list of ByteSize objects.
   *
   * @param byteSizes list of ByteSize objects
   */
  public ByteSizeList(List<ByteSize> byteSizes) {
    this.byteSizes = Collections.unmodifiableList(byteSizes);
  }

  /**
   * Returns the list of byte sizes.
   *
   * @return unmodifiable list of ByteSize objects
   */
  public List<ByteSize> getByteSizes() {
    return byteSizes;
  }

  /**
   * Returns the raw list of ByteSize objects.
   *
   * @return list of ByteSize objects
   */
  @Override
  public Object value() {
    return byteSizes;
  }

  /**
   * Returns the token type.
   *
   * @return BYTE_SIZE_LIST token type
   */
  @Override
  public TokenType type() {
    return TokenType.BYTE_SIZE_LIST;
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
    JsonArray array = new JsonArray();
    for (ByteSize byteSize : byteSizes) {
      array.add(byteSize.toJson());
    }
    object.add("value", array);
    return object;
  }

  /**
   * Returns an iterator over the ByteSize objects in this list.
   *
   * @return iterator of ByteSize objects
   */
  @Override
  public Iterator<ByteSize> iterator() {
    return byteSizes.iterator();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ByteSizeList that = (ByteSizeList) o;
    return Objects.equals(byteSizes, that.byteSizes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(byteSizes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    Iterator<ByteSize> iterator = byteSizes.iterator();
    while (iterator.hasNext()) {
      sb.append(iterator.next().toString());
      if (iterator.hasNext()) {
        sb.append(", ");
      }
    }
    sb.append("]");
    return sb.toString();
  }
}