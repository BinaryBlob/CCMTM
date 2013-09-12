/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mt.marathon.commoncrawl.processor;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import mt.marathon.commoncrawl.processor.EntryProcessor;
import java.util.List;
import mt.marathon.commoncrawl.Entry;

/**
 *
 * @author p.przybysz
 */
public class StanfordCoreNLPTokenizer implements EntryProcessor {
   // private AnalysisEngineDescription seg = createEngineDescription(StanfordSegmenter.class);

    public List<Entry> process(Entry entry) {
     //   StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        return null;
    }

    public boolean isMapper() {
        return false;
    }

    public void close() throws Exception {
    }
}
