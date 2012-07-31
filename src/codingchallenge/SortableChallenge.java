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

import java.io.*;
import java.util.*;
import org.json.*;

/**
 * Application for the Sortable coding challenge.  Works as follows:
 * 
 * <ol>
 * <li>Reads the products and indexs them by product family and model</li>
 * <li>Reads the listings, and for each listing find the best matching product(s)</li>
 * <li>Inverts the listing to products map to a product to listings map</li>
 * <li>Outputs the product to listings map to standard output</li>
 * </ol>
 * <p>
 * Conversion to/from JSON is done via open source from org.json 
 * 
 * @author Shannon
 */
public class SortableChallenge {
	private Reader						productsReader;
	private Reader						listingsReader;
	private Writer						outputWriter;
	private Map<Product,Set<Listing>>	productToListings = new HashMap<Product,Set<Listing>>();
	private Map<Listing,JSONObject>		listingToJSON = new HashMap<Listing,JSONObject>();
	private ProductMatcher	matcher;
	
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            usage();
        }
        String productsFileName = args[0];
        String listingsFileName = args[1];
        
        Reader productsReader = new FileReader(productsFileName);
        Reader listingsReader = new FileReader(listingsFileName);
        
        new SortableChallenge(productsReader, listingsReader).run();
    }
    
    SortableChallenge(Reader productsReader, Reader listingsReader) {
    	this.productsReader = productsReader;
    	this.listingsReader = listingsReader;
    	matcher = new ProductMatcher(new NormalizingAlphaNumericTokenizer());
    }
    
    private void run() throws IOException, JSONException {
        initProductsMatcher();
        
        processListings();
		    	
    	outputProductMatches();
    }
    
    private void processListings() throws IOException, JSONException {
    	try {
    		LineNumberReader llistingsReader = new LineNumberReader(listingsReader);
    		for (String line = llistingsReader.readLine(); line != null; line = llistingsReader.readLine()) {
    			line = line.trim();
    			if (line.length() == 0) {
    				continue;
    			}
            	JSONTokener tokener = new JSONTokener(line);
        		Object token = tokener.nextValue();
        		if (!(token instanceof JSONObject)) {
        			throw new BadInputException("Bad listing data: " + token);
        		}
        		JSONObject listingJSON = (JSONObject) token;
        		String title = getStringProp("title", listingJSON);
        		String manufacturer = getStringProp("manufacturer", listingJSON);
        		String currency = getStringProp("currency", listingJSON);
        		String price = getStringProp("price", listingJSON);
        		Listing listing = new Listing(title, manufacturer, currency, price);
        		listingToJSON.put(listing, listingJSON);
        		Set<Product> matchingProducts = matcher.getMatches(listing);
        		addMatch(listing, matchingProducts);
        	}
    	} finally {
        	listingsReader.close();
    	}    	
    }
    
    private void initProductsMatcher() throws IOException, JSONException {
		List<Product> products = new ArrayList<Product>();
		LineNumberReader lproductsReader = new LineNumberReader(productsReader);
    	try {
    		for (String line = lproductsReader.readLine(); line != null; line = lproductsReader.readLine()) {
    			line = line.trim();
    			if (line.length() == 0) {
    				continue;
    			}
            	JSONTokener tokener = new JSONTokener(line);
        		Object token = tokener.nextValue();
        		if (!(token instanceof JSONObject)) {
        			throw new BadInputException("Bad product data: " + token);
        		}
        		JSONObject productJSON = (JSONObject) token;
        		String name = getStringProp("product_name", productJSON);
        		String manufacturer = getStringProp("manufacturer", productJSON);
        		String family = getStringProp("family", productJSON);
        		String model = getStringProp("model", productJSON);
        		String announcedDate = getStringProp("announced-date", productJSON);
        		products.add(new Product(name, manufacturer, family, model, announcedDate));
        	}
    	} finally {
    		lproductsReader.close();
    	}
		matcher.initProducts(products);
    	
    }
    
    private void outputProductMatches() throws IOException, JSONException {
    	outputWriter = new OutputStreamWriter(System.out);
    	try {
        	for (Map.Entry<Product,Set<Listing>> entry: productToListings.entrySet()) {
        		Product product = entry.getKey();
            	JSONWriter writer = new JSONWriter(outputWriter);
        		writer.object();
        		writer.key("product_name").value(product.getName());
    			JSONArray listingsArray = new JSONArray();
        		for (Listing listing: entry.getValue()) {
        			listingsArray.put(listingToJSON.get(listing));
        		}
        		
    			writer.key("listings").value(listingsArray);
        		writer.endObject();
        		outputWriter.write("\n");
        	}
    	} finally {
        	outputWriter.close();
    	}
    }
    
    private void addMatch(Listing listing, Set<Product> products) {
    	for (Product product: products) {
    		Set<Listing> listings = productToListings.get(product);
    		if (listings == null) {
    			listings = new HashSet<Listing>();
    			productToListings.put(product, listings);
    		}
    		listings.add(listing);
    	}
    }

    private static void usage() {
        System.out.println("java codingchallenge.SortableChallenge <products> <listings>");
        System.exit(0);
    }
    
    private static String getStringProp(String key, JSONObject obj) throws JSONException {
    	return obj.has(key) ? obj.getString(key) : null;
    }
    
	private static class BadInputException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		public BadInputException(String s) {
			super (s);
		}
	}	
}
