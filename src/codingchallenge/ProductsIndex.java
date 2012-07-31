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
 * An index over products.  The products are indexed by model and family.
 * In addition, a list of products with no family is kept.
 * 
 * @author Shannon
 *
 */
public class ProductsIndex implements TextIndex {
    private TextIndex   	modelIndex;
    private TextIndex   	familyIndex;
    private Set<Integer>	noFamilyProducts;

    /**
     * Products index
     * @param modelIndex index over product models
     * @param familyIndex index over products families
     * @param noFamilyProducts those products that have no specified family
     */
    public ProductsIndex(TextIndex modelIndex, TextIndex familyIndex, Set<Integer> noFamilyProducts) {
        this.modelIndex = modelIndex;
        this.familyIndex = familyIndex;
        this.noFamilyProducts = noFamilyProducts;
    }

    /**
     * The set of products whose model and family match the given query are returned.
     * Those products who have no family, but have a matching model, are also returned.
     */
    public Set<Integer> bestMatches(String query) {
        Set<Integer> modelMatches = modelIndex.bestMatches(query);
        Set<Integer> familyMatches = familyIndex.bestMatches(query);
        
        Set<Integer> intersection = new HashSet<Integer>();
        for (Integer match: modelMatches) {
            if (familyMatches.contains(match) || noFamilyProducts.contains(match)) {
                intersection.add(match);
            }
        }
        return intersection;
    } 
}
