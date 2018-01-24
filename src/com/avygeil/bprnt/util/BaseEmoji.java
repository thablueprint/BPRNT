package com.avygeil.bprnt.util;

import java.util.Base64;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

public class BaseEmoji {
	
	private static BidiMap<Character, String> emojiMap = new DualHashBidiMap<>();
	
	static {		
		emojiMap.put('B', "\uD83C\uDF3F");
		emojiMap.put('C', "\uD83C\uDF45");
		emojiMap.put('D', "\uD83C\uDF52");
		emojiMap.put('E', "\uD83C\uDF5F");
		emojiMap.put('F', "\uD83C\uDF65");
		emojiMap.put('G', "\uD83C\uDF00");
		emojiMap.put('H', "\uD83C\uDF68");
		emojiMap.put('I', "\uD83C\uDF6A");
		emojiMap.put('J', "\uD83C\uDF6B");
		emojiMap.put('K', "\uD83C\uDF6F");
		emojiMap.put('L', "\uD83C\uDF70");
		emojiMap.put('M', "\uD83C\uDF73");
		emojiMap.put('N', "\uD83C\uDF77");
		emojiMap.put('O', "\uD83C\uDF7B");
		emojiMap.put('P', "\uD83C\uDF85");
		emojiMap.put('Q', "\uD83C\uDF63");
		emojiMap.put('R', "\uD83C\uDF44");
		emojiMap.put('S', "\uD83C\uDF1F");
		emojiMap.put('T', "\uD83C\uDF08");
		emojiMap.put('U', "\uD83C\uDFE1");
		emojiMap.put('V', "\uD83D\uDE48");
		emojiMap.put('W', "\uD83D\uDE49");
		emojiMap.put('X', "\uD83D\uDE4A");
		emojiMap.put('Y', "\uD83D\uDC4C");
		emojiMap.put('Z', "\uD83D\uDCA9");
		emojiMap.put('a', "\uD83D\uDDFF");
		emojiMap.put('b', "\uD83C\uDF0B");
		emojiMap.put('c', "\uD83C\uDF0C");
		emojiMap.put('d', "\uD83D\uDEBC");
		emojiMap.put('e', "\uD83D\uDEAB");
		emojiMap.put('f', "\uD83C\uDF41");
		emojiMap.put('g', "\uD83D\uDEAA");
		emojiMap.put('h', "\uD83C\uDF88");
		emojiMap.put('i', "\uD83C\uDF54");
		emojiMap.put('j', "\uD83C\uDFA1");
		emojiMap.put('k', "\uD83C\uDFA9");
		emojiMap.put('l', "\uD83C\uDFB0");
		emojiMap.put('m', "\uD83C\uDFC1");
		emojiMap.put('n', "\uD83C\uDFC6");
		emojiMap.put('o', "\uD83C\uDFE5");
		emojiMap.put('p', "\uD83D\uDC0C");
		emojiMap.put('q', "\uD83D\uDC17");
		emojiMap.put('r', "\uD83D\uDC19");
		emojiMap.put('s', "\uD83D\uDC1D");
		emojiMap.put('t', "\uD83D\uDC21");
		emojiMap.put('u', "\uD83D\uDC23");
		emojiMap.put('v', "\uD83D\uDC27");
		emojiMap.put('w', "\uD83D\uDC2C");
		emojiMap.put('x', "\uD83D\uDC32");
		emojiMap.put('y', "\uD83D\uDC38");
		emojiMap.put('z', "\uD83D\uDC3C");
		emojiMap.put('0', "\uD83D\uDC40");
		emojiMap.put('1', "\uD83D\uDC44");
		emojiMap.put('2', "\uD83D\uDC4D");
		emojiMap.put('3', "\uD83D\uDC4E");
		emojiMap.put('4', "\uD83D\uDC59");
		emojiMap.put('5', "\uD83D\uDC75");
		emojiMap.put('6', "\uD83C\uDF4C");
		emojiMap.put('7', "\uD83D\uDC4F");
		emojiMap.put('8', "\uD83D\uDC77");
		emojiMap.put('9', "\uD83D\uDC13");
		emojiMap.put('+', "\uD83D\uDC0A");
		emojiMap.put('/', "\uD83D\uDD1E");
	}
	
	public static String encode(String rawInput) {
		String b64 = Base64.getEncoder().encodeToString(rawInput.getBytes());
		StringBuilder result = new StringBuilder();
		
		for (char character : b64.toCharArray()) {
			result.append(emojiMap.get(character));
			result.append(" ");
		}
		
		return result.toString();
	}
	
	public static String decode(String encodedInput) {
		return "";
	}

}
