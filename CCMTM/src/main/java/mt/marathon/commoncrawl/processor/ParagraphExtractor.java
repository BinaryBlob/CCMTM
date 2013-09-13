/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mt.marathon.commoncrawl.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import mt.marathon.commoncrawl.Entry;

/**
 * TODO
 * @author p.przybysz
 */
public class ParagraphExtractor implements EntryProcessor {

    private static final String PARA_PATTERN = ".*([\\p{L}]{2,}\\s+){10,}.*";//|.*([\\p{L}]{2,}\\s+){3,}.*\\.

    public List<Entry> process(Entry entry) {
        Scanner valueScanner = new Scanner(entry.getValue());
        StringBuilder builder = new StringBuilder();
        while (valueScanner.hasNextLine()) {
            String line = valueScanner.nextLine();
            if (line.matches(PARA_PATTERN)) {
                builder.append(line).append(" ");
            } else if (line.matches("\\p{Z}+")) {
                //skip
            } else {
                builder.append(line).append("\n");
            }

        }
        entry.setValue(builder.toString());
        return null;
    }

    /**
     *
     * @return
     */
    public boolean isMapper() {
        return false;
    }

    public void close() throws Exception {
    }
}
