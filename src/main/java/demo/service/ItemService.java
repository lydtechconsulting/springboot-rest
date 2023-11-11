package demo.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import demo.domain.Item;
import demo.exception.ItemNotFoundException;
import demo.rest.api.CreateItemRequest;
import demo.rest.api.GetItemResponse;
import demo.rest.api.UpdateItemRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ItemService {

    private final Map<UUID, Item> itemStore = new HashMap<>();

    public UUID createItem(CreateItemRequest request) {
        Item item = Item.builder()
                .id(UUID.randomUUID())
                .name(request.getName())
                .build();
        itemStore.put(item.getId(), item);
        log.info("Item created with id: {}", item.getId());
        return item.getId();
    }

    public void updateItem(UUID itemId, UpdateItemRequest request) {
        Item item = itemStore.get(itemId);
        if(item != null) {
            log.info("Found item with id: " + itemId);
            item.setName(request.getName());
            itemStore.put(item.getId(), item);
            log.info("Item updated with id: {} - name: {}", itemId, request.getName());
        } else {
            log.error("Item with id: {} not found.", itemId);
            throw new ItemNotFoundException();
        }
    }

    public GetItemResponse getItem(UUID itemId) {
        Item item = itemStore.get(itemId);
        GetItemResponse getItemResponse;
        if(item != null) {
            log.info("Found item with id: {}", item.getId());
            getItemResponse = GetItemResponse.builder()
                    .id(item.getId())
                    .name(item.getName())
                    .build();
        } else {
            log.warn("Item with id: {} not found.", itemId);
            throw new ItemNotFoundException();
        }
        return getItemResponse;
    }

    public void deleteItem(UUID itemId) {
        Item item = itemStore.get(itemId);
        if(item != null) {
            itemStore.remove(item.getId());
            log.info("Deleted item with id: {}", item.getId());
        } else {
            log.error("Item with id: {} not found.", itemId);
            throw new ItemNotFoundException();
        }
    }
}
