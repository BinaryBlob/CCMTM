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
public interface EntryProcessor {
    
    List<Entry> process(Entry entry) throws Exception;
    
    void close() throws Exception;
    
    boolean isMapper();
}
