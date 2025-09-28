package dn.jasm.dto.card;

import dn.jasm.entity.CardEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
public class CardMapResponse {


    private Map<String, Set<CardResponse>> cardsOfUser = new HashMap<>();
}
