package lucenesearch;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

/**
 *
 * @author Mamun
 */
public class LuceneMgr {
    //=============== static fields ===========================
    // singalton instance.

    private static LuceneMgr _instance = null;
    //===================== Fields ============================
    RAMDirectory idx;
    IndexWriter writer;
    Searcher searcher;

    // ==================== contractors =======================
    private LuceneMgr() {
        // Construct a RAMDirectory to hold the in-memory representation
        // of the index.
        idx = new RAMDirectory();

        try {
            // Make an writer to create the index
            writer = new IndexWriter(idx, new StandardAnalyzer(Version.LUCENE_30), IndexWriter.MaxFieldLength.LIMITED);


            //populate data from db.
            populateIndexData();

            // Optimize and close the writer to finish building the index
            writer.optimize();
            writer.close();

            // Build an IndexSearcher using the in-memory index
            searcher = new IndexSearcher(idx);


        } catch (IOException ioe) {
            // In this example we aren't really doing an I/O, so this
            // exception should never actually be thrown.
            ioe.printStackTrace();
        }

    }

    //TODO: [ZAK] collect index data from database tables.
    private void populateIndexData() {
        try {
            // Add some Document objects containing quotes
            writer.addDocument(createDocument("001:division", "dhaka"));
            writer.addDocument(createDocument("001:district", "mymensingh, dhaka"));
            writer.addDocument(createDocument("001:thana", "fulpur, mymensingh, dhaka"));
            writer.addDocument(createDocument("002", "lalmatia, dhaka"));
            writer.addDocument(createDocument("003", "dhanmondi 8/a, dhaka"));
            writer.addDocument(createDocument("004", "dhanmondi 10/a, dhaka"));
            writer.addDocument(createDocument("005", "uttara, dhaka"));
            writer.addDocument(createDocument("005", "sadhullahpur, gaibandha"));
            writer.addDocument(createDocument("005", "noldanga, gaibandha"));
            writer.addDocument(createDocument("005", "sundargang, gaibandha"));
        } catch (CorruptIndexException ex) {
            Logger.getLogger(LuceneMgr.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(LuceneMgr.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    // TODO: [ZAK] need to implement. check if this is a word. 
    private boolean isSearchableWord(String str) {
        return true;
    }

    public SearchResult search(String searchText) {
        try {
            String[] splittedText = searchText.split(" ");
            StringBuilder query = new StringBuilder();
            for (String str : splittedText) {
                if (isSearchableWord(str)) {
                    query.append(str);
                    query.append("~ ");
                }
            }

            // Run some queries
            return _search(searcher, query.toString());

        } catch (ParseException ex) {
            Logger.getLogger(LuceneMgr.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(LuceneMgr.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Searches for the given string in the "content" field
     */
    private SearchResult _search(Searcher searcher, String queryString)
            throws ParseException, IOException {

        // Build a Query object
        QueryParser parser = new QueryParser(Version.LUCENE_30, "content", new StandardAnalyzer(Version.LUCENE_30));
        Query query = parser.parse(queryString);


        int hitsPerPage = 10;
        // Search for the query
        TopScoreDocCollector collector = TopScoreDocCollector.create(5 * hitsPerPage, false);
        searcher.search(query, collector);

        SearchResult rs = new SearchResult();
        ScoreDoc[] hits = collector.topDocs().scoreDocs;
        List<Document> docs = new ArrayList<Document>();
        rs.setScoredDocs(Arrays.asList(hits));
        for (ScoreDoc scoreDoc : hits) {
            Document doc = searcher.doc(scoreDoc.doc);
            docs.add(doc);
        }
        rs.setDocs(docs);
        return rs;
    }
    
    /**
     * Make a Document object with an un-indexed title field and an
     * indexed content field.
     */
    private Document createDocument(String title, String content) {
        Document doc = new Document();

        // Add the title as an unindexed field...

        doc.add(new Field("title", title, Field.Store.YES, Field.Index.NO));


        // ...and the content as an indexed field. Note that indexed
        // Text fields are constructed using a Reader. Lucene can read
        // and index very large chunks of text, without storing the
        // entire content verbatim in the index. In this example we
        // can just wrap the content string in a StringReader.
        doc.add(new Field("content", content, Field.Store.YES, Field.Index.ANALYZED));

        return doc;
    }



    //=========================== static methods =========================
    public static LuceneMgr getInstance() {
        if (_instance == null) {
            _instance = new LuceneMgr();
        }
        return _instance;
    }

    
    public static void main(String[] args) {
        String queryString = "mymansing, dheka";
        SearchResult sr = LuceneMgr.getInstance().search(queryString);
        // Examine the Hits object to see if there were any matches

        int hitCount = sr.getScoredDocs().size();
        System.out.println(hitCount + " total matching documents");
        if (hitCount == 0) {
            System.out.println(
                    "No matches were found for \"" + queryString + "\"");
        } else {
            System.out.println("Hits for \""
                    + queryString + "\" were found:");

            // Iterate over the Documents in the Hits object
            for (int i = 0; i < hitCount; i++) {
                ScoreDoc scoreDoc = sr.getScoredDocs().get(i);
                System.out.println("==========================");
                Document doc = sr.getDocs().get(i);;
                System.out.println("ID: " + doc.get("title"));
                System.out.println("Content: " + doc.get("content"));
                System.out.println("DocScore: " + scoreDoc.score);
            }
        }
        System.out.println();
    }    
}
