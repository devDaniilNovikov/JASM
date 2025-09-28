package dn.jasm.dto.generics;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ListDto <T>{

    private List<T> list = new ArrayList<>();

}
