package com.cms.util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class StringUtils {
	/*public static boolean isBlank(String s){
		if(s!=null){
			if("".equals(s.trim())){
				return true;
			}
		}
		return false;
	}*/
	public static final String[] EMPTY_STRING_ARRAY = new String[0];
	public static final int INDEX_NOT_FOUND = -1;
	public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }
	public static String replace(String text, String searchString, String replacement) {
        return replace(text, searchString, replacement, -1);
    }
	public static String replace(String text, String searchString, String replacement, int max) {
        if (isEmpty(text) || isEmpty(searchString) || replacement == null || max == 0) {
            return text;
        }
        int start = 0;
        int end = text.indexOf(searchString, start);
        if (end == INDEX_NOT_FOUND) {
            return text;
        }
        int replLength = searchString.length();
        int increase = replacement.length() - replLength;
        increase = increase < 0 ? 0 : increase;
        increase *= max < 0 ? 16 : max > 64 ? 64 : max;
        StringBuilder buf = new StringBuilder(text.length() + increase);
        while (end != INDEX_NOT_FOUND) {
            buf.append(text.substring(start, end)).append(replacement);
            start = end + replLength;
            if (--max == 0) {
                break;
            }
            end = text.indexOf(searchString, start);
        }
        buf.append(text.substring(start));
        return buf.toString();
    }
	public static boolean isBlank(CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (Character.isWhitespace(cs.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }
	public static String[] split(String str, String separatorChars) {
        return splitWorker(str, separatorChars, -1, false);
    }
	public static String[] split(String str, char separatorChar) {
        return splitWorker(str, separatorChar, false);
    }
	private static String[] splitWorker(String str, char separatorChar, boolean preserveAllTokens) {
        // Performance tuned for 2.0 (JDK1.4)

        if (str == null) {
            return null;
        }
        int len = str.length();
        if (len == 0) {
            return EMPTY_STRING_ARRAY;
        }
        List<String> list = new ArrayList<String>();
        int i = 0, start = 0;
        boolean match = false;
        boolean lastMatch = false;
        while (i < len) {
            if (str.charAt(i) == separatorChar) {
                if (match || preserveAllTokens) {
                    list.add(str.substring(start, i));
                    match = false;
                    lastMatch = true;
                }
                start = ++i;
                continue;
            }
            lastMatch = false;
            match = true;
            i++;
        }
        if (match || preserveAllTokens && lastMatch) {
            list.add(str.substring(start, i));
        }
        return list.toArray(new String[list.size()]);
    }
	private static String[] splitWorker(String str, String separatorChars, int max, boolean preserveAllTokens) {
        // Performance tuned for 2.0 (JDK1.4)
        // Direct code is quicker than StringTokenizer.
        // Also, StringTokenizer uses isSpace() not isWhitespace()

        if (str == null) {
            return null;
        }
        int len = str.length();
        if (len == 0) {
            return EMPTY_STRING_ARRAY;
        }
        List<String> list = new ArrayList<String>();
        int sizePlus1 = 1;
        int i = 0, start = 0;
        boolean match = false;
        boolean lastMatch = false;
        if (separatorChars == null) {
            // Null separator means use whitespace
            while (i < len) {
                if (Character.isWhitespace(str.charAt(i))) {
                    if (match || preserveAllTokens) {
                        lastMatch = true;
                        if (sizePlus1++ == max) {
                            i = len;
                            lastMatch = false;
                        }
                        list.add(str.substring(start, i));
                        match = false;
                    }
                    start = ++i;
                    continue;
                }
                lastMatch = false;
                match = true;
                i++;
            }
        } else if (separatorChars.length() == 1) {
            // Optimise 1 character case
            char sep = separatorChars.charAt(0);
            while (i < len) {
                if (str.charAt(i) == sep) {
                    if (match || preserveAllTokens) {
                        lastMatch = true;
                        if (sizePlus1++ == max) {
                            i = len;
                            lastMatch = false;
                        }
                        list.add(str.substring(start, i));
                        match = false;
                    }
                    start = ++i;
                    continue;
                }
                lastMatch = false;
                match = true;
                i++;
            }
        } else {
            // standard case
            while (i < len) {
                if (separatorChars.indexOf(str.charAt(i)) >= 0) {
                    if (match || preserveAllTokens) {
                        lastMatch = true;
                        if (sizePlus1++ == max) {
                            i = len;
                            lastMatch = false;
                        }
                        list.add(str.substring(start, i));
                        match = false;
                    }
                    start = ++i;
                    continue;
                }
                lastMatch = false;
                match = true;
                i++;
            }
        }
        if (match || preserveAllTokens && lastMatch) {
            list.add(str.substring(start, i));
        }
        return list.toArray(new String[list.size()]);
    }
	public static String null2Blank(Integer s){
		if(s!=null){
			return String.valueOf(s);
		}
		return "";
	}
	 public static void main(String[] args) throws UnsupportedEncodingException {
		 if(StringUtils.isBlank("bob")){
			 System.out.println("true");
		 }
		 else
			 System.out.println("false");
	 }
	 
	    // Count matches
	    public static int countMatches(CharSequence str, CharSequence sub) {
	        if (isEmpty(str) || isEmpty(sub)) {
	            return 0;
	        }
	        int count = 0;
	        int idx = 0;
	        while ((idx = CharSequenceUtils.indexOf(str, sub, idx)) != INDEX_NOT_FOUND) {
	            count++;
	            idx += sub.length();
	        }
	        return count;
	    }
	    
	    //获取分页内容
		public static String getTxtByNo(String txt, int pageNo) {		
			if (StringUtils.isBlank(txt) || pageNo < 1) {
				return null;
			}
			String PAGE_START = "<p>[NextPage]";
			String PAGE_END = "[/NextPage]</p>";
			int start = 0, end = 0;
			for (int i = 0; i < pageNo; i++) {

				if (i != 0) {
					start = txt.indexOf(PAGE_END, end);
					if (start == -1) {
						return null;
					} else {
						start += PAGE_END.length();
					}
				}
				end = txt.indexOf(PAGE_START, start);
				if (end == -1) {
					end = txt.length();
				}
			}
			return txt.substring(start, end);
		}

}
