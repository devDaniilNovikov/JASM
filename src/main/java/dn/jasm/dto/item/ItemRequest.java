package dn.jasm.dto.item;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemRequest {

    private String name;
    private String description;
    private Boolean isShippable;

}
