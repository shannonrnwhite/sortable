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
 * Matches products to listings.  Indexes are kept on a per manufacturer basis.
 * 
 * @author Shannon
 *
 */
public class ProductMatcher {
    private List<Product>           productList			= new ArrayList<Product>();
    private Map<String,TextIndex>   manufacturerToIndex	= new HashMap<String,TextIndex>();
    private Tokenizer				tokenizer;

    /**
     * Product matcher
     * 
     * @param tokenizer for tokenizing product names and listing titles
     */
    public ProductMatcher(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }
    
    /**
     * Finds the product(s) associated with a particular product listing
     * @param listing the listing to find
     * @return the products matching the listing
     */
    public Set<Product> getMatches(Listing listing) {
        Set<Product> matchingProducts = new HashSet<Product>();
        TextIndex index =  indexForListing(listing);
        if (index == null) {
            return Collections.emptySet();
        }
        String title = listing.getTitle();
        for (Integer productId: index.bestMatches(title)) {
            Product product = productList.get(productId);
            if (product == null) {
                continue;
            }
            matchingProducts.add(product);
        }
        return matchingProducts;
    }

    /**
     * Creates search indexes over the given products 
     * @param products the products over which to create indices
     */
    public void initProducts(Iterable<Product> products) {
        Map<String,SimpleTextIndex> manufacturerToFamilyIndex = new HashMap<String,SimpleTextIndex>();
        Map<String,SimpleTextIndex> manufacturerToModelIndex = new HashMap<String,SimpleTextIndex>();
        Map<String,Set<Integer>> manufacturerToNoFamily = new HashMap<String,Set<Integer>>();
        for (Product product: products) {
            int prodId = productList.size();
            productList.add(product);

            String manu = product.getManufacturer().toLowerCase();

            SimpleTextIndex index = manufacturerToFamilyIndex.get(manu);
            if (index == null) {
                index = createSimpleIndex();
                manufacturerToFamilyIndex.put(manu, index);
            }
            String family = product.getFamily();
            if (family != null) {
            	index.index(prodId, product.getFamily());
            } else {
            	Set<Integer> noFamilySet = manufacturerToNoFamily.get(manu);
            	if (noFamilySet == null) {
            		noFamilySet = new HashSet<Integer>();
            		manufacturerToNoFamily.put(manu, noFamilySet);
            	}
            	noFamilySet.add(prodId);
            }

            index = manufacturerToModelIndex.get(manu);
            if (index == null) {
                index = createSimpleIndex();
                manufacturerToModelIndex.put(manu, index);
            }
            String model = product.getModel();
            if (model != null) {
            	index.index(prodId, product.getModel());
            }
        }

        for (String manu: manufacturerToFamilyIndex.keySet()) {
            TextIndex familyIndex = manufacturerToFamilyIndex.get(manu);
            TextIndex modelIndex = manufacturerToModelIndex.get(manu);
            Set<Integer> noFamilySet = manufacturerToNoFamily.get(manu);
            ProductsIndex productIndex = new ProductsIndex(modelIndex, familyIndex, noFamilySet);
            manufacturerToIndex.put(manu, productIndex);
        }
    }
    
    private SimpleTextIndex createSimpleIndex() {
    	return new SimpleTextIndex(tokenizer);
    }
    
    /**
     * Gets the manufacturer-specific index associated with a given listing.
     * The manufacturer is determined as follows:
     * <ol>
     * <li>listing.getManufacturer(), or</li>
     * <li>first word in listing.getManufacturer(), ex. "Canon" from "Canon Canada", or</li>
     * <li>first word in listing.getTitle() ex. "Nikon" from "Nikon SLR..."</li>
     * </ol>
     * @param listing
     * @return
     */
    private TextIndex indexForListing(Listing listing) {
        String manu = listing.getManufacturer();
        if (manu == null) {
        	return null;
        }
        manu = manu.toLowerCase();
        TextIndex index = manufacturerToIndex.get(manu);
        if (index != null) {
        	return index;
        }
        manu = manu.split(" ")[0];
        index = manufacturerToIndex.get(manu);
        if (index != null) {
        	return index;
        }
        String title = listing.getTitle();
        if (title == null) {
        	return null;
        }
    	manu = title.toLowerCase().split(" ")[0];
        return manufacturerToIndex.get(manu);    
    }
}
