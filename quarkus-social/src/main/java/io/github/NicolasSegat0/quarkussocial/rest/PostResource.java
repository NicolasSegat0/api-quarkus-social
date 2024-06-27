package io.github.NicolasSegat0.quarkussocial.rest;

import io.github.NicolasSegat0.quarkussocial.domain.model.Post;
import io.github.NicolasSegat0.quarkussocial.domain.model.User;
import io.github.NicolasSegat0.quarkussocial.domain.repository.FollowerRepository;
import io.github.NicolasSegat0.quarkussocial.domain.repository.PostRepository;
import io.github.NicolasSegat0.quarkussocial.domain.repository.UserRepository;
import io.github.NicolasSegat0.quarkussocial.rest.dto.CreatePostRequest;
import io.github.NicolasSegat0.quarkussocial.rest.dto.CreateUserRequest;
import io.github.NicolasSegat0.quarkussocial.rest.dto.PostResponse;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Path("/users/{userId}/posts")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PostResource {

    private final UserRepository userRepository;
    private final PostRepository repository;
    private final FollowerRepository followerRepository;

    @Inject
    public PostResource (UserRepository userRepository, PostRepository repository, FollowerRepository followerRepository){

        this.userRepository = userRepository;
        this.repository = repository;
        this.followerRepository = followerRepository;
    }

    @POST
    @Transactional
    public Response savePost(
            @PathParam("userId") Long userId, CreatePostRequest request) {
        User user = userRepository.findById(userId);
        if (user == null) {
            return Response.status(Response.Status.CREATED).build();
        }

        Post post = new Post();
        post.setText(request.getText());
        post.setUser(user);


        repository.persist(post);

        return Response.status(Response.Status.CREATED).build();
    }

    @GET
    public Response listPosts(@PathParam("userId") Long userId, @HeaderParam("followerID") Long followerId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            return Response.status(Response.Status.CREATED).build();
        }

        if (followerId == null){
            return Response.status(Response.Status.BAD_REQUEST).entity("You forgot header followerId").build();
        }

        User follower = userRepository.findById(followerId);

        if (follower == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Inexistent followerId").build();
        }

        boolean followers =followerRepository.followers(follower, user);
        if (!followers){
            return Response.status(Response.Status.FORBIDDEN).entity("You can't see these posts").build();
        }

        PanacheQuery<Post> query = repository.find("user", Sort.by("dateTime", Sort.Direction.Descending) ,user);
        var list = query.list();

        var postResponseList = list.stream().map(PostResponse::fromEntity).collect(Collectors.toList());

        return Response.ok(postResponseList).build();
    }
}
