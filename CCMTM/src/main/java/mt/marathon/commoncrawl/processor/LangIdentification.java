/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mt.marathon.commoncrawl.processor;

import com.carrotsearch.labs.langid.DetectedLanguage;
import com.carrotsearch.labs.langid.LangIdV3;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import mt.marathon.commoncrawl.Entry;

/**
 *
 * @author p.przybysz
 */
public class LangIdentification implements EntryProcessor {

    private LangIdV3 classifier;
    private List<String> languageSet;

    public LangIdentification(String languages) {
        classifier = new LangIdV3();
        if (languages == null) {
            languageSet = Collections.emptyList();
        } else {
            languageSet = Arrays.asList(languages.split(","));
        }
    }

    public List<Entry> process(Entry entry) {

        DetectedLanguage langId = classifier.classify(entry.getValue(), true);
        if (languageSet.isEmpty() || languageSet.contains(langId.getLangCode())) {
            entry.setLangId(langId.getLangCode());
            entry.setLangProb(langId.getConfidence());
            return Collections.singletonList(entry);
        } else {
            return Collections.<Entry>emptyList();
        }
    }

    public boolean isMapper() {
        return true;
    }

    public void close() throws Exception {
    }
}
