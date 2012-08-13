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

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.theelements.collect.SortedFixedSizedList;
import org.theelements.enigma.EnigmaRunner.EnigmaResult;

import com.google.common.collect.Lists;

public class EnigmaRunnerTest {

  private EnigmaRunner runner;
  List<Rotor> rotors;
  List<Rotor> reflectors;

  @Before
  public void setUp() throws Exception {
    runner = new EnigmaRunner();
    rotors = Lists.newArrayList(Rotor.ROTOR_1_1930, Rotor.ROTOR_2_1930, Rotor.ROTOR_3_1930,
        Rotor.ROTOR_4_1938, Rotor.ROTOR_5_1938);
    reflectors = Lists.newArrayList(Rotor.REFLECTOR_A, Rotor.REFLECTOR_B, Rotor.REFLECTOR_C);
  }

  @Test
  public void testEnigmaRunner01() throws Exception {
    char[] encrypted =
        "ZTQBLVXKPBPGAVQBRYDYQEZNKRLMZTMRGBJSQKHDPHHNTNIDLYVFCOKZYYSMJFAHQBTEAVFKOXRPSQX"
        .toCharArray();
    String expected =
        "THISISASLIGHTLYLONGERTESTSOIHAVETOSEEIFICANKEEPWRITINGALONGERSTRINGTOUSEASINPUT";

    SortedFixedSizedList<EnigmaResult> results = runner.run(encrypted, rotors, reflectors, 3, 3);
    Assert.assertEquals(expected, results.iterator().next().getMessage());
  }

}
