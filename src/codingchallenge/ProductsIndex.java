package codingchallenge;

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
        if (noFamilyProducts == null) {
            this.noFamilyProducts = Collections.emptySet();
        } else {        	
            this.noFamilyProducts = noFamilyProducts;
        }
    }

    /**
     * The set of products whose model and family match the given query are returned.
     * Those products who have no family, but have a matching model, are also returned.
     */
    public Set<Integer> bestMatches(String query) {
        Set<Integer> modelMatches = Collections.emptySet();
        if (modelIndex != null) {
        	modelMatches = modelIndex.bestMatches(query);
        }
        Set<Integer> familyMatches = Collections.emptySet();
        if (familyIndex != null) {
        	familyMatches = familyIndex.bestMatches(query);
        }
        
        Set<Integer> intersection = new HashSet<Integer>();
        for (Integer match: modelMatches) {
            if (familyMatches.contains(match) || noFamilyProducts.contains(match)) {
                intersection.add(match);
            }
        }
        return intersection;
    } 
}
