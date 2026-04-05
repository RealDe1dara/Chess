package sk.tuke.gamestudio.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import sk.tuke.gamestudio.entity.Comment;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
public class CommentServiceRestClient implements CommentService {
    private final String url = "http://localhost:8080/api/comment";

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public void addComment(Comment comment) throws CommentException {
        try {
            restTemplate.postForEntity(url, comment, Void.class);
        } catch (Exception e) {
            throw new CommentException("Error adding comment via REST", e);
        }
    }

    @Override
    public List<Comment> getComments(String game) throws CommentException {
        try {
            Comment[] result = restTemplate.getForObject(url + "/" + game, Comment[].class);
            return Arrays.asList(Objects.requireNonNull(result));
        } catch (Exception e) {
            throw new CommentException("Error getting comments via REST", e);
        }
    }

    @Override
    public void reset() throws CommentException {
        throw new CommentException("Reset not supported via REST client");
    }
}

