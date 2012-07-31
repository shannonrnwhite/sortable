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

import java.util.*;

/**
 * General index over a set of documents
 * 
 * @author Shannon
 *
 */
public class SimpleTextIndex implements TextIndex {
	/**
	 * Used for tokenizing documents and queries
	 */
    private Tokenizer                   tokenizer;
    
    /**
     * List of ids of all indexed documents, indexed by document offset
     */
    private List<Integer>               docIds = new ArrayList<Integer>();
    
    /**
     * List of normalized documents, indexed by document offset
     */
    private List<String>				normalizedDocs = new ArrayList<String>();
    
    /**
     * List of document by number of tokens, indexed by document offset
     */
    private List<Integer>				docSizes = new ArrayList<Integer>();
    
    /**
     * Maps tokens to documents containing those tokens
     */
    private Map<String,List<Integer>>   tokenToDocIndices = new HashMap<String,List<Integer>>();

    /**
     * Text index
     * @param tokenizer used for tokenizing documents and queries
     */
    public SimpleTextIndex(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    /**
     * Adds document to index
     * @param docId the id of the added document
     * @param txt the document text
     */
    public void index(Integer docId, String txt) {
        int docIndex = docIds.size();
        docIds.add(docId);
        StringBuilder buf = new StringBuilder();
        buf.append(' ');
        int cnt = 0;
        for (String tok: tokenizer.tokenize(txt)) {
            List<Integer> docIndices = tokenToDocIndices.get(tok);
            if (docIndices == null) {
                docIndices = new ArrayList<Integer>();
                tokenToDocIndices.put(tok, docIndices);
            }
            docIndices.add(docIndex);
            buf.append(tok);
            buf.append(' ');
            cnt++;
        }
        normalizedDocs.add(buf.toString());
        docSizes.add(cnt);
    }

    /**
     * Walks through the query tokens, intersecting the sets of documents having
     * those tokens.  At the end, the documents whose tokens are totally covered
     * by the query are considered to be the best matches
     */
    public Set<Integer> bestMatches(String query) {
        Iterable<String> tokens = tokenizer.tokenize(query);
        StringBuilder buf = new StringBuilder();
        buf.append(' ');
        Map<Integer,Integer> docToFreq = new HashMap<Integer,Integer>();
        for (String token: tokens) {
        	buf.append(token);
            buf.append(' ');
            List<Integer> docs = tokenToDocIndices.get(token);
            if (docs == null) {
                continue;
            }
            for (Integer doc: docs) {
                Integer freq = docToFreq.get(doc);
                if (freq == null) {
                    docToFreq.put(doc, 1);
                } else {
                    docToFreq.put(doc, freq + 1);
                }
            }
        }
        // Go through list of matching documents and find those totally
        // covered by query
        String normalizedQuery = buf.toString();
        Set<Integer> bestMatchingDocs = new HashSet<Integer>();
        int mostMatches = 0;
        for (Map.Entry<Integer,Integer> entry: docToFreq.entrySet()) {
            Integer freq = entry.getValue();
            int doc = entry.getKey();
            if (freq == null || freq < mostMatches || freq != docSizes.get(doc)) {
                continue;
            }
            String normalizedDoc = normalizedDocs.get(doc);
            if (!normalizedQuery.contains(normalizedDoc)) {
            	continue;
            }
            if (freq > mostMatches) {
            	bestMatchingDocs.clear();
                mostMatches = freq;
            }
            bestMatchingDocs.add(docIds.get(doc));
        }
        return bestMatchingDocs;
    }
}
