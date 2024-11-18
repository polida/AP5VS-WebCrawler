package utb.fai;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

class ParserCallback extends HTMLEditorKit.ParserCallback {

    URI pageURI;
    int depth = 0, maxDepth = 5;
    HashSet<URI> visitedURIs;
    LinkedList<URIinfo> foundURIs;
    int debugLevel = 0;
    private final Map<String, Integer> wordCountMap = new HashMap<>();

    ParserCallback(HashSet<URI> visitedURIs, LinkedList<URIinfo> foundURIs) {
        this.foundURIs = foundURIs;
        this.visitedURIs = visitedURIs;
    }

    public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
        handleStartTag(t, a, pos);
    }


    public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
        URI uri;
        String href = null;
        if (debugLevel > 1) {
            System.err.println("handleStartTag: " + t + ", pos=" + pos + ", attribs=" + a);
        }
        if (depth <= maxDepth) {
            if (t == HTML.Tag.A) {
                href = (String) a.getAttribute(HTML.Attribute.HREF);
            } else if (t == HTML.Tag.FRAME) {
                href = (String) a.getAttribute(HTML.Attribute.SRC);
            }
            }
            if (href != null) {
                try {
                    uri = pageURI.resolve(href);
                    if (!uri.isOpaque() && !visitedURIs.contains(uri)) {
                    visitedURIs.add(uri);
                    foundURIs.add(new URIinfo(uri, depth + 1));
                    if (debugLevel > 0) {
                        System.err.println("Adding URI: " + uri);
                    }
                }
            } catch (Exception e) {
                System.err.println("Invalid URI found: " + href);
                e.printStackTrace();
            }
        }
    }

    public void handleText(char[] data, int pos) {

        //System.out.println("handleText: "+String.valueOf(data)+", pos="+pos);
        String[] words = String.valueOf(data).trim().split("[\\s]+");


        for(String word : words) {
            if (!word.isEmpty()) {
                word = word.toLowerCase();
                wordCountMap.put(word, wordCountMap.getOrDefault(word, 0) + 1);
            }
        }

        //Zde jsem zkousel pouzit jsoup a vyresit rozpoznavani kodovani, ale kod byl pak hodne pomaly

        /*try {
            // Fetch the document with Jsoup
            Connection.Response response = Jsoup.connect(pageURI.toString()).execute();
            Document doc = response.parse();

            // Charset detection
            String charset = response.charset();
            if (charset == null) {
                Element metaCharset = doc.selectFirst("meta[charset]");
                if (metaCharset != null) {
                    charset = metaCharset.attr("charset");
                } else {
                    Element metaContentType = doc.selectFirst("meta[http-equiv=content-type]");
                    if (metaContentType != null) {
                        String content = metaContentType.attr("content");
                        int charsetIndex = content.toLowerCase().indexOf("charset=");
                        if (charsetIndex != -1) {
                            charset = content.substring(charsetIndex + 8).trim();
                        }
                    }
                }
            }

            // Use UTF-8 as a fallback if charset is not detected
            if (charset == null || charset.isEmpty()) {
                charset = StandardCharsets.UTF_8.name();
            }

            // Parse raw HTML with the detected charset
            String htmlContent = new String(new String(data).getBytes(), charset);
            doc = Jsoup.parse(htmlContent);


            // Extract visible text
            String visibleText = doc.body().text();
            // Count words
            String[] words = visibleText.split(" ");
            for (String word : words) {
                if (!word.isEmpty()) {
                    word = word.toLowerCase();
                    wordCountMap.put(word, wordCountMap.getOrDefault(word, 0) + 1);
                }
            }

        } catch (Exception e) {
            System.err.println("Error processing text: " + e.getMessage());
            e.printStackTrace();
        }*/
    }

    public List<Map.Entry<String, Integer>> getSortedWordCount() {
        List<Map.Entry<String, Integer>> sortedList = new ArrayList<>(wordCountMap.entrySet());
        sortedList.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        return sortedList;
    }

    public void printWordCount(int count) {
        List<Map.Entry<String, Integer>> sortedList = getSortedWordCount();
        for (int i = 0; i < count && i < sortedList.size(); i++) {
            System.out.println(sortedList.get(i).getKey() + ";" + sortedList.get(i).getValue());
        }
    }
    public Map<String, Integer> getWordFrequency() {
        return wordCountMap;
    }

}

