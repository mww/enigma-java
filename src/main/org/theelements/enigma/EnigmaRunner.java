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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.theelements.enigma.EnigmaMachine.EnigmaMachineConfig;

import com.google.common.collect.Lists;

public class EnigmaRunner {

  private static class Triple<T> {
    public T o1;
    public T o2;
    public T o3;

    public Triple(T o1, T o2, T o3) {
      this.o1 = o1;
      this.o2 = o2;
      this.o3 = o3;
    }
  }

  private static class EnigmaResult implements Comparable<EnigmaResult> {
    private String message;
    private String settings;
    private double difference;

    public EnigmaResult(String decodedMessage, double frequencyDifference, String settings) {
      this.message = decodedMessage;
      this.settings = settings;
      this.difference = frequencyDifference;
    }

    public double getDifference() {
      return difference;
    }

    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder();
      buf.append("SETTINGS:\n").append(settings).append("\n");
      buf.append("FREQUENCY SCORE (smaller is better): ").append(difference).append("\n");
      buf.append("MESSAGE:\n").append(message).append("\n");
      return buf.toString();
    }

    @Override
    public int compareTo(EnigmaResult other) {
      return Double.compare(difference, other.difference);
    }
  }

  private static class EnigmaCallable implements Callable<EnigmaResult> {
    private EnigmaMachine machine;
    private FrequencyAnalysis analysis;
    private final char[] message;
    private String crib;

    public EnigmaCallable(EnigmaMachineConfig config, final char[] message, String crib) {
      this.message = message;
      this.crib = crib;
      machine = new EnigmaMachine(config);
      analysis = new FrequencyAnalysis();
    }

    @Override
    public EnigmaResult call() throws Exception {
      StringBuilder result = new StringBuilder();

      char letter;
      for (char c : message) {
        letter = machine.step(c);
        result.append(letter);
        analysis.add(letter);
      }

      String decodedMessage = result.toString();
      double score = analysis.calculateDifference();
      if (crib != null) {
        if (decodedMessage.contains(crib)) {
          System.out.println("=== Found crib in the message: " + decodedMessage + " ===");
          score -= crib.length() * 100;
        }
      }
      return new EnigmaResult(decodedMessage, score, machine.toString());
    }
  }

  @Option(name="-message", usage="The encrypted message to crack.")
  private String message = null;

  @Option(name="-crib", usage="Provide a crib if you have one.")
  private String crib = null;

  @Option(name="-rotors", usage="The rotors to use while decrypting the message.")
  private String rotors = "1, 2, 3, 4, 5";

  @Option(name="-reflectors", usage="The reflectors to use while decrypting the message.")
  private String reflectors = "A, B, C";

  @Option(name="-num_threads", usage="The number of threads to use when running.")
  private int numThreads = 8;

  @Option(name="-results", usage="The number of results to display.")
  private int numResults = 10;

  public void doMain(String[] args) throws Exception {
    CmdLineParser parser = new CmdLineParser(this);
    parser.parseArgument(args);

    if (message == null) {
      throw new Exception("You must provide a message to be cracked.");
    }
    char[] messageArray = message.toCharArray();

    List<Rotor> rotorsToUse = Lists.newArrayList();
    String [] splitRotors = rotors.split(",");
    if (splitRotors.length < 3) {
      throw new Exception("You must specifiy at least 3 rotors.");
    }
    for (String rotorName : rotors.split(",")) {
      Rotor rotor = Rotor.getRotorByName(rotorName.trim());
      if (rotor == null) {
        throw new Exception(
            String.format("Rotor %s does not exist, check your command line.", rotorName));
      }
      rotorsToUse.add(rotor);
    }

    List<Rotor> reflectorsToUse = Lists.newArrayList();
    String[] splitReflectors = reflectors.split(",");
    if (splitReflectors.length < 1) {
      throw new Exception("You must specifiy at least 1 reflector.");
    }
    for (String reflectorName : splitReflectors) {
      Rotor reflector = Rotor.getRotorByName(reflectorName.trim());
      if (reflector == null) {
        throw new Exception(
            String.format("Reflector %s does not exist, check your command line.", reflectorName));
      }
      reflectorsToUse.add(reflector);
    }

    List<EnigmaResult> finalResults = Lists.newArrayListWithCapacity(numResults);
    ExecutorService executor = Executors.newFixedThreadPool(numThreads);

    List<Character> letters = Lists.newArrayListWithCapacity(26);
    for (Character c : "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()) {
      letters.add(c);
    }
    List<Triple<Character>> startingPositions = permutations(letters, true);
    List<Triple<Rotor>> rotorPermutations = permutations(rotorsToUse, false);

    for (Triple<Rotor> rotors : rotorPermutations) {
      // TODO(mww): Come up with the correct initial capacity.
      for (Rotor reflector : reflectorsToUse) {

        List<EnigmaCallable> tasks = Lists.newArrayListWithCapacity(1000);
        for (Triple<Character> startingPosition : startingPositions) {
          EnigmaMachineConfig config = new EnigmaMachineConfig(startingPosition.o1,
              startingPosition.o2, startingPosition.o3, rotors.o1, rotors.o2, rotors.o3,
              reflector);
          tasks.add(new EnigmaCallable(config, messageArray, crib));
        }

        System.out.println(String.format("Running config: %s, %s, %s, %s", rotors.o1, rotors.o2,
            rotors.o3, reflector));
        List<Future<EnigmaResult>> results = executor.invokeAll(tasks);

        for (Future<EnigmaResult> future : results) {
          EnigmaResult result = future.get();
          addResultToQueue(result, finalResults);
        }
      }
    }

    executor.shutdown();
    Collections.sort(finalResults);

    for (EnigmaResult result : finalResults) {
      System.out.println(result);
      System.out.println("==============================");
    }
  }

  private void addResultToQueue(EnigmaResult result, List<EnigmaResult> list) {
    synchronized(list) {
      if (list.size() < numResults) {
        list.add(result);
        Collections.sort(list);
        return;
      }

      if (result.getDifference() < list.get(numResults - 1).getDifference()) {
        list.remove(numResults - 1);
        list.add(result);
        Collections.sort(list);
      }
    }
  }

  private <T> List<Triple<T>> permutations(List<T> items, boolean duplicates) {
    List<Triple<T>> permutations = Lists.newArrayList();
    for (T o1 : items) {
      for (T o2 : items) {
        if (!duplicates && o1.equals(o2)) {
          continue;
        }

        for (T o3 : items) {
          if (!duplicates && (o1.equals(o2) || o1.equals(o3) || o2.equals(o3))) {
            continue;
          }
          permutations.add(new Triple<T>(o1, o2, o3));
        }
      }
    }

    return permutations;
  }

  public static void main(String[] args) throws Exception {
    new EnigmaRunner().doMain(args);
  }

}
