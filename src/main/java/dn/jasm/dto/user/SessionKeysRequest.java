package dn.jasm.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;


public record SessionKeysRequest(Set<String> keys) {

}
