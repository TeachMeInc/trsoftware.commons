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

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.function.Supplier;

import static org.junit.Assert.assertArrayEquals;
import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertArraysEqual;
import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertThrows;
import static solutions.trsoftware.commons.shared.util.ArrayUtils.*;

public class ArrayUtilsTest extends TestCase {

  public void testFlexibleArrayAdd() {
    assertArrayEquals(new String[]{"a", "b", "c", "d"},
        flexibleArrayAdd(new String[]{"a", "b", "c"}, 3, "d"));
    assertArrayEquals(new String[]{"a", "b", "c", null, "d"},
        flexibleArrayAdd(new String[]{"a", "b", "c"}, 4, "d"));
    assertArrayEquals(new String[]{null, null, null, "d"},
        flexibleArrayAdd(null, 3, "d"));
    assertArrayEquals(new String[]{"a", "b", "d"},
        flexibleArrayAdd(new String[]{"a", "b", "c"}, 2, "d"));
  }

  public void testFlexibleArrayPrimitiveFloat() {
    // start with an empty array and test a few base cases, and the rest will follow
    // by induction
    float[] a = new float[0];
    int i = 0;
    a = flexibleArrayAdd(a, i++, 1f);
    assertEquals(2, a.length);  // should have been grown to size max(2, ceil(1.5*length)) => 2
    assertTrue(Arrays.equals(new float[]{1f,0f}, a));

    a = flexibleArrayAdd(a, i++, 2f);
    assertEquals(2, a.length);  // should still be the same size as before
    assertTrue(Arrays.equals(new float[]{1,2}, a));

    a = flexibleArrayAdd(a, i++, 3f);
    assertEquals(3, a.length);  // should have been grown to size max(2, ceil(1.5*length)) => 3
    assertTrue(Arrays.equals(new float[]{1,2,3}, a));

    a = flexibleArrayAdd(a, i++, 4f);
    assertEquals(5, a.length);  // should have been grown to size max(2, ceil(1.5*length)) => 5
    assertTrue(Arrays.equals(new float[]{1,2,3,4,0}, a));

    a = flexibleArrayAdd(a, i++, 5f);
    assertEquals(5, a.length);  // should still be the same size as before
    assertTrue(Arrays.equals(new float[]{1,2,3,4,5}, a));

    a = flexibleArrayAdd(a, i++, 6f);
    assertEquals(8, a.length);  // should have been grown to size max(2, ceil(1.5*length)) => 8
    assertTrue(Arrays.equals(new float[]{1,2,3,4,5,6,0,0}, a));
    // we assume the rest is correct by induction
  }

  public void testFilterIntArray() {
    assertTrue(Arrays.equals(
        new int[]{2, 5, 1},
        ArrayUtils.filter(
            new int[]{8, 2, 5, 1, 8, 9, 1000},
            item -> item <= 5)
    ));
  }

  public void testFilterStringArray() {
    assertTrue(Arrays.equals(
        new String[]{"foo", "bar", "baz"},
        filter(
            new String[]{"foo", "a", "bar", "cigar", "baz"},
            item -> item.length() == 3).toArray()
    ));
  }

  public void testIntSwap() throws Exception {
    assertTrue(Arrays.equals(new int[]{1, 2, 3},
        swap(new int[]{2, 1, 3}, 0, 1)));
  }

  public void testObjectSwap() throws Exception {
    assertTrue(Arrays.equals(new String[]{"foo", "bar", "baz"},
        swap(new String[]{"baz", "bar", "foo"}, 0, 2)));
  }

  public void testConcat() throws Exception {
    String[] result = concat(
        new String[]{"a"},
        new String[]{"b", "c"},
        new String[]{},
        new String[]{"d"},
        new String[]{}
    );
    assertTrue(Arrays.equals(new String[]{"a", "b", "c", "d"}, result));
  }

  public void testInterleave() throws Exception {
    String[] result = interleave(
        new String[]{"a", "b"},
        new String[]{"c", "d", "e"},
        new String[]{"f"}
    );
    assertTrue(Arrays.equals(new String[]{"a", "c", "f", "b", "d", "e"}, result));
  }

  public void testUnbox() throws Exception {
    double[] result = unbox(new Double[]{1d, 2d, 3d});
    assertEquals(3, result.length);
    assertEquals(1d, result[0]);
    assertEquals(2d, result[1]);
    assertEquals(3d, result[2]);
  }

  public void testIndexOfChar() throws Exception {
    assertEquals(-1, indexOf(new char[]{'a', 'b', 'c'}, 'd'));
    assertEquals(-1, indexOf(new char[]{'a'}, 'd'));
    assertEquals(-1, indexOf(new char[0], 'd'));
    assertEquals(0, indexOf(new char[]{'a', 'b', 'c'}, 'a'));
    assertEquals(1, indexOf(new char[]{'a', 'b', 'c'}, 'b'));
    assertEquals(2, indexOf(new char[]{'a', 'b', 'c'}, 'c'));
    assertEquals(0, indexOf(new char[]{'a', 'b'}, 'a'));
    assertEquals(1, indexOf(new char[]{'a', 'b'}, 'b'));
    assertEquals(0, indexOf(new char[]{'a'}, 'a'));
  }

  public void testIndexOfInt() throws Exception {
    assertEquals(-1, indexOf(new int[]{1, 2, 3}, 4));
    assertEquals(-1, indexOf(new int[]{1}, 4));
    assertEquals(-1, indexOf(new int[0], 4));
    assertEquals(0, indexOf(new int[]{1, 2, 3}, 1));
    assertEquals(0, indexOf(new int[]{1, 1, 2, 3}, 1));  // should return first match if more than one
    assertEquals(1, indexOf(new int[]{1, 2, 3}, 2));
    assertEquals(2, indexOf(new int[]{1, 2, 3}, 3));
    assertEquals(0, indexOf(new int[]{1, 2}, 1));
    assertEquals(1, indexOf(new int[]{1, 2}, 2));
    assertEquals(0, indexOf(new int[]{1}, 1));
  }

  public void testIndexOfObject() throws Exception {
    assertEquals(-1, indexOf(new String[]{"a", "b", "c"}, "d"));
    assertEquals(-1, indexOf(new String[]{"a"}, "d"));
    assertEquals(-1, indexOf(new String[0], "d"));
    assertEquals(0, indexOf(new String[]{"a", "b", "c"}, "a"));
    assertEquals(0, indexOf(new String[]{"a", "a", "b", "c"}, "a"));  // should return first match if more than one
    assertEquals(1, indexOf(new String[]{"a", "b", "c"}, "b"));
    assertEquals(2, indexOf(new String[]{"a", "b", "c"}, "c"));
    assertEquals(0, indexOf(new String[]{"a", "b"}, "a"));
    assertEquals(1, indexOf(new String[]{"a", "b"}, "b"));
    assertEquals(0, indexOf(new String[]{"a"}, "a"));
    // also test null values
    assertEquals(-1, indexOf(new String[]{null}, "a"));
    assertEquals(0, indexOf(new String[]{null}, null));
    assertEquals(1, indexOf(new String[]{"a", null}, null));
    assertEquals(-1, indexOf(new String[]{"a", "b"}, null));
  }

  public void testContains() throws Exception {
    assertFalse(contains(new String[]{"a", "b", "c"}, "d"));
    assertFalse(contains(new String[]{"a"}, "d"));
    assertFalse(contains(new String[0], "d"));
    assertTrue(contains(new String[]{"a", "b", "c"}, "a"));
    assertTrue(contains(new String[]{"a", "b", "c"}, "b"));
    assertTrue(contains(new String[]{"a", "b", "c"}, "c"));
    assertTrue(contains(new String[]{"a", "b"}, "a"));
    assertTrue(contains(new String[]{"a", "b"}, "b"));
    assertTrue(contains(new String[]{"a"}, "a"));
    // also test null values
    assertFalse(contains(new String[]{null}, "a"));
    assertFalse(contains(new String[]{"a", "b"}, null));
    assertTrue(contains(new String[]{null}, null));
    assertTrue(contains(new String[]{"a", null}, null));
  }

  public void testContainsInt() throws Exception {
    assertFalse(contains(new int[]{1, 2, 3}, 4));
    assertFalse(contains(new int[]{1}, 4));
    assertFalse(contains(new int[0], 4));
    assertTrue(contains(new int[]{1, 1, 2, 3}, 1));
    assertTrue(contains(new int[]{1, 2, 3}, 2));
    assertTrue(contains(new int[]{1, 2, 3}, 3));
    assertTrue(contains(new int[]{1, 2}, 1));
    assertTrue(contains(new int[]{1, 2}, 2));
    assertTrue(contains(new int[]{1}, 1));
  }

  public void testSlice() throws Exception {
    assertEquals(Arrays.asList("c", "d", "e"), slice(new String[]{"a", "b", "c", "d", "e", "f"}, 2, 4));
    assertEquals(Arrays.asList("e", "f"), slice(new String[]{"a", "b", "c", "d", "e", "f"}, 4, 5));
    assertEquals(Arrays.asList("a"), slice(new String[]{"a", "b", "c", "d", "e", "f"}, 0, 0));
  }

  public void testGetLast() throws Exception {
    assertThrows(NullPointerException.class, new Runnable() {
      public void run() {
        getLast(null);
      }
    });
    assertThrows(ArrayIndexOutOfBoundsException.class, new Runnable() {
      public void run() {
        getLast(new Object[0]);
      }
    });
    assertEquals("a", getLast(new String[]{"a"}));
    assertEquals("b", getLast(new String[]{"a", "b"}));
    assertEquals("c", getLast(new String[]{"a", "b", "c"}));
  }

  public void testCheckBounds() throws Exception {
    // 1) check some cases that shouldn't throw an exception
    for (int arrayLength = 1; arrayLength < 10; arrayLength++) {
      for (int i = 0; i < arrayLength; i++) {
        checkBounds(arrayLength, i);
      }
    }
    // 2) check some cases that should throw an exception
    assertThrows(new ArrayIndexOutOfBoundsException(0), new Runnable() {
      @Override
      public void run() {
        checkBounds(0, 0);
      }
    });
    assertThrows(new ArrayIndexOutOfBoundsException(1), new Runnable() {
      @Override
      public void run() {
        checkBounds(1, 1);
      }
    });
    assertThrows(new ArrayIndexOutOfBoundsException(-1), new Runnable() {
      @Override
      public void run() {
        checkBounds(2, -1);
      }
    });
  }

  public void testFill() throws Exception {
    Integer[] expected = {0, 1, 2, 3, 4};
    Integer[] array = new Integer[expected.length];
    Integer[] result = fill(array, new Supplier<Integer>() {
      private int next;
      @Override
      public Integer get() {
        return next++;
      }
    });
    assertSame(array, result);
    assertArraysEqual(expected, result);
  }

  public void testMerge() throws Exception {
    assertArraysEqual(new String[]{"a", "b", "c", "d"}, merge(new String[]{"a", "b"}, new String[]{"c", "d"}));
    assertArraysEqual(new String[]{"a", "b", "c", "d"}, merge(new String[]{"a", "b", "c", "d"}, new String[]{}));
    assertArraysEqual(new String[]{"a", "b", "c", "d"}, merge(new String[]{}, new String[]{"a", "b", "c", "d"}));
    assertArraysEqual(new String[]{}, merge(new String[]{}, new String[]{}));
  }
}