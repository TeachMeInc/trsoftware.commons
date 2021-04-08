/*
 * Copyright 2021 TR Software Inc.
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

package solutions.trsoftware.commons.server.memquery.struct;

/**
 * A tuple whose elements can be accessed by name.
 *
 * @author Alex, 1/15/14
 */
public interface MutableNamedTuple extends NamedTuple {

  /**
   * @param <V> the value type
   */
  <V> void setValue(String name, V value);
}
