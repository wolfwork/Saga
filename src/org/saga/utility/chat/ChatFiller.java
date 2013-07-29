package org.saga.utility.chat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.saga.messages.colours.Colour.CustomColour;

public class ChatFiller {

	
	/**
	 * Default character length.
	 */
	public final static Double DEFAULT_LENGTH = 3.0 / 2.0;
	
	/**
	 * Maximum character length.
	 */
	public final static Double MAX_LENGTH = 3.0 / 2.0;

	/**
	 * Gap fill string maximum size.
	 */
	private final static Double MAX_GAP = 1.25;
	
	/**
	 * Chat width.
	 */
	public final static Double CHAT_WIDTH = 80.0;
	
	
	/**
	 * Size map.
	 */
	private final static HashMap<Character, Double> SIZE_MAP = new HashMap<Character, Double>(){
		
		/**
		 * Serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		{
			put('i', 0.5);
			put('k', 5.0 / 4.0);
			put('t', 1.0);
			put('f', 5.0 / 4.0);
			put('(', 5.0 / 4.0);
			put(')', 5.0 / 4.0);
			put('<', 5.0 / 4.0);
			put('>', 5.0 / 4.0);
			put('{', 5.0 / 4.0);
			put('}', 5.0 / 4.0);
			put(',', 1.0 / 2.0);
			put('.', 1.0 / 2.0);
			put('[', 1.0);
			put(']', 1.0);
			put('I', 1.0);
			put('|', 1.0 / 2.0);
			put('*', 5.0 / 4.0);
			put('"', 5.0 / 4.0);
			put('|', 0.5);
			put('!', 0.5);
			put(':', 0.5);
			put('l', 3.0 / 4.0);
			put('.', 1.0 / 2.0);
			put('\'', 3.0 / 4.0);
			put(' ', 1.0 / 1.0);
			put('\"', 5.0 / 4.0);
			put('`', 0.5);
			put('\0', 0.0);

			put('\u278A', 0.5);
			put('\u278B', 3.0/4.0);
			put(' ', 1.0);
			put('\u278C', 5.0/4.0);
			
			put('\u2500', 5.0/4.0);
			put('\u2502', 1.0/4.0);
			put('\u250C', 3.0/4.0);
			put('\u2510', 3.0/4.0);
			put('\u2514', 3.0/4.0);
			put('\u2518', 3.0/4.0);
			
			put('\u2550', 5.0/4.0);
			put('\u2551', 1.0/2.0);
			
			put('\u2554', 3.0/4.0);
			put('\u2560', 3.0/4.0);
			put('\u255A', 3.0/4.0);
			
			put('\u2557', 4.0/4.0);
			put('\u2563', 4.0/4.0);
			put('\u255D', 4.0/4.0);
			
			put('\u2591', 2.0);
			
			put(CustomColour.PREVIOUS_COLOR.getChar(), 0.0);
			put(CustomColour.NORMAL_FORMAT.getChar(), 0.0);
			
		}
	};
	
	
	/**
	 * Gap fill chars.
	 */
	private final static HashSet<Character> FILL_CHARS = new HashSet<Character>(){
		private static final long serialVersionUID = 1L;
		{
			add('\u278A');
			add('\u278B');
			add(' ');
			add('\u278C');
		}
	};

	
	

	
	
	/**
	 * Fills a string.
	 * 
	 * @param str string to fill
	 * @param reqLength required length
	 * @return string with the given length 
	 */
	public static String fillString(String str, Double reqLength) {

		
		char[] chars = str.toCharArray();
		
		StringBuffer result = new StringBuffer();
		Double length = 0.0;
		
		// Cut size:
		for (int i = 0; i < chars.length; i++) {
			
			Double charLength = SIZE_MAP.get(chars[i]);
			if(charLength == null) charLength = DEFAULT_LENGTH;
			
			if(length + charLength > reqLength) break;
			
			result.append(chars[i]);
			
			if(!(chars[i] == ChatColor.COLOR_CHAR || (i > 0 && chars[i-1] == ChatColor.COLOR_CHAR)))
			length += charLength;
			
		}
		
		// Add spaces:
		Character fillChar = ' ';
		Double fillLength = 1.0;
		while(true){

			Double gapLength = reqLength - length;
			
			// Gap filled:
			if(gapLength <= 0) break;
			
			// Add custom fillers: 
			if(gapLength <= MAX_GAP){

				fillChar = findCustom(gapLength, reqLength);
				if(fillChar != null){
					result.append(fillChar);
					fillLength = SIZE_MAP.get(fillChar);
				}
				
				break;
				
			}
			
			result.append(fillChar);
			length += fillLength;
			
		}
		
		return result.toString();
		
		
	}

	/**
	 * Finds a custom character with the best fit.
	 * 
	 * @param gapLen gap length
	 * @param reqLength required length
	 * @return char that best fits the gap, null if none
	 */
	private static Character findCustom(Double gapLen, Double reqLength) {
		

		Set<Character> gapStrs = new HashSet<Character>(FILL_CHARS);
		Double bestFitLen = -1.0;
		Character bestFitStr = null;
		
		for (Character gapStr : gapStrs) {
			
			Double gapStrLen = SIZE_MAP.get(gapStr);
			
			if(gapLen - gapStrLen >= 0 && gapStrLen > bestFitLen){
				bestFitLen = gapStrLen;
				bestFitStr = gapStr;
			}
			
		}
		
		return bestFitStr;
		
		
	}
	
	
	/**
	 * Calculates the length of a string.
	 * 
	 * @param str string
	 * @return string length
	 */
	public static Double calcLength(String str) {

		
		char[] chars = str.toCharArray();
		
		Double length = 0.0;
		
		for (int i = 0; i < chars.length; i++) {
			
			Double charLength = SIZE_MAP.get(chars[i]);
			if(charLength == null) charLength = DEFAULT_LENGTH;
			
			if(!(chars[i] == ChatColor.COLOR_CHAR || (i > 0 && chars[i-1] == ChatColor.COLOR_CHAR)))
			length += charLength;
			
		}
		
		return length;
		
		
	}

	
	/**
	 * Adjusts filler characters.
	 * 
	 * @param str string
	 * @return adjusted string
	 */
	public static String adjustFillers(String str) {

//		str = str.replace("\u278A", ChatColor.DARK_GRAY + "`");
//		str = str.replace("\u278B", ChatColor.DARK_GRAY + "\'");
//		str = str.replace("\u278C", ChatColor.DARK_GRAY + "\"");
		
		str = str.replace("\u278A", ChatColor.DARK_GRAY + "`");
		str = str.replace("\u278B", ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "`" + CustomColour.NORMAL_FORMAT);
		str = str.replace("\u278C", ChatColor.DARK_GRAY + "" + ChatColor.BOLD + " " + CustomColour.NORMAL_FORMAT);
		
		return str;
		
	}
	
	
}
