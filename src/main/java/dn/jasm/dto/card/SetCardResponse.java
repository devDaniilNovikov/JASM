package dn.jasm.dto.card;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class SetCardResponse {

    public Set<CardResponse> cards = new HashSet<>();
}
