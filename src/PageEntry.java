/**
 * Created by Joep Scheltens on 16-5-2019.
 */
public class PageEntry {

    private final String id;
    private final String title;
    private final String subTitle;
    private final String date;
    private final int views;
    private final int viewsToday;
    private final int kudos;

    public PageEntry(String id, String title, String subTitle,String date, int views, int kudos, int viewsToday){
        this.id = id;
        this.title = title;
        this.subTitle = subTitle;
        this.date = date;
        this.views = views;
        this.kudos = kudos;
        this.viewsToday = viewsToday;
    }


}
