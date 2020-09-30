package com.takipi.oss.storage.helper;

import java.util.ArrayList;
import java.util.List;

public class StringUtil {
  public static String padRight(String str, int targetLength) {
    return String.format("%1$-" + targetLength + "s", new Object[] { str });
  }
  
  public static String padLeft(String str, int targetLength) {
    return String.format("%1$" + targetLength + "s", new Object[] { str });
  }
  
  public static String[] csvSplit(String s) {
    if (s == null)
      return null; 
    return csvSplit(s, 0, s.length());
  }
  
  public static String[] csvSplit(String s, int off, int len) {
    if (s == null)
      return null; 
    if (off < 0 || len < 0 || off > s.length())
      throw new IllegalArgumentException(); 
    List<String> list = new ArrayList<>();
    csvSplit(list, s, off, len);
    return list.<String>toArray(new String[list.size()]);
  }
  
  enum CsvSplitState {
    PRE_DATA, QUOTE, SLOSH, DATA, WHITE, POST_DATA;
  }
  
  public static List<String> csvSplit(List<String> list, String s, int off, int len) {
    if (list == null)
      list = new ArrayList<>(); 
    CsvSplitState state = CsvSplitState.PRE_DATA;
    StringBuilder out = new StringBuilder();
    int last = -1;
    while (len > 0) {
      char ch = s.charAt(off++);
      len--;
      switch (state) {
        case PRE_DATA:
          if (Character.isWhitespace(ch))
            continue; 
          if ('"' == ch) {
            state = CsvSplitState.QUOTE;
            continue;
          } 
          if (',' == ch) {
            list.add("");
            continue;
          } 
          state = CsvSplitState.DATA;
          out.append(ch);
        case DATA:
          if (Character.isWhitespace(ch)) {
            last = out.length();
            out.append(ch);
            state = CsvSplitState.WHITE;
            continue;
          } 
          if (',' == ch) {
            list.add(out.toString());
            out.setLength(0);
            state = CsvSplitState.PRE_DATA;
            continue;
          } 
          out.append(ch);
        case WHITE:
          if (Character.isWhitespace(ch)) {
            out.append(ch);
            continue;
          } 
          if (',' == ch) {
            out.setLength(last);
            list.add(out.toString());
            out.setLength(0);
            state = CsvSplitState.PRE_DATA;
            continue;
          } 
          state = CsvSplitState.DATA;
          out.append(ch);
          last = -1;
        case QUOTE:
          if ('\\' == ch) {
            state = CsvSplitState.SLOSH;
            continue;
          } 
          if ('"' == ch) {
            list.add(out.toString());
            out.setLength(0);
            state = CsvSplitState.POST_DATA;
            continue;
          } 
          out.append(ch);
        case SLOSH:
          out.append(ch);
          state = CsvSplitState.QUOTE;
        case POST_DATA:
          if (',' == ch)
            state = CsvSplitState.PRE_DATA; 
      } 
    } 
    switch (state) {
      case DATA:
      case QUOTE:
      case SLOSH:
        list.add(out.toString());
        break;
      case WHITE:
        out.setLength(last);
        list.add(out.toString());
        break;
    } 
    return list;
  }
  
  public static boolean isNullOrEmpty(String str) {
    if (str == null || str.trim().isEmpty())
      return true; 
    return false;
  }
}

