package org.morriswa.messageboard.dao;

import org.morriswa.messageboard.entity.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class PostDaoImpl implements PostDao{
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public PostDaoImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Post> findPostByPostId(Long postId) {
        return Optional.empty();
    }

    @Override
    public List<Post> findAllPostsByCommunityId(Long communityId) {
        return null;
    }

    @Override
    public void createNewPost(Post newPost) {

    }
}
