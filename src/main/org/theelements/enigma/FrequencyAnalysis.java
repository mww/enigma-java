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

  private static final ImmutableMap<String, Double> ENGLISH_DIGRAPH_FREQUENCY =
      ImmutableMap.<String, Double>builder().put("TH", 1.52).put("HE", 1.28).put("IN", 0.94)
      .put("ER", 0.94).put("AN", 0.82).put("RE", 0.68).put("ND", 0.63).put("AT", 0.59)
      .put("ON", 0.57).put("NT", 0.56).put("HA", 0.56).put("ES", 0.56).put("ST", 0.55)
      .put("EN", 0.55).put("TO", 0.52).put("IT", 0.50).put("OU", 0.50).put("EA", 0.47)
      .put("HI", 0.46).put("IS", 0.46).put("OR", 0.43).put("TI", 0.34).put("AS", 0.33)
      .put("TE", 0.27).put("ET", 0.19).build();

  private Map<Character, Long> analysis = Maps.newHashMap();
  private Map<String, Long> digraphAnalysis = Maps.newHashMap();
  private long totalCount = 0;
  private char previousChar = ' ';

  public FrequencyAnalysis() {
    for (char c : ENGLISH_EXPECTED_FREQUENCY.keySet()) {
      analysis.put(c, 0L);
    }

    for (String digraph : ENGLISH_DIGRAPH_FREQUENCY.keySet()) {
      digraphAnalysis.put(digraph, 0L);
    }
  }

  public void add(char c) {
    String digraph = new String(new char[] {previousChar, c});
    if (digraphAnalysis.containsKey(digraph)) {
      digraphAnalysis.put(digraph, digraphAnalysis.get(digraph) + 1);
    }

    analysis.put(c, analysis.get(c) + 1);
    previousChar = c;
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

    for (String digraph : ENGLISH_DIGRAPH_FREQUENCY.keySet()) {
      long count = digraphAnalysis.get(digraph);
      double expected = ENGLISH_DIGRAPH_FREQUENCY.get(digraph);
      if (count == 0) {
        totalDifference += (expected * 100);
        continue;
      }

      double actual = ((double) count / (totalCount - 1)) * 100;
      totalDifference += Math.abs(actual - expected);
    }
    return totalDifference;
  }

  @Override
  public String toString() {
    return String.format("Total difference: %.3f", calculateDifference());
  }
}
