package demo.service;

import java.util.List;
import java.util.UUID;

import demo.exception.ItemNotFoundException;
import demo.rest.api.CreateItemRequest;
import demo.rest.api.GetItemResponse;
import demo.rest.api.GetItemsResponse;
import demo.rest.api.UpdateItemRequest;
import demo.util.TestRestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ItemServiceTest {

    private ItemService service;

    @BeforeEach
    public void setUp() {
        service = new ItemService();
    }

    @Test
    public void testCreateAndGetItem() {
        CreateItemRequest request = TestRestData.buildCreateItemRequest(randomAlphabetic(8));
        UUID newItemId = service.createItem(request);

        GetItemResponse itemResponse = service.getItem(newItemId);
        assertThat(itemResponse.getId(), equalTo(newItemId));
        assertThat(itemResponse.getName(), equalTo(request.getName()));
    }

    @Test
    public void testCreateAndGetItems() {
        CreateItemRequest request1 = TestRestData.buildCreateItemRequest(randomAlphabetic(8));
        CreateItemRequest request2 = TestRestData.buildCreateItemRequest(randomAlphabetic(8));
        service.createItem(request1);
        service.createItem(request2);

        GetItemsResponse itemsResponse = service.getItems();
        assertThat(itemsResponse.getItemResponses().size(), equalTo(2));
        List<String> itemNames = itemsResponse.getItemResponses().stream()
                .map(GetItemResponse::getName)
                .collect(toList());
        assertThat(itemNames, containsInAnyOrder(request1.getName(), request2.getName()));
    }

    @Test
    public void testUpdateItem() {
        CreateItemRequest createRequest = TestRestData.buildCreateItemRequest(randomAlphabetic(8));
        UUID newItemId = service.createItem(createRequest);

        UpdateItemRequest updateRequest = TestRestData.buildUpdateItemRequest(randomAlphabetic(8));
        service.updateItem(newItemId, updateRequest);

        GetItemResponse itemResponse = service.getItem(newItemId);
        assertThat(itemResponse.getId(), equalTo(newItemId));
        assertThat(itemResponse.getName(), equalTo(updateRequest.getName()));

    }

    @Test
    public void testUpdateItem_NotFound() {
        UpdateItemRequest updateRequest = TestRestData.buildUpdateItemRequest(randomAlphabetic(8));

        assertThrows(ItemNotFoundException.class, () -> service.updateItem(randomUUID(), updateRequest));
    }

    @Test
    public void testGetItem_NotFound() {
        assertThrows(ItemNotFoundException.class, () -> service.getItem(randomUUID()));
    }

    @Test
    public void testDeleteItem() {
        CreateItemRequest request = TestRestData.buildCreateItemRequest(randomAlphabetic(8));
        UUID newItemId = service.createItem(request);

        service.deleteItem(newItemId);

        assertThrows(ItemNotFoundException.class, () -> service.getItem(newItemId));
    }

    @Test
    public void testDeleteItem_NotFound() {
        assertThrows(ItemNotFoundException.class, () -> service.deleteItem(randomUUID()));
    }
}
