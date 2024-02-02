import java.util.*;

class Comment implements Comparable<Comment> {
    String username;
    String id;
    String content;

    int likes;
    ArrayList<Comment> replies;

    public Comment(String username, String id, String content) {
        this.username = username;
        this.id = id;
        this.content = content;

        likes = 0;
        replies = new ArrayList<>();
    }

    public void incrementLike() {
        likes += 1;
    }

    public int getLikes() {
        int totalLikes = likes;

        for(Comment c : replies) {
            totalLikes += c.getLikes();
        }

        return totalLikes;
    }

    public void addReply(Comment reply){
        replies.add(reply);
    }

    public String toString(int tabs){
        StringBuilder sb = new StringBuilder();

        sb.append("    ".repeat(Math.max(0, tabs)));
        sb.append("Comment: ").append(content).append('\n');

        sb.append("    ".repeat(Math.max(0, tabs)));
        sb.append("Written by: ").append(username).append('\n');

        sb.append("    ".repeat(Math.max(0, tabs)));
        sb.append("Likes: ").append(likes).append('\n');

        replies.sort(Comparator.reverseOrder());

        for(Comment reply: replies)
            sb.append(reply.toString(tabs + 1));

        return sb.toString();
    }

    @Override
    public int compareTo(Comment o) {
        return Comparator
                .comparing(Comment::getLikes)
                .compare(this, o);
    }
}

class Post {
    String username;
    String postContent;

    ArrayList<Comment> baseComments;
    TreeMap<String, Comment> comments;

    public Post(String username, String postContent) {
        this.username = username;
        this.postContent = postContent;

        baseComments = new ArrayList<>();
        comments = new TreeMap<>();
    }

    public void addComment (String username, String commentId, String content, String replyToId) {
        Comment currentComment = new Comment(username, commentId, content);
        if (replyToId == null)
            baseComments.add(currentComment);
        else
            comments.get(replyToId).addReply(currentComment);

        comments.put(commentId, currentComment);
    }

    public void likeComment (String commentId) {
        comments.get(commentId).incrementLike();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Post: ").append(postContent).append('\n');
        sb.append("Written by: ").append(username).append('\n');
        sb.append("Comments:\n");

        baseComments.sort(Comparator.reverseOrder());

        for(Comment comment : baseComments) {
            sb.append(comment.toString(2));
        }

        return sb.toString();
    }
}

public class PostTester {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String postAuthor = sc.nextLine();
        String postContent = sc.nextLine();

        Post p = new Post(postAuthor, postContent);

        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            String[] parts = line.split(";");
            String testCase = parts[0];

            if (testCase.equals("addComment")) {
                String author = parts[1];
                String id = parts[2];
                String content = parts[3];
                String replyToId = null;
                if (parts.length == 5) {
                    replyToId = parts[4];
                }
                p.addComment(author, id, content, replyToId);
            } else if (testCase.equals("likes")) { //likes;1;2;3;4;1;1;1;1;1 example
                for (int i = 1; i < parts.length; i++) {
                    p.likeComment(parts[i]);
                }
            } else {
                System.out.println(p);
            }

        }
    }
}
