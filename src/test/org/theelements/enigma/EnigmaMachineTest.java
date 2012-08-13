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

import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.theelements.enigma.EnigmaMachine.EnigmaMachineConfig;

public class EnigmaMachineTest {
  private EnigmaMachine machine;
  private EnigmaMachine machine2;
  private EnigmaMachine machine3;

  @Before
  public void setUp() {
    machine = EnigmaMachine.getEnigmaMachine(new EnigmaMachineConfig('A', 'A', 'A',
        Rotor.ROTOR_1_1930, Rotor.ROTOR_2_1930, Rotor.ROTOR_3_1930, Rotor.REFLECTOR_B));
    machine2 = EnigmaMachine.getEnigmaMachine(new EnigmaMachineConfig('A', 'D', 'U',
        Rotor.ROTOR_1_1930, Rotor.ROTOR_2_1930, Rotor.ROTOR_3_1930, Rotor.REFLECTOR_B));
    machine3 = EnigmaMachine.getEnigmaMachine(new EnigmaMachineConfig('V', 'P', 'C',
        Rotor.ROTOR_3_1930, Rotor.ROTOR_1_1930, Rotor.ROTOR_2_1930, Rotor.REFLECTOR_B));
  }

  @Test
  public void testGetOutputIndex() {
    Assert.assertEquals(2, machine.getOutputIndex(Rotor.ROTOR_3_1930, 1, 'A' - 'A', false));
    Assert.assertEquals(3, machine.getOutputIndex(Rotor.ROTOR_2_1930, 0, 2, false));
    Assert.assertEquals(5, machine.getOutputIndex(Rotor.ROTOR_1_1930, 0, 3, false));
    Assert.assertEquals(18, machine.getOutputIndex(Rotor.REFLECTOR_B, 0, 5, false));
    Assert.assertEquals(18, machine.getOutputIndex(Rotor.ROTOR_1_1930, 0, 18, true));
    Assert.assertEquals(4, machine.getOutputIndex(Rotor.ROTOR_2_1930, 0, 18, true));
    Assert.assertEquals(1, machine.getOutputIndex(Rotor.ROTOR_3_1930, 1, 4, true));
  }

  @Test
  public void testMoveRotors() {
    assertArrayEquals(new int[] {0, 0, 1}, machine.moveRotorsWithResult());
    assertArrayEquals(new int[] {0, 0, 2}, machine.moveRotorsWithResult());
    assertArrayEquals(new int[] {0, 0, 3}, machine.moveRotorsWithResult());

    assertArrayEquals(new int[] {21, 15, 3}, machine3.moveRotorsWithResult());
    assertArrayEquals(new int[] {21, 15, 4}, machine3.moveRotorsWithResult());
    assertArrayEquals(new int[] {21, 16, 5}, machine3.moveRotorsWithResult());
    assertArrayEquals(new int[] {22, 17, 6}, machine3.moveRotorsWithResult());
    assertArrayEquals(new int[] {22, 17, 7}, machine3.moveRotorsWithResult());
  }

  private void assertArrayEquals(int[] expected, int[] actual) {
    String message = String.format("Expected %s, got %s", Arrays.toString(expected),
        Arrays.toString(actual));
    Assert.assertTrue(message, Arrays.equals(expected, actual));
  }

  @Test
  public void testStep() {
    Assert.assertEquals('B', machine.step('A'));
    Assert.assertEquals('H', machine.step('P'));
    Assert.assertEquals('S', machine.step('P'));
    Assert.assertEquals('D', machine.step('L'));
    Assert.assertEquals('R', machine.step('E'));
  }

  @Test
  public void testStepWithTurnover() {
    Assert.assertEquals('W', machine2.step('O'));
    Assert.assertEquals('V', machine2.step('R'));
    Assert.assertEquals('I', machine2.step('A'));
    Assert.assertEquals('D', machine2.step('N'));
    Assert.assertEquals('E', machine2.step('G'));
    Assert.assertEquals('O', machine2.step('E'));
  }

  @Test
  public void testStepWithMachine3() {
    Assert.assertEquals('K', machine3.step('F'));
    Assert.assertEquals('V', machine3.step('O'));
    Assert.assertEquals('Z', machine3.step('O'));
    Assert.assertEquals('J', machine3.step('T'));
    Assert.assertEquals('I', machine3.step('B'));
    Assert.assertEquals('T', machine3.step('A'));
    Assert.assertEquals('Y', machine3.step('L'));
    Assert.assertEquals('W', machine3.step('L'));
  }
}
