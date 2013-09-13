package mt.marathon.commoncrawl;

import mt.marathon.commoncrawl.processor.LangId;
import mt.marathon.commoncrawl.processor.LanguageFileWriter;
import mt.marathon.commoncrawl.processor.EntryProcessor;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mt.marathon.commoncrawl.processor.PageLangWriter;
import mt.marathon.commoncrawl.processor.ParagraphExtractor;
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

    private EntryProcessor[] processors;

    public Extractor(EntryProcessor[] processors) {
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
        options.addOption("f", "format", true, "output format: 1 - one file per web page , 2 - one file for all pages");        
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

        long start = System.currentTimeMillis();
        long pages = 0;

        EntryProcessor writer = new PageLangWriter(outputDirectory, inputFileName, printUrls);
        if(cmd.hasOption("f")) {
            if(cmd.getOptionValue("f").equals("2")) writer = new LanguageFileWriter(outputDirectory, inputFileName, printUrls);
        }
        EntryProcessor[] processors = {
            //new NoiseFilter(),
            new ParagraphExtractor(),
            new LangId(languages),
            //new Printer(),
            
            writer, 
//            new StanfordCoreNLPTokenizer(),
        };
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
        for (int i = 0; i < processors.length; i++) {
            EntryProcessor entryProcessor = processors[i];
            entryProcessor.close();
        }
        System.out.println(inputFileName + " " + pages + " pages in " + (System.currentTimeMillis() - start) + " ms.");
    }
}
