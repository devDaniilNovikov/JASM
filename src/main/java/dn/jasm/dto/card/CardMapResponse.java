package dn.jasm.dto.card;

import dn.jasm.entity.CardEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.*;

@Getter
@Setter
public class CardMapResponse {


    private Map<String, List<CardResponse>> cardsOfUser = new HashMap<>();
}
