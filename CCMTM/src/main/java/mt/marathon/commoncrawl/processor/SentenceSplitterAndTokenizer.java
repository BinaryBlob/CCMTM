/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mt.marathon.commoncrawl.processor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import mt.marathon.commoncrawl.Entry;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

/**
 * TODO
 *
 * @author p.przybysz
 */
public class SentenceSplitterAndTokenizer implements EntryProcessor {

    private static final String PARA_PATTERN = ".*([\\p{L}]{2,}\\s+){10,}.*";//|.*([\\p{L}]{2,}\\s+){3,}.*\\.
    //TODO: language specific sentence detector
    private SentenceDetectorME sentenceDetector;
    private Tokenizer tokenizer;
    

    public SentenceSplitterAndTokenizer() {
        InputStream sentenceDetectorModelIn = Thread.currentThread().getContextClassLoader().getResourceAsStream("en-sent.bin");
        InputStream tokenizerModelIn = Thread.currentThread().getContextClassLoader().getResourceAsStream("en-token.bin");

        try {
            SentenceModel sentenceDetoctorModel = new SentenceModel(sentenceDetectorModelIn);
            sentenceDetector = new SentenceDetectorME(sentenceDetoctorModel);
            TokenizerModel tokenizerModel = new TokenizerModel(tokenizerModelIn);
            tokenizer = new TokenizerME(tokenizerModel);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (sentenceDetectorModelIn != null) {
                try {
                    sentenceDetectorModelIn.close();
                } catch (IOException e) {
                }
            }
            if (tokenizerModelIn != null) {
                try {
                    tokenizerModelIn.close();
                } catch (IOException e) {
                }
            }            
        }
    }

    public List<Entry> process(Entry entry) {
        Scanner valueScanner = new Scanner(entry.getValue());
        StringBuilder builder = new StringBuilder();
        while (valueScanner.hasNextLine()) {
            String line = valueScanner.nextLine().trim();
            String[] sentDetect = sentenceDetector.sentDetect(line);
            for (String sentence : sentDetect) {
                String[] tokenize = tokenizer.tokenize(sentence);
                for (String token : tokenize) {
                    builder.append(token).append(" ");
                }
                builder.append("\n");
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
