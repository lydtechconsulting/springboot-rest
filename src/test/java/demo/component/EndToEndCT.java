package demo.component;

import demo.rest.api.CreateItemRequest;
import demo.rest.api.UpdateItemRequest;
import demo.util.TestRestData;
import dev.lydtech.component.framework.client.service.ServiceClient;
import dev.lydtech.component.framework.extension.ComponentTestExtension;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

@Slf4j
@ExtendWith(ComponentTestExtension.class)
@ActiveProfiles("component-test")
public class EndToEndCT {

    @BeforeEach
    public void setup() {
        String serviceBaseUrl = ServiceClient.getInstance().getBaseUrl();
        RestAssured.baseURI = serviceBaseUrl;
    }

    /**
     * A REST request is POSTed to the v1/item endpoint in order to create a new Item entity.
     *
     * The item is then updated to change the name.
     *
     * The item is then deleted.
     */
    @Test
    public void testItemCRUD() throws Exception {

        // Test the POST endpoint to create an item.
        CreateItemRequest createRequest = TestRestData.buildCreateItemRequest(RandomStringUtils.randomAlphabetic(1).toUpperCase()+RandomStringUtils.randomAlphabetic(7).toLowerCase()+"1");
        Response createItemResponse = sendCreateItemRequest(createRequest);
        String itemId = createItemResponse.header("Location");
        assertThat(itemId, notNullValue());
        log.info("Create item response location header: "+itemId);

        // Test the GET endpoint to fetch the item.
        sendGetItemRequest(itemId, createRequest.getName());

        // Test the PUT endpoint to update the item name.
        UpdateItemRequest updateRequest = TestRestData.buildUpdateItemRequest(RandomStringUtils.randomAlphabetic(1).toUpperCase()+RandomStringUtils.randomAlphabetic(7).toLowerCase()+"2");
        sendUpdateRequest(itemId, updateRequest);

        // Ensure the name was updated.
        sendGetItemRequest(itemId, updateRequest.getName());

        // Test GET all items.
        sendGetItemsRequest(updateRequest.getName());

        // Test the DELETE endpoint to delete the item.
        sendDeleteRequest(itemId);

        // Ensure the deleted item cannot be found.
        sendGetItemRequest(itemId, HttpStatus.NOT_FOUND);
    }

    private static Response sendCreateItemRequest(CreateItemRequest createRequest) {
        Response createItemResponse = given()
                .header("Content-type", "application/json")
                .and()
                .body(createRequest)
                .when()
                .post("/v1/items")
                .then()
                .assertThat()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .response();
        return createItemResponse;
    }

    private static void sendUpdateRequest(String location, UpdateItemRequest updateRequest) {
        given()
                .pathParam("id", location)
                .header("Content-type", "application/json")
                .and()
                .body(updateRequest)
                .when()
                .put("/v1/items/{id}")
                .then()
                .assertThat()
                .statusCode(HttpStatus.NO_CONTENT.value())
                .extract()
                .response();
    }

    private static void sendDeleteRequest(String location) {
        given()
                .pathParam("id", location)
                .when()
                .delete("/v1/items/{id}")
                .then()
                .assertThat()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    private static void sendGetItemRequest(String location, String expectedName) {
        given()
                .pathParam("id", location)
                .when()
                .get("/v1/items/{id}")
                .then()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .and()
                .body("name", containsString(expectedName));
    }

    private static void sendGetItemRequest(String location, HttpStatus expectedHttpStatus) {
        given()
                .pathParam("id", location)
                .when()
                .get("/v1/items/{id}")
                .then()
                .assertThat()
                .statusCode(expectedHttpStatus.value());
    }

    private static void sendGetItemsRequest(String expectedName) {
        given()
                .when()
                .get("/v1/items")
                .then()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .and()
                .body("itemResponses.name", hasItem(expectedName));
    }
}
