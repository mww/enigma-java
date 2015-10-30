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

import java.util.concurrent.ConcurrentLinkedQueue;

public class EnigmaMachine {

  private static final char[] LETTERS = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K',
    'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

  public static class EnigmaMachineConfig {
    private char positionA;
    private char positionB;
    private char positionC;
    private Rotor rotorA;
    private Rotor rotorB;
    private Rotor rotorC;
    private Rotor reflector;

    public EnigmaMachineConfig(char positionA, char positionB, char positionC, Rotor rotorA,
        Rotor rotorB, Rotor rotorC, Rotor reflector) {
      this.positionA = positionA;
      this.positionB = positionB;
      this.positionC = positionC;
      this.rotorA = rotorA;
      this.rotorB = rotorB;
      this.rotorC = rotorC;
      this.reflector = reflector;
    }
  }

  private static ConcurrentLinkedQueue<EnigmaMachine> freeList;

  static {
    freeList = new ConcurrentLinkedQueue<EnigmaMachine>();
    for (int i = 0; i < 200000; i++) {
      freeList.add(new EnigmaMachine());
    }
  }

  private Rotor rotor1;
  private Rotor rotor2;
  private Rotor rotor3;
  private Rotor reflector;

  // Index into BASE_MAPPING
  private int position1;
  private int position2;
  private int position3;

  private char rotor1Start;
  private char rotor2Start;
  private char rotor3Start;

  public static EnigmaMachine getEnigmaMachine(EnigmaMachineConfig config) {
    if (freeList.isEmpty()) {
      for (int i = 0; i < 100000; i++) {
        freeList.add(new EnigmaMachine());
      }
    }
    EnigmaMachine machine = freeList.poll();
    machine.init(config);
    return machine;
  }

  public static void freeEnigmaMachine(EnigmaMachine machine) {
    freeList.add(machine);
  }

  private EnigmaMachine() { /* No public constructor. */ }

  private void init(EnigmaMachineConfig config) {
    this.rotor1 = config.rotorA;
    this.rotor2 = config.rotorB;
    this.rotor3 = config.rotorC;
    this.reflector = config.reflector;
    this.position1 = config.positionA - 'A';
    this.position2 = config.positionB - 'A';
    this.position3 = config.positionC - 'A';

    rotor1Start = config.positionA;
    rotor2Start = config.positionB;
    rotor3Start = config.positionC;
  }

  public char step(final char letter) {
    moveRotors();

    int stepValue = getOutputIndex(rotor3, position3, letter - 'A', false);
    stepValue = getOutputIndex(rotor2, position2, stepValue, false);
    stepValue = getOutputIndex(rotor1, position1, stepValue, false);
    stepValue = getOutputIndex(reflector, 0, stepValue, false);
    stepValue = getOutputIndex(rotor1, position1, stepValue, true);
    stepValue = getOutputIndex(rotor2, position2, stepValue, true);
    stepValue = getOutputIndex(rotor3, position3, stepValue, true);
    return LETTERS[stepValue];
  }

  protected int getOutputIndex(Rotor rotor, int rotorOffset, int inputIndex, boolean reverse) {
    int value = (inputIndex + rotorOffset) % 26;
    char letter = LETTERS[value];
    letter = rotor.get(letter, reverse);
    value = (letter - 'A') - rotorOffset;
    if (value < 0) {
      value += 26;
    }
    return value;
  }

  private void moveRotors() {
    position3 = (position3 + 1) % 26;
    if (rotor3.turnover(position3)) {
      position2 = (position2 + 1) % 26;
      if (rotor2.turnover(position2)) {
        position1 = (position1 + 1) % 26;
      }
    }

    // Handles double-stepping case.
    if (rotor3.turnover(position3 - 1) && rotor2.turnover(position2 + 1)) {
      position2 = (position2 + 1) % 26;
      if (rotor2.turnover(position2)) {
        position1 = (position1 + 1) % 26;
      }
    }
  }

  protected int[] moveRotorsWithResult() {
    moveRotors();
    return new int[] {position1, position2, position3};
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append("KEY: ").append(rotor1Start).append(rotor2Start).append(rotor3Start).append("\n");
    buf.append("ROTORS: ");
    buf.append(rotor1.toString()).append(", ");
    buf.append(rotor2.toString()).append(", ");
    buf.append(rotor3.toString()).append("\n");
    buf.append("REFLECTOR: " ).append(reflector.toString()).append("\n");
    return buf.toString();
  }
}
