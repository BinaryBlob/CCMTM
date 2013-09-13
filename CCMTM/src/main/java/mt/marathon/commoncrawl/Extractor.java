package mt.marathon.commoncrawl;

import mt.marathon.commoncrawl.processor.LangIdentification;
import mt.marathon.commoncrawl.processor.OneFileForAllPagesPrinter;
import mt.marathon.commoncrawl.processor.EntryProcessor;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mt.marathon.commoncrawl.processor.OneFilePerPagePrinter;
import mt.marathon.commoncrawl.processor.StandardOutputPrinter;
import mt.marathon.commoncrawl.processor.SentenceSplitterAndTokenizer;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.util.ReflectionUtils;

/**
 * Hello world!
 *
 */
public class Extractor {

    private List<EntryProcessor> processors;

    public Extractor(List<EntryProcessor> processors) {
        this.processors = processors;
    }

    public void processEntry(String key, String text) throws Exception {
        List<Entry> entries = Collections.singletonList(new Entry(key, text));
        for (EntryProcessor valueProcessor : processors) {
            if (valueProcessor.isMapper()) {
                List<Entry> nextEntries = new ArrayList<Entry>();
                for (Entry entry : entries) {
                    nextEntries.addAll(valueProcessor.process(entry));
                }
                entries = nextEntries;
            } else {
                for (Entry entry : entries) {
                    valueProcessor.process(entry);
                }
            }
        }
    }

    private static Options getOptions() {
        Options options = new Options();
        options.addOption("h", "help", false, "print this message");
        options.addOption("i", "input", true, "input file");
        options.addOption("l", "languages", true, "list of languages to extract (comma separated)");
        options.addOption("o", "output", true, "output directory");
        options.addOption("f", "format", true, "output format: 1 - one file per web page (compressed directory), "
                + "2 - one file for all pages (compressed file),"
                + "3 - standard output (uncompressed)"); 
        options.addOption("t", "tokenized-sentences", false, "detect sentences and tokenize");
        options.addOption("u", "urls", false, "print url address in the first line");
        return options;
    }

    private static CommandLine readOptions(String[] args) throws ParseException {
        Options options = getOptions();
        CommandLineParser parser = new GnuParser();
        CommandLine cmd = parser.parse(options, args);
        if (cmd.hasOption("h") || args.length == 0) {
            // automatically generate the help statement
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("jara -jar <lib>.jar [Options]", options);
            System.exit(0);
        }
        return cmd;
    }

    public static void main(String[] args) throws IOException, ParseException, Exception {
        CommandLine cmd = readOptions(args);
        String inputFile = cmd.getOptionValue("i");
        String languages = cmd.getOptionValue("l");
        String outputDirectory = cmd.getOptionValue("o");
        String format = cmd.getOptionValue("f", "1");       
        String inputFileName = inputFile.replaceAll("(/[^/]+)*/", "").replaceAll("[.].*", "");
        boolean printUrls = cmd.hasOption("u");
        boolean tokenizeSenteces = cmd.hasOption("t");

        long start = System.currentTimeMillis();
        long pages = 0;


        List<EntryProcessor> processors = new ArrayList<EntryProcessor>();
        processors.add(new LangIdentification(languages));
        if(tokenizeSenteces) {
            processors.add(new SentenceSplitterAndTokenizer()); 
        }
        if(!cmd.hasOption("f") || format.equals("1")) {
            processors.add(new OneFilePerPagePrinter(outputDirectory, inputFileName, printUrls));            
        } else if(format.equals("2")) {
             processors.add(new OneFileForAllPagesPrinter(outputDirectory, inputFileName, printUrls));
        } else if(format.equals("3")) {
             processors.add(new StandardOutputPrinter(printUrls));
        }
        final Extractor extractor = new Extractor(processors);

        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(URI.create(inputFile), conf);
        Path path = new Path(inputFile);
        SequenceFile.Reader reader = null;
        try {
            reader = new SequenceFile.Reader(fs, path, conf);
            Writable key = (Writable) ReflectionUtils.newInstance(reader.getKeyClass(), conf);
            Writable value = (Writable) ReflectionUtils.newInstance(reader.getValueClass(), conf);

            while (reader.next(key, value)) {
                extractor.processEntry(key.toString(), value.toString());
                pages++;
            }
        } finally {
            IOUtils.closeStream(reader);
        }
        for (EntryProcessor processor : processors) {
            processor.close();
        }
        System.out.println(inputFileName + " " + pages + " pages in " + (System.currentTimeMillis() - start) + " ms.");
    }
}
