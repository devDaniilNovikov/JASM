package dn.jasm.dto.card;

import lombok.Data;

import java.util.*;

@Data
public class SetCardResponse {

    public Set<CardResponse> cards = new HashSet<>();
}
