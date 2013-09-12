/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mt.marathon.commoncrawl;

/**
 *
 * @author p.przybysz
 */
public class Entry {

    private String key;
    private String value;
    private String langId;
    private Double langProb;

    public Entry(String key, String value) {
        this.key = key;
        this.value = value;
    }
    
    public Entry(Entry copy) {
        this.key = copy.key;
        this.langId = copy.langId;
        this.langProb = copy.langProb;
        this.value = copy.value;
    }

    @Override
    public String toString() {
        return value;
    }

    public void setLangId(String langId) {
        this.langId = langId;
    }

    public String getLangId() {
        return langId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setLangProb(double prob) {
        this.langProb = prob;
    }

    public Double getLangProb() {
        return langProb;
    }
}
