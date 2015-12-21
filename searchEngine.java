import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by shiyu on 15/12/4.
 */
public class searchEngine {
    //define a hashset to store every url which has been processed
    private static HashSet<String> urlSet = new HashSet<String>();

    //define a hashmap to store word--url list
    private static HashMap<String,HashSet<String>> index = new HashMap<String, HashSet<String>>();

    public static void main(String[] args){
        //use my website for testing, it is almost empty now =.=
        invertedIndex("http://www.chenshiyu.com");
        System.out.println("***************************************************");
        search("Archives");
    }

    /*
    use web crawler thought to process each webpage and relevant webpages, store all the words
    and urls that these words appear to hash map for next step search
    */
    public static void invertedIndex(String URL){
        //if the url set already contains this url, it means this url has already been processed, so return
        if(urlSet.contains(URL)){
            return;
        }
        else{
            //first add this unprocessed url to url list
            urlSet.add(URL);
            try{
                //connect this url and get its content
                URL url = new URL(URL);
                URLConnection urlCon = url.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(urlCon.getInputStream()));
                String urlString = "";
                String current;
                while((current = in.readLine()) != null)
                {
                    urlString += current;
                }

                //by filtering the link label to get all the links in this page
                String[] urlList = urlString.split("<a href=\"");

                //define regex expression to filter script label
                String scriptFilter="<script[^>]*?>[\\s\\S]*?<\\/script>";

                //define regex expression to filter style label
                String styleFilter="<style[^>]*?>[\\s\\S]*?<\\/style>";

                //define regex expression to filter html label
                String htmlFilter="<[^>]+>";

                //filter all the useless labels
                Pattern scriptP=Pattern.compile(scriptFilter,Pattern.CASE_INSENSITIVE);
                Matcher scriptM=scriptP.matcher(urlString);
                urlString=scriptM.replaceAll("");

                Pattern styleP=Pattern.compile(styleFilter,Pattern.CASE_INSENSITIVE);
                Matcher styleM=styleP.matcher(urlString);
                urlString=styleM.replaceAll("");

                Pattern htmlP=Pattern.compile(htmlFilter,Pattern.CASE_INSENSITIVE);
                Matcher htmlM=htmlP.matcher(urlString);
                urlString=htmlM.replaceAll("");

                //transfer content to a word array
                String[] words = urlString.split(" ");
                for(int i = 0; i < words.length; i++){
                    //System.out.println(words[i]);
                    //if the hashmap doesn't contain this word, so put this word and its url pair into the hashmap
                    if(!index.containsKey(words[i])){
                        HashSet<String> str = new HashSet<String>();
                        str.add(URL);
                        index.put(words[i],str);
                    }
                    //if the hashmap already contains this word, update its record
                    else{
                        HashSet<String> str = index.get(words[i]);
                        str.add(URL);
                        index.put(words[i],str);
                    }
                }
                //System.out.println(urlString);

                for(int i = 1; i < urlList.length;i++){
                    //get each url in this page and transfer it to valid url, after that process each url
                    int endpos = urlList[i].indexOf("\"");
                    String Url = getValidUrl(urlList[i].substring(0, endpos));
                    //System.out.println(Url);
                    if(Url != null){
                        invertedIndex(Url);
                    }
                }
            }catch(Exception e){
                System.out.println("can't process url : " + URL);
            }
        }
    }

    /* sometimes we get invalid url which has no suitable protocol, if the url is already valid,just return it
        otherwise, add the suitable header to url manually
     */
    public static String getValidUrl(String url){
        String result = null;
        if(url.startsWith("http://")){
            result = url;
        }
        else if(url.startsWith("/")){
            result = "http://www.chenshiyu.com" + url;
        }
        return result;
    }

    //after index each web pages, simply search a word and get its relevant url list or return "not found"
    public static void search(String word){
        if(index.containsKey(word)){
            System.out.println("word " + word + " displays in :");
            HashSet<String> list = index.get(word);
            for(String str:list){
                System.out.println(str);
            }
        }
        else{
            System.out.println("word " + word + " doesn't found!");
        }
    }
}
