package dn.jasm.dto.card;

import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class ListCardResponse {

    public List<CardResponse> cards = new ArrayList<>();
}
