package codingchallenge;

/*
Copyright (c) 2012 Shannon White

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

import java.text.Normalizer;
import java.util.Arrays;

/**
 * Normalizes and tokenizes a product description string.  Tokens are strictly alpha or numeric.  That is WX-30 tokenizes as "wx" and "30"
 * <ul>
 * <li>Removes accents</li>
 * <li>Convers to lower case</li>
 * <li>Chops string at certain words</li>
 * <li>Does a few substitutions</li>
 * <li>Removes bracketed</li>
 * <li>Alphanumeric sequences changed to alpha and numeric sequence delimited by blanks. Ex WX30 changed to WX 30</li>
 * <li>Splits on non-alphanumeric characters
 * </li>
 * @author Shannon
 *
 */
public class NormalizingAlphaNumericTokenizer implements Tokenizer {

	@Override
	public Iterable<String> tokenize(String text) {
		// Removes accents
		String normalized = Normalizer.normalize(text.toLowerCase(), Normalizer.Form.NFD);
		normalized = normalized.replaceAll("[^\\p{ASCII}]", "");
		
		// Chops string trailing for, pour and - as these mark the end of the key part of the text
		normalized = normalized.replaceAll("( for | pour | \\- ).+$", "");
		
		// Removes bracketed text
		normalized = normalized.replaceAll("\\([^)]*\\)", "");
		
		// Ensures digits separated from alpha
		normalized = normalized.replaceAll("([\\p{Digit}\\.]+)", " $1 ").trim();
		
		// Common substitution
		if (normalized.contains("digital slr")) {
			normalized += " dslr";
		}
		// Splits on non-alphanumeric
		return Arrays.asList(normalized.split("[^\\p{Alnum}\\.]+"));
	}
}
