/*
 * Copyright 2012 Mark Weaver
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.theelements.collect;

import java.util.Iterator;

public class SortedFixedSizedList<E extends Comparable<E>> implements Iterable<E> {
  private static class Node<E> {
    E data;
    Node<E> next;
    Node<E> prev;

    Node(E data) {
      this.data = data;
    }
  }

  private static class ListIterator<E> implements Iterator<E> {
    private Node<E> current;

    ListIterator(Node<E> head) {
      this.current = head;
    }

    @Override
    public boolean hasNext() {
      return current != null;
    }

    @Override
    public E next() {
      E data = current.data;
      current = current.next;
      return data;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("remove not supported");
    }
  }

  public SortedFixedSizedList(int capacity) {
    this.size = 0;
    this.capacity = capacity;
  }

  private Node<E> head;
  private Node<E> tail;
  private int capacity;
  private int size;

  public E maybeAdd(E data) {
    if (size < capacity) {
      Node<E> node = new Node<E>(data);

      if (head == null && tail == null) {
        head = node;
        tail = node;
      } else {
        addToList(node);
      }

      size++;
      return null;
    } else {
      if (tail.data.compareTo(data) < 0) {
        Node<E> node = tail;
        tail = tail.prev;
        tail.next = null;

        E oldData = node.data;
        node.data = data;
        addToList(node);
        return oldData;
      }
    }

    // Not adding the new item, so return it
    return data;
  }

  // Call this only when you're sure you want to add the item to the list.
  private void addToList(Node<E> node) {
    if (head.data.compareTo(node.data) < 0) {
      // Adding before head
      head.prev = node;
      node.next = head;
      head = node;
      head.prev = null;
      return;
    } else if (node.data.compareTo(tail.data) < 0){
      // Adding after tail
      tail.next = node;
      node.prev = tail;
      tail = node;
      tail.next = null;
      return;
    }

    // Add somewhere in the middle
    Node<E> current = tail;
    while (current.data.compareTo(node.data) < 0) {
      current = current.prev;
      if (current == null) {
        // This should never happen
        throw new RuntimeException("Can't find the correct place to add new data.");
      }
    }

    node.prev = current;
    node.next = current.next;
    current.next = node;
    node.next.prev = node;
  }

  public int size() {
    return size;
  }

  @Override
  public Iterator<E> iterator() {
    return new ListIterator<E>(head);
  }
}
