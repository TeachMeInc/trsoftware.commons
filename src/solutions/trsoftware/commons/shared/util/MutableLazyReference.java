/*
 * Copyright 2022 TR Software Inc.
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

package solutions.trsoftware.commons.shared.util;

import java.util.function.Supplier;

/**
 * A {@link LazyReference} that can be modified
 *
 * @author Alex
 */
public abstract class MutableLazyReference<V> extends LazyReference<V> {

  public synchronized void set(V value) {
    this.value = value;
  }

  public void refresh() {
    set(create());
  }

  /**
   * Factory method that uses the given supplier function to implement the {@link #create()} method.
   */
  public static <T> MutableLazyReference<T> fromSupplier(Supplier<T> supplier) {
    return new MutableLazyReference<T>() {
      @Override
      protected T create() {
        return supplier.get();
      }
    };
  }

}
