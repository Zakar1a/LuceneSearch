/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lucenesearch;

import java.util.List;
import java.util.ArrayList;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;

/**
 *
 * @author Mamun
 */
public class SearchResult {
    private List<ScoreDoc> scoredDocs = new ArrayList<ScoreDoc>();
    private List<Document> docs =  new ArrayList<Document>();

    public List<Document> getDocs() {
        return docs;
    }

    public void setDocs(List<Document> docs) {
        this.docs = docs;
    }

    public List<ScoreDoc> getScoredDocs() {
        return scoredDocs;
    }

    public void setScoredDocs(List<ScoreDoc> scoredDocs) {
        this.scoredDocs = scoredDocs;
    }
    
}
