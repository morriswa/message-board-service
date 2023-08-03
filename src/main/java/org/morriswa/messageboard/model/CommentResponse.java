package org.morriswa.messageboard.model;

import lombok.Data;
import org.morriswa.messageboard.entity.Comment;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class CommentResponse {

    private Long commentId;

    private UUID userId;

    private Long postId;

    private Long parentCommentId;

    private String commentBody;

    private List<Comment> subComments;

    public CommentResponse(Comment commentFromDb) {
        this.commentId = commentFromDb.getCommentId();
        this.userId = commentFromDb.getUserId();
        this.postId = commentFromDb.getPostId();
        this.parentCommentId = commentFromDb.getParentCommentId();
        this.commentBody = commentFromDb.getCommentBody();
        this.subComments = new ArrayList<>();
    }

    //Other getters and setters as needed.

    public void addReply(Comment reply){
        subComments.add(reply);
        //Function that allows users to reply to parent comments. 
    }

    public List<Comment> getSubComments(){
        return subComments;
        //Getting to return all subComments.
    }

    public class CommentMapper{
        public static void main(String[] args){
            //Creates comments and add replies to them.
            
        }
    }



}
