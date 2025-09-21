package dn.jasm.dto.item;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ListItemResponse {

    private List<ItemResponse> items = new ArrayList<>();


}
