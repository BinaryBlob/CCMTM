/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mt.marathon.commoncrawl.processor;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import mt.marathon.commoncrawl.Entry;

/**
 *
 * @author p.przybysz
 */
public class PageLangWriter implements EntryProcessor {

    private final Map<String, ZipOutputStream> zips = new HashMap<String, ZipOutputStream>();
    private final String outputDirectory;
    private final String filePrefix;
    private long index;
    private boolean printKeyInTheFirstLine = false;

    public PageLangWriter(String outputDirectory, String filePrefix, boolean printUrls) {
        File directory = new File(outputDirectory);
        directory.mkdirs();
        this.filePrefix = filePrefix;
        this.outputDirectory = outputDirectory;
        this.printKeyInTheFirstLine = printUrls;
    }

    public List<Entry> process(Entry entry) throws IOException {
        String lang = entry.getLangId();

        String dirName = outputDirectory + "/" + lang ;
        String zipDir = outputDirectory + "/" + lang + "/" + filePrefix + ".gz";
        String fileName = ("000000" + index).substring(("000000" + ++index).length() - 6);
        ZipOutputStream zip;
        if (zips.get(lang) == null) {
            File dir = new File(dirName);
            dir.mkdirs();
            zip = new ZipOutputStream(new FileOutputStream(zipDir));
            zips.put(lang, zip);
        } else {
            zip = zips.get(lang);
        }
        StringBuilder writer = new StringBuilder();
        if (printKeyInTheFirstLine) {
            writer.append(entry.getKey())
                    .append("\n")
                    .append(entry.getValue())
                    .append("\n");
        } else {
            writer.append(entry.getValue());
        }

        ByteArrayInputStream in = new ByteArrayInputStream(writer.toString().getBytes()); // Stream to read file

        ZipEntry zipEntry = new ZipEntry(fileName); // Make a ZipEntry
        zip.putNextEntry(zipEntry); // Store entry
        byte[] buffer = new byte[4096]; // Create a buffer for copying
        int bytes_read;
        while ((bytes_read = in.read(buffer)) != -1) // Copy bytes
        {
            zip.write(buffer, 0, bytes_read);
        }
        in.close(); // Close input stream
        return null;
    }

    public boolean isMapper() {
        return false;
    }

    public void close() throws Exception {
        for (String lang : zips.keySet()) {
            ZipOutputStream zip = zips.get(lang);
            zip.close();
        }
    }

    /**
     * Zip the contents of the directory, and save it in the zipfile
     */
    public void zipDirectory(File d, String zipfile) throws IOException,
            IllegalArgumentException {
        // Check that the directory is a directory, and get its contents
        if (!d.isDirectory()) {
            throw new IllegalArgumentException("Compress: not a directory:  " + d.getPath());
        }
        String[] entries = d.list();
        byte[] buffer = new byte[4096]; // Create a buffer for copying
        int bytes_read;

        // Create a stream to compress data and write it to the zipfile
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipfile));

        // Loop through all entries in the directory
        for (int i = 0; i < entries.length; i++) {
            File f = new File(d, entries[i]);
            if (f.isDirectory()) {
                continue; // Don't zip sub-directories
            }
            FileInputStream in = new FileInputStream(f); // Stream to read file
            ZipEntry entry = new ZipEntry(f.getPath()); // Make a ZipEntry
            out.putNextEntry(entry); // Store entry
            while ((bytes_read = in.read(buffer)) != -1) // Copy bytes
            {
                out.write(buffer, 0, bytes_read);
            }
            in.close(); // Close input stream
        }
        // When we're done with the whole loop, close the output stream
        out.close();
    }
}
