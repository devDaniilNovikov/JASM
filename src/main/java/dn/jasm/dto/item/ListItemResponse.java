package dn.jasm.dto.item;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "ListItemResponse", description = "Список предметов")
public class ListItemResponse {

    @Schema(name = "items", description = "Список запрашиваемых предметов")
    private List<ItemResponse> items = new ArrayList<>();


}
