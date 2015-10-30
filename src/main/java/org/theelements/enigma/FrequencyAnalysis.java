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

package org.theelements.enigma;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class FrequencyAnalysis {
  private static final ImmutableMap<Character, Double> ENGLISH_EXPECTED_FREQUENCY =
      ImmutableMap.<Character, Double>builder().put('A', 8.167).put('B', 1.492).put('C', 2.782)
      .put('D', 4.253).put('E', 12.702).put('F', 2.228).put('G', 2.015).put('H', 6.094)
      .put('I', 6.966).put('J', 0.153).put('K', 0.772).put('L', 4.025).put('M', 2.406)
      .put('N', 6.749).put('O', 7.507).put('P', 1.929).put('Q', 0.095).put('R', 5.987)
      .put('S', 6.327).put('T', 9.056).put('U', 2.758).put('V', 0.978).put('W', 2.360)
      .put('X', 0.150).put('Y', 1.974).put('Z', 0.074).build();

  private Map<Character, Long> analysis = Maps.newHashMap();
  private long totalCount = 0;

  public FrequencyAnalysis() {
    for (char c : ENGLISH_EXPECTED_FREQUENCY.keySet()) {
      analysis.put(c, 0L);
    }
  }

  public void add(char c) {
    analysis.put(c, analysis.get(c) + 1);
    totalCount++;
  }

  public double calculateDifference() {
    double totalDifference = 0.0;
    for (char c : ENGLISH_EXPECTED_FREQUENCY.keySet()) {
      long count = analysis.get(c);
      if (count == 0) {
        continue;
      }

      double actual = ((double) count / totalCount) * 100;
      double expected = ENGLISH_EXPECTED_FREQUENCY.get(c);
      totalDifference += Math.abs(actual - expected);
    }

    return totalDifference;
  }

  @Override
  public String toString() {
    return String.format("Total difference: %.3f", calculateDifference());
  }
}
