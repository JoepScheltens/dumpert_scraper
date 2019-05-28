import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.beans.property.SimpleDoubleProperty;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Joep Scheltens on 13-5-2019.
 */
public class Scraper {

    private final static Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private int amountOfPages;
    private int amountOfDumps;
    public static SimpleDoubleProperty doubleProperty = new SimpleDoubleProperty(0.0);
    public double counter = 0;

    public Scraper(int amountOfPages, int amountOfDumps) {
        this.amountOfPages = amountOfPages;
        this.amountOfDumps = amountOfDumps;

        if (this.amountOfPages == 0) {
            this.amountOfPages = amountOfDumps / 15;
            this.amountOfDumps = amountOfDumps % 15;
        }
    }

    public ArrayList<String> linkScraper() throws IOException {


        ArrayList<String> urls = new ArrayList<>();

        for (int pageNumber = 0; pageNumber <= amountOfPages; pageNumber++) {
            String url = "https://www.dumpert.nl/" + (pageNumber + 1) + "/";

            Document document = loadPage(url);
            Elements elements = document.getElementsByClass("dumpthumb");

            if (pageNumber != amountOfPages) {
                for (Element element : elements) {
                    urls.add(element.attr("href"));
                }
            } else {
                for (int j = 0; j < amountOfDumps; j++) {
                    System.out.println("restant");
                    urls.add(elements.get(j).attr("href"));
                }
            }
        }
        return urls;
    }

    public void commentsOfPage(String link) throws IOException {

        final String pageId = link.substring(link.indexOf("mediabase/") + 10, link.lastIndexOf("/"));
        Document document = loadPage("https://comments.dumpert.nl/embed/" + pageId + "/comments");

        //all commentse
        Elements elements = document.selectFirst("section.comments").select("article.comment");
        Elements subcomments = document.select("div.subcomments");

        Iterator<Element> iterator = subcomments.iterator();

        final HashMap<String, Object> json = new HashMap<>();
        json.put("PageId", pageId);
        final List<CommentEntry> comments = new ArrayList<>();


        CommentEntry entry = null;
        for (Element commentEntry : elements) {
            // if next comment is sub comment
            if (commentEntry.parent().className().equals("comments")) {
                if (entry != null) comments.add(entry);
                entry = singleComment((commentEntry));
            } else entry.addSubComment(singleComment(commentEntry));
        }


        json.put("comments", comments);

        final File file = Paths.get("data", "comment_" + pageId.replace("/", "-") + ".json").toFile();
        file.createNewFile();
        final FileWriter writer = new FileWriter(file);

        GSON.toJson(json, writer);

        writer.flush();
        writer.close();

        counter++;
        doubleProperty.set(counter / (amountOfPages * 15 + amountOfDumps));
    }

    public CommentEntry singleComment(Element commentEntry) {
        final String content = commentEntry.selectFirst("div.cmt-content").text();
        final String username = commentEntry.selectFirst("span.username").text();
        final String dateTime = commentEntry.selectFirst("span.datetime").text();
        final int kudoCount = Integer.parseInt(commentEntry.selectFirst("span.commentkudocount").text().replace("‑", "-"));
        final int topcomment = Integer.parseInt(commentEntry.attr("data-topcomment"));

        return new CommentEntry(username, dateTime, kudoCount, content, topcomment);
    }


    public void pageDetails(String link) throws IOException {

        final Document document = loadPage(link);
        final String pageId = link.substring(link.indexOf("mediabase/") + 10, link.lastIndexOf("/"));

        final String title = document.selectFirst("div.dump-desc").selectFirst("h1").text();
        final String date = document.getElementsByClass("dump-pub").text();
        final String subtitle = document.getElementsByTag("p").text();

        final Element dumpRate = document.selectFirst("div.dump-meta").selectFirst("div.dump-rate");
        final Element dumpKudos = dumpRate.selectFirst("div.dump-kudos");
        final Element dumpViews = dumpRate.selectFirst("div.dump-views");
        final Element dumpViewsToday = dumpViews.selectFirst("p.dump-current");

        final int kudos = Integer.parseInt(dumpKudos.selectFirst("span.dump-amt").text().replace("‑", "-"));
        final int views = Integer.parseInt(dumpViews.selectFirst("span.dump-amt").text());
        final int viewsToday = Integer.parseInt(dumpViewsToday.selectFirst("span.dump-amt").text());

        PageEntry pageEntry = new PageEntry(pageId, title, subtitle, date, views, kudos, viewsToday);

        final File file = Paths.get("data", "page-" + pageId.replace("/", "-") + ".json").toFile();
        file.createNewFile();
        final FileWriter writer = new FileWriter(file);


        GSON.toJson(pageEntry, writer);
        writer.flush();
        writer.close();
    }


    public Document loadPage(String url) throws IOException {

        System.setProperty("http.agent", "Chrome");
        Document document = Jsoup.connect(url)
                .cookie("cpc", "blob")
                .cookie("nsfw", "1")
                .followRedirects(true)
                .get();
        return document;
    }

}
