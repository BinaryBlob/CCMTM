/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mt.marathon.commoncrawl.processor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;
import mt.marathon.commoncrawl.Entry;

/**
 *
 * @author p.przybysz
 */
public class OneFileForAllPagesPrinter implements EntryProcessor {

    private static final String MAGIC_NUMBER = "df6fa1abb58549287111ba8d776733e9";
    private final Map<String, Writer> writers = new HashMap<String, Writer>();
    private final String outputDirectory;
    private final String filePrefix;
    private boolean printUrls;

    public OneFileForAllPagesPrinter(String outputDirectory, String filePrefix, boolean printUrls) {
        File directory = new File(outputDirectory);
        directory.mkdirs();
        this.filePrefix = filePrefix;
        this.outputDirectory = outputDirectory;
        this.printUrls = printUrls;
    }

    public List<Entry> process(Entry entry) throws IOException {
        String lang = entry.getLangId();
        Writer writer;
        if (writers.get(lang) == null) {
            String fileName = outputDirectory + "/" + filePrefix + "-" + lang + ".gz";
            writer = new BufferedWriter(
                    new OutputStreamWriter(
                    new GZIPOutputStream(
                    new FileOutputStream(fileName))));
            writers.put(lang, writer);
        } else {
            writer = writers.get(lang);
        }
        if(printUrls) {
          writer.append(MAGIC_NUMBER)
                .append("\t")
                .append(entry.getLangId())
                .append("\t")
                .append("" + entry.getLangProb())
                .append("\t")
                .append(entry.getKey())
                .append("\n")
                .append(entry.getValue())
                .append("\n");          
        } else {
             writer.append(entry.getValue()).append("\n");
        }

        return null;
    }

    public boolean isMapper() {
        return false;
    }

    public void close() throws Exception {
        for(Writer writer : writers.values()) {
            writer.close();
        }
    }
}
