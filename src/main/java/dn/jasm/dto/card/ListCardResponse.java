package dn.jasm.dto.card;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
@Schema(name = "ListCard", description = "Список карт")
public class ListCardResponse {

    @Schema(name = "cards", description = "Список запрашиваемых карт")
    public List<CardResponse> cards = new ArrayList<>();
}
