package org.theelements.enigma;

import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class Rotor {
  // LETTERS must come first or else the static Rotors will fail to initialize.
  private static final char[] LETTERS = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K',
    'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

  public static final Rotor ROTOR_1_1930 =
      new Rotor("Rotor 1, 1930", "EKMFLGDQVZNTOWYHXUSPAIBRCJ", 'Q');

  public static final Rotor ROTOR_2_1930 =
      new Rotor("Rotor 2, 1930", "AJDKSIRUXBLHWTMCQGZNPYFVOE", 'E');

  public static final Rotor ROTOR_3_1930 =
      new Rotor("Rotor 3, 1930", "BDFHJLCPRTXVZNYEIWGAKMUSQO", 'V');

  public static final Rotor ROTOR_4_1938 =
      new Rotor("Rotor 4, 1938", "ESOVPZJAYQUIRHXLNFTGKDCMWB", 'J');

  public static final Rotor ROTOR_5_1938 =
      new Rotor("Rotor 5, 1938", "VZBRGITYUPSDNHLXAWMJQOFECK", 'Z');

  // TODO(mww): Rotor 6 has double steps, implement that.
  public static final Rotor ROTOR_6 =
      new Rotor("Rotor 6", "JPGVOUMFYQBENHZRDKASXLICTW", 'Z');

  public static final Rotor REFLECTOR_A =
      new Rotor("Reflector A", "EJMZALYXVBWFCRQUONTSPIKHGD", 'Z');

  public static final Rotor REFLECTOR_B =
      new Rotor("Reflector B", "YRUHQSLDPXNGOKMIEBFZCWVJAT", 'Z');

  public static final Rotor REFLECTOR_C =
      new Rotor("Reflector C", "FVPJIAOYEDRZXWGCTKUQSBNMHL", 'Z');

  private static final Map<String, Rotor> registery = Maps.newHashMap();
  static {
    registery.put("1", ROTOR_1_1930);
    registery.put("2", ROTOR_2_1930);
    registery.put("3", ROTOR_3_1930);
    registery.put("4", ROTOR_4_1938);
    registery.put("5", ROTOR_5_1938);
    registery.put("6", ROTOR_6);

    registery.put("A", REFLECTOR_A);
    registery.put("B", REFLECTOR_B);
    registery.put("C", REFLECTOR_C);
  }

  private final String description;
  private final ImmutableBiMap<Character, Character> map;
  private final ImmutableMap<Character, Character> reverseMap;
  private final int turnover;

  private Rotor(String description, String mapping, char turnoverLetter) {
    this.description = description;
    map = buildMap(mapping);
    reverseMap = map.inverse();

    turnover = (turnoverLetter - 'A') + 1;
  }

  public static Rotor getRotorByName(String name) {
    return registery.get(name);
  }

  public char get(char input, boolean reverse) {
    if (reverse) {
      return reverseMap.get(input);
    } else {
      return map.get(input);
    }
  }

  public boolean turnover(int position) {
    return position == turnover;
  }

  @Override
  public String toString() {
    return description;
  }

  private static ImmutableBiMap<Character, Character> buildMap(String mapping) {
    Preconditions.checkArgument(mapping.length() == 26);

    ImmutableBiMap.Builder<Character, Character> builder = ImmutableBiMap.builder();
    for (int i = 0; i < mapping.length(); i++) {
      builder.put(LETTERS[i], mapping.charAt(i));
    }

    return builder.build();
  }
}
