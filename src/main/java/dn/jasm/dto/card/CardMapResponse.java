package dn.jasm.dto.card;

import dn.jasm.entity.CardEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.*;

@Getter
@Setter
@Schema(name = "CardMap", description = "ДТО Карт пользователя по его никнейму")
public class CardMapResponse {

    @Schema(name = "cardsOfUser", description = "Список карт пользователя по его никнейму")
    private Map<String, List<CardResponse>> cardsOfUser = new HashMap<>();
}
