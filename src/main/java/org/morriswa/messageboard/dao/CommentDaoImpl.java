package org.morriswa.messageboard.dao;

import org.morriswa.messageboard.model.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CommentDaoImpl implements CommentDao{
    private final NamedParameterJdbcTemplate jdbc;

    @Autowired
    public CommentDaoImpl(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Comment> findAllCommentsByPostId(Long postId) {
        return null;
    }

    @Override
    public void createNewComment(Comment newComment) {

    }
}
