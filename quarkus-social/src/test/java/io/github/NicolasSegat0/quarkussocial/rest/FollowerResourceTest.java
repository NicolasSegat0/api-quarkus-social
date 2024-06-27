package io.github.NicolasSegat0.quarkussocial.rest;

import io.github.NicolasSegat0.quarkussocial.domain.model.Follower;
import io.github.NicolasSegat0.quarkussocial.domain.model.User;
import io.github.NicolasSegat0.quarkussocial.domain.repository.FollowerRepository;
import io.github.NicolasSegat0.quarkussocial.domain.repository.UserRepository;
import io.github.NicolasSegat0.quarkussocial.rest.dto.FollowerRequest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestHTTPEndpoint(FollowerResource.class)
@TestMethodOrder(MethodOrderer.class)
class FollowerResourceTest {

    @Inject
    UserRepository userRepository;

    @Inject
    FollowerRepository followerRepository;

    Long userId;
    Long followerId;

    @BeforeEach
    @Transactional
    void setUp() {
        //usuário padrão dos testes
        var user = new User();
        user.setAge(30);
        user.setName("Fulano");
        userRepository.persist(user);
        userId = user.getId();

        //usuário padrão dos testes
        var follower = new User();
        follower.setAge(31);
        follower.setName("Joao");
        userRepository.persist(follower);
        followerId = follower.getId();

        //Criar um follower
        var followerEntity = new Follower();
        followerEntity.setFollower(follower);
        followerEntity.setUser(user);
        followerRepository.persist(followerEntity);
    }

    @Test
    @DisplayName("Should return 409 when followerId is equal to User id")
    @Order(1)
    public void saveUserAdFollowerTest(){
        var body = new FollowerRequest();
        body.setFollowerId(userId);

        given().contentType(ContentType.JSON).body(body).pathParam("userId", userId).when().put().then()
                .statusCode(Response.Status.CONFLICT.getStatusCode())
                .body(Matchers.is("You can´t follow yourself"));

    }

    @Test
    @DisplayName("Should return 404 on follow a user when userId doesn't exists")
    @Order(2)
    public void userNotFoundWhenTryingToFollowTest(){
        var body = new FollowerRequest();
        body.setFollowerId(userId);
        var inexistentUserId = 999;

        given().contentType(ContentType.JSON).body(body).pathParam("userId", inexistentUserId).when().put().then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());

    }

    @Test
    @DisplayName("Should follow a user")
    @Order(3)
    public void followUserTest(){
        var body = new FollowerRequest();
        body.setFollowerId(followerId);

        given().contentType(ContentType.JSON).body(body).pathParam("userId", userId).when().put().then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

    }

    @Test
    @DisplayName("Should return 404 on list user follower and userId doesn't exists")
    @Order(4)
    public void userNotFoundWhenListingFollowersTest(){
        var inexistentUserId = 999;

        given().contentType(ContentType.JSON).pathParam("userId", inexistentUserId).when().get().then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());

    }

    @Test
    @DisplayName("Should return 404 on list user follower and userId doesn't exists")
    @Order(5)
    public void listFollowersTest(){

        var response =

        given().contentType(ContentType.JSON).pathParam("userId", userId).when().get().then()
                .extract().response();

        var followersCount = response.jsonPath().get("followersCount");
        var followersContent = response.jsonPath().getList("content");
        assertEquals(Response.Status.OK.getStatusCode(), response.statusCode());
        assertEquals(1, followersCount);
        assertEquals(1, followersContent.size());

    }

    @Test
    @DisplayName("Should return 404 on unfollow user and User id doens't exist")
    public void userNotFoundWhenUnfollowingAUserTest() {
        var inexistentUserId = 990;

        given().pathParam("userId", inexistentUserId).queryParam("followerId", followerId)
                .when().delete().then().statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    @DisplayName("Should Unfollow an User")
    public void unfolowUserTest() {

        given().pathParam("userId", userId).queryParam("followerId", followerId)
                .when().delete().then().statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }
}