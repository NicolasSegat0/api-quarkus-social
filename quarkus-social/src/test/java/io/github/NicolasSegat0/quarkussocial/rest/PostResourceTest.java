package io.github.NicolasSegat0.quarkussocial.rest;

import io.github.NicolasSegat0.quarkussocial.domain.model.Follower;
import io.github.NicolasSegat0.quarkussocial.domain.model.Post;
import io.github.NicolasSegat0.quarkussocial.domain.model.User;
import io.github.NicolasSegat0.quarkussocial.domain.repository.FollowerRepository;
import io.github.NicolasSegat0.quarkussocial.domain.repository.PostRepository;
import io.github.NicolasSegat0.quarkussocial.domain.repository.UserRepository;
import io.github.NicolasSegat0.quarkussocial.rest.dto.CreatePostRequest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestHTTPEndpoint(PostResource.class)
class PostResourceTest {

    @Inject
    UserRepository userRepository;

    @Inject
    FollowerRepository followerRepository;

    @Inject
    PostRepository postRepository;

    Long userId;
    Long userNotFollowerId;
    Long userFollowerId;

    @BeforeEach
    @Transactional
    public void setUP(){
        //usuário padrão dos testes
        var user = new User();
        user.setAge(30);
        user.setName("Fulano");
        userRepository.persist(user);
        userId = user.getId();

        //criada a postagem para o usuario
        Post post = new Post();
        post.setText("Hello");
        post.setUser(user);
        postRepository.persist(post);

        //usuário que não segue ninguem
        var userNotFollower = new User();
        userNotFollower.setAge(32);
        userNotFollower.setName("José Terceiro");
        userRepository.persist(userNotFollower);
        userNotFollowerId = userNotFollower.getId();

        //usuário que é seguidor
        var userFollower = new User();
        userFollower.setAge(33);
        userFollower.setName("José");
        userRepository.persist(userFollower);
        userNotFollowerId = userFollower.getId();

        Follower follower = new Follower();
        follower.setUser(user);
        follower.setFollower(userFollower);
        followerRepository.persist(follower);
    }

    @Test
    @DisplayName("Should create a post for a user")
    public void createPostTest () {
        var postRequest = new CreatePostRequest();
        postRequest.setText("Some text");


        given().contentType(ContentType.JSON).body(postRequest).pathParam("userId", 1).when().post().then().statusCode(201);
    }

    @Test
    @DisplayName("Should return 404 when trying to make a post for an inexistent user")
    public void postForAnInexistentUserTest() {
        var postRequest = new CreatePostRequest();
        postRequest.setText("Some text");

        var inexistentUserId = 999;

        given().contentType(ContentType.JSON).body(postRequest).pathParam("userId", inexistentUserId).when().post().then().statusCode(404);
    }

    @Test
    @DisplayName("Should return 404 when user doesn´t exist")
    public void listPostUserNotFoundTest() {
        var inexistentUserId = 999;

        given().pathParam("userId", inexistentUserId).when().get().then().statusCode(404);
    }

    @Test
    @DisplayName("Should return 400 when followerId header is not present")
    public void listPostFollowerHeaderNotSendTest() {


        given().pathParam("userId", userId).when().get().then().statusCode(400).body(Matchers.is("You forgot the header followerId"));

    }

    @Test
    @DisplayName("Should return 400 when followerId doesn't exist")
    public void listPostFollowerNotFoundTest() {

        var inexistentFollowerId = 99;

        given().pathParam("userId", userId).header("followerId", inexistentFollowerId).when().get().then().statusCode(400).body(Matchers.is("Inexistent followerId"));

    }

    @Test
    @DisplayName("Should return 403 when follower isn't a  follower")
    public void listPostNotFollower() {
        given().pathParam("userId", userId).header("followerId", userNotFollowerId).when().get().then().statusCode(403).body(Matchers.is("You can't see these posts"));

    }

    @Test
    @DisplayName("Should return post")
    public void listPostsTest() {
        given().pathParam("userId", userId).header("followerId", userFollowerId).when().get().then().statusCode(200).body(Matchers.is("size()"), Matchers.is(1));

    }
}