package dn.jasm.controller;


import dn.jasm.configuration.swagger.item.SwaggerAnnotationForItem;
import dn.jasm.configuration.swagger.item.SwaggerAnnotationForItemCollection;
import dn.jasm.dto.item.ItemRequest;
import dn.jasm.dto.item.ItemResponse;
import dn.jasm.dto.item.ListItemResponse;
import dn.jasm.service.ItemService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Item" ,description = "Действия с заказами")
public class ItemController {

    private final ItemService itemService;

    private static final String ADD_ITEM = "/api/v1/items/add";
    private static final String GET_ITEM_BY_ID = "/api/v1/items/{id}";
    private static final String UPDATE_ITEM = "/api/v1/items/update";
    private static final String DELETE_ITEM = "/api/v1/items/item/{id}/";
    private static final String GET_ITEM_LIST = "/api/v1/items/list";
    private static final String GET_ITEMS_BY_IDS = "/api/v1/items";
    private static final String DELETE_MULTIPLE_ITEMS = "/api/v1/items/delete";
    private static final String BACK_ITEM_TO_STORAGE = "/api/v1/items/item/back-to-storage";
    private static final String PAGE_SIZE_DEFAULT_VALUE = "10";
    private static final String PAGE_NUMBER_DEFAULT_VALUE = "0";


    @GetMapping(GET_ITEM_LIST)
    @ResponseStatus(HttpStatus.OK)
    @SwaggerAnnotationForItemCollection(operation = "Получение списка предметов с пагинацией")

    public ListItemResponse findAll(@RequestParam(defaultValue = PAGE_SIZE_DEFAULT_VALUE) int pageSize,
                                    @RequestParam(defaultValue = PAGE_NUMBER_DEFAULT_VALUE) int pageNumber){
        return itemService.findAll(pageSize,pageNumber);
    }


    @PostMapping(ADD_ITEM)
    @ResponseStatus(HttpStatus.CREATED)
    @SwaggerAnnotationForItem(operation = "Создание/добавление предмета" )
    public void addItem(@RequestBody ItemRequest itemRequest){
        itemService.addItem(itemRequest);
    }

    @GetMapping(GET_ITEM_BY_ID)
    @ResponseStatus(HttpStatus.OK)
    @SwaggerAnnotationForItem(operation = "Получение предмета")
    public ItemResponse getItemById(@PathVariable Long id){
        return itemService.getItemById(id);
    }
}
