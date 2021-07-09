package com.attestorforensics.mobifumecore.model.connection.message;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessagePattern {

  private final Pattern topicPattern;
  private final Map<Integer, Pattern> argumentPatterns;

  private MessagePattern(Pattern topicPattern, Map<Integer, Pattern> argumentPatterns) {
    this.topicPattern = topicPattern;
    this.argumentPatterns = argumentPatterns;
  }

  public static MessagePattern create(Pattern topicPattern,
      Map<Integer, Pattern> argumentPatterns) {
    return new MessagePattern(topicPattern, ImmutableMap.copyOf(argumentPatterns));
  }

  public static MessagePattern createSingleArgumentPattern(String topicPattern,
      String firstArgumentPattern) {
    return new MessagePattern(Pattern.compile(topicPattern),
        ImmutableMap.of(0, Pattern.compile(firstArgumentPattern)));
  }

  public boolean matches(String topic, String[] arguments) {
    Matcher topicMatcher = topicPattern.matcher(topic);
    if (!topicMatcher.matches()) {
      return false;
    }

    for (Entry<Integer, Pattern> argumentPatternEntry : argumentPatterns.entrySet()) {
      int index = argumentPatternEntry.getKey();
      if (index >= arguments.length) {
        return false;
      }

      Pattern argumentPattern = argumentPatternEntry.getValue();
      String argument = arguments[index];
      Matcher argumentMatcher = argumentPattern.matcher(argument);
      if (!argumentMatcher.matches()) {
        return false;
      }
    }

    return true;
  }
}
