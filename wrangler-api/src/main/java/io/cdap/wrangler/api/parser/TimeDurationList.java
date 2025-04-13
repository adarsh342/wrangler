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
 * This class represents a list of time duration values.
 * It is used to hold multiple TimeDuration tokens in a directive.
 */
@PublicEvolving
public class TimeDurationList implements Token, Iterable<TimeDuration> {
  private final List<TimeDuration> timeDurations;

  /**
   * Constructor to create a TimeDurationList from a list of TimeDuration objects.
   *
   * @param timeDurations list of TimeDuration objects
   */
  public TimeDurationList(List<TimeDuration> timeDurations) {
    this.timeDurations = Collections.unmodifiableList(timeDurations);
  }

  /**
   * Returns the list of time durations.
   *
   * @return unmodifiable list of TimeDuration objects
   */
  public List<TimeDuration> getTimeDurations() {
    return timeDurations;
  }

  /**
   * Returns the raw list of TimeDuration objects.
   *
   * @return list of TimeDuration objects
   */
  @Override
  public Object value() {
    return timeDurations;
  }

  /**
   * Returns the token type.
   *
   * @return TIME_DURATION_LIST token type
   */
  @Override
  public TokenType type() {
    return TokenType.TIME_DURATION_LIST;
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
    for (TimeDuration timeDuration : timeDurations) {
      array.add(timeDuration.toJson());
    }
    object.add("value", array);
    return object;
  }

  /**
   * Returns an iterator over the TimeDuration objects in this list.
   *
   * @return iterator of TimeDuration objects
   */
  @Override
  public Iterator<TimeDuration> iterator() {
    return timeDurations.iterator();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TimeDurationList that = (TimeDurationList) o;
    return Objects.equals(timeDurations, that.timeDurations);
  }

  @Override
  public int hashCode() {
    return Objects.hash(timeDurations);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    Iterator<TimeDuration> iterator = timeDurations.iterator();
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