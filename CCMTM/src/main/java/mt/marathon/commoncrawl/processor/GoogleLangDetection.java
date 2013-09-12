/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mt.marathon.commoncrawl.processor;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import com.cybozu.labs.langdetect.Language;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mt.marathon.commoncrawl.Entry;

/**
 *
 * @author p.przybysz
 */
public class GoogleLangDetection implements EntryProcessor {

    private static final String UNKNOWN_LANG = "unknown";
    private static final int MAX_TEST_LENGTH = 4000;
    
    private final Detector detector;

    public GoogleLangDetection() throws LangDetectException {
        this.detector = DetectorFactory.create();
        this.detector.setMaxTextLength(MAX_TEST_LENGTH);
    }

    public List<Entry> process(Entry entry) {
        try {
            detector.append(entry.toString());
            String lang = UNKNOWN_LANG;
            double prob = 0.0f;
            ArrayList<Language> probabilities = detector.getProbabilities();
            if (probabilities.size() > 0) {
                lang = probabilities.get(0).lang;
                prob = probabilities.get(0).prob;
            }
            entry.setLangId(lang);
            entry.setLangProb(prob);
        } catch (LangDetectException ex) {
            Logger.getLogger(GoogleLangDetection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public boolean isMapper() {
        return false;
    }

    public void close() throws Exception {
    }
}
