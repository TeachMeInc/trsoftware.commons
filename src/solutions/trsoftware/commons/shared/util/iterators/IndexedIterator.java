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

package solutions.trsoftware.commons.shared.util.iterators;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * Base class to help implement iterators over indexed sequences.
 * Essentially an Iterator version of an indexed for-loop.
 *
 * @author Alex, 4/27/2016
 */
public abstract class IndexedIterator<T> implements Iterator<T> {

  /** The starting index value (iteration will start with {@link #i} equal to this value) */
  protected final int start;
  /** Upper limit for the index value (iteration will stop when {@link #i} is greater than or equal to this value) */
  protected final int limit;
  /** The next index value to be returned */
  protected int i;

  /**
   * Creates a new instance to iterate indices in the range {@code [0, limit[}.
   * @param limit the value for {@link #limit}
   */
  public IndexedIterator(int limit) {
   this(0, limit);
  }

  /**
   * Creates a new instance to iterate indices in the range {@code [start, limit[}
   *
   * @param start initial index value (inclusive)
   * @param limit upper limit for the index value (exclusive)
   */
  public IndexedIterator(int start, int limit) {
    this.start = start;
    this.limit = limit;
    i = start;
  }

  protected abstract T get(int idx);

  @Override
  public boolean hasNext() {
    return i < limit;
  }

  @Override
  public T next() {
    maybeThrowNoSuchElement();
    return get(i++);
  }

  /**
   * @throws NoSuchElementException iff {@link #hasNext()} returns {@code false}, to comply with the Iterator interface
   */
  protected void maybeThrowNoSuchElement() {
    if (!hasNext())
      throw new NoSuchElementException();  // to comply with the Iterator interface
  }

  /**
   * @see ListIterator#nextIndex()
   */
  public int nextIndex() {
    return i;
  }

  /**
   * Not supported.
   */
  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
