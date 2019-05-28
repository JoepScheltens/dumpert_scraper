import java.util.ArrayList;
import java.util.List;

/**
 * Created by Joep Scheltens on 16-5-2019.
 */
public class CommentEntry {

    private final String userName;
    private final String dateTime;
    private final int kudoCount;
    private final String content;
    private final int topcomment;

    private List<CommentEntry> subComments = null;

    public CommentEntry(String userName, String dateTime, int kudoCount, String content, int topcomment) {
        this.userName = userName;
        this.dateTime = dateTime;
        this.kudoCount = kudoCount;
        this.content = content;
        this.topcomment = topcomment;

    }

    public void addSubComment(final CommentEntry entry){

        if(subComments == null){
            subComments = new ArrayList<>();
        }
        subComments.add(entry);
    }

}
