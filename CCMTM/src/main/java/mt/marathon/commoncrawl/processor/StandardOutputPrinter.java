/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mt.marathon.commoncrawl.processor;

import java.util.List;
import mt.marathon.commoncrawl.Entry;

/**
 *
 * @author p.przybysz
 */
public class StandardOutputPrinter implements EntryProcessor {

    private static final String MAGIC_NUMBER = "df6fa1abb58549287111ba8d776733e9";
    private final boolean printUrls;

    public StandardOutputPrinter(boolean printUrls) {
        this.printUrls = printUrls;
    }
    
    @Override
    public List<Entry> process(Entry entry) {
        if(printUrls) {
            System.out.printf("%s\t%s\t%f\t%s\n%s\n", MAGIC_NUMBER, entry.getLangId(), entry.getLangProb(), entry.getKey(), entry.toString());            
        } else {
            System.out.printf("%s\n", entry.getValue());
        }
        
        return null;
    }

    public boolean isMapper() {
        return false;
    }

    public void close() throws Exception {
    }
}
