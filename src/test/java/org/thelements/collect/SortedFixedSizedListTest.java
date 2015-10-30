package org.thelements.collect;

import java.util.Iterator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.theelements.collect.SortedFixedSizedList;

public class SortedFixedSizedListTest {

  private SortedFixedSizedList<Integer> list;

  @Before
  public void setUp() throws Exception {
    list = new SortedFixedSizedList<Integer>(3);
  }

  public void assertEquals(SortedFixedSizedList<Integer> list, Integer... expected) {
    Iterator<Integer> itr = list.iterator();
    for (int i = 0; itr.hasNext(); i++) {
      Integer actual = itr.next();
      Assert.assertEquals(expected[i], actual);
    }
  }

  @Test
  public void testMaybeAdd() {
    list.maybeAdd(1);
    list.maybeAdd(2);
    list.maybeAdd(3);
    list.maybeAdd(4);
    list.maybeAdd(5);

    Assert.assertEquals(3, list.size());
    assertEquals(list, 5, 4, 3);
  }

  @Test
  public void testMaybeAdd2() {
    list.maybeAdd(3);
    list.maybeAdd(13);
    list.maybeAdd(1);
    list.maybeAdd(8);
    list.maybeAdd(25);
    list.maybeAdd(7);

    Assert.assertEquals(3, list.size());
    assertEquals(list, 25, 13, 8);
  }

}
