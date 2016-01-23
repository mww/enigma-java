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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.theelements.collect.SortedFixedSizedList;
import org.theelements.enigma.EnigmaMachine.EnigmaMachineConfig;

import com.google.common.collect.Lists;

public class EnigmaRunner {

  private static class Triple<T> {
    public final T o1;
    public final T o2;
    public final T o3;

    public Triple(T o1, T o2, T o3) {
      this.o1 = o1;
      this.o2 = o2;
      this.o3 = o3;
    }
  }

  protected static class EnigmaResult implements Comparable<EnigmaResult> {
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

    public String getMessage() {
      return message;
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
      return Double.compare(other.difference, difference);
      //return Double.compare(difference,  other.difference);
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
  private int numResults = 3;

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
    
    // TODO(mww): Remove this.
    //System.console().readLine("Press enter to start");

    SortedFixedSizedList<EnigmaResult> results = run(messageArray, rotorsToUse, reflectorsToUse,
        numResults, numThreads);

    for (EnigmaResult result : results) {
      System.out.println(result);
      System.out.println("==============================");
    }
  }

  // TODO(mww): Benchmark and then convert this to use java 8. I should be able to make a
  // stream out of all of the tasks, use CompletableFuture.supplyAsync() with the executor
  // to create a bunch of CompletableFutures that I can then add to the output list. Want a
  // benchmark before and after to see the new method is any faster.
  //
  // Benchmark pre-java 8 version. Using:
  // time java -jar build/libs/enigma-java-0.1-all.jar \
  //   -message=ZTQBLVXKPBPGAVQBRYDYQEZNKRLMZTMRGBJSQKHDPHHNTNIDLYVFCOKZYYSMJFAHQBTEAVFKOXRPSQX
  // Over 3 runs I saw real times of:
  // 0m11.120s, 0m11.335s, and 0m10.704s
  //
  // The java 8 version had real times of:
  // 0m19.946s, 0m20.340s, 0m19.811s
  //
  // So the new version is much slower.
  // My initial guess is this has to do with combining the results and the fact that
  // SortedFixedSizedList is single threaded and therefore running on its own executor. Perhaps
  // I can make it thread safe, or perhaps I can make a single one for each thread in the
  // thread pool, and add the results there as the task finishes. Then I can combine all of the
  // results.
  //
  // I should profile the code and verify this hunch before I spend much time trying to
  // improve the results.
  public SortedFixedSizedList<EnigmaResult> run(char[] message, List<Rotor> rotorList,
      List<Rotor> reflectors, int numResults, int numThreads) throws Exception {
    SortedFixedSizedList<EnigmaResult> finalResults =
        new SortedFixedSizedList<EnigmaResult>(numResults);
    ExecutorService executor = Executors.newFixedThreadPool(numThreads);
    ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

    List<Character> letters = new ArrayList<>(26);
    for (Character c : "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()) {
      letters.add(c);
    }
    List<Triple<Character>> startingPositions = permutations(letters, true);
    List<Triple<Rotor>> rotorPermutations = permutations(rotorList, false);

    for (Triple<Rotor> rotors : rotorPermutations) {
      for (Rotor reflector : reflectors) {

        for (Triple<Character> startingPosition : startingPositions) {
          EnigmaMachineConfig config = new EnigmaMachineConfig(startingPosition.o1,
              startingPosition.o2, startingPosition.o3, rotors.o1, rotors.o2, rotors.o3,
              reflector);

          CompletableFuture.supplyAsync(() -> decodeMessage(config, message, crib), executor)
              .thenAcceptAsync((EnigmaResult r) -> finalResults.maybeAdd(r), singleThreadExecutor);
        }
      }
    }

    // Tell the executor to stop accepting new requests
    executor.shutdown();
    // TODO(mww): Need a better way to wait for all the tasks in the executor to finish.
    if (!executor.awaitTermination(2, TimeUnit.MINUTES)) {
    	System.err.println("Executor did not shutdown as expected! Forcing it to stop.");
    	executor.shutdownNow();
    }
    singleThreadExecutor.shutdown();

    return finalResults;
  }
  
  private EnigmaResult decodeMessage(EnigmaMachineConfig config, final char[] message, String crib) {
	  EnigmaMachine machine = EnigmaMachine.getEnigmaMachine(config);
	  FrequencyAnalysis analysis = new FrequencyAnalysis();
	  StringBuilder result = new StringBuilder(message.length);

	  try {
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
	      EnigmaResult enigmaResult = new EnigmaResult(decodedMessage, score, machine.toString());
	      return enigmaResult;
	  } finally {
		  EnigmaMachine.freeEnigmaMachine(machine);
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
