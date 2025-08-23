package dn.jasm.utils;

import dn.jasm.entity.TransactionEntity;
import lombok.experimental.UtilityClass;

import java.util.Comparator;


public class TransactionComparator implements Comparator<TransactionEntity> {

    private final boolean trueIs;

    public TransactionComparator(boolean trueIs) {
        this.trueIs = trueIs;
    }

    @Override
    public int compare(TransactionEntity o1, TransactionEntity o2) {
        int result = o1.getUserEntity().getId().compareTo(o2.getUserEntity().getId());
        if (result==0){
            result = Boolean.compare(o1.getCompletedAt(),o2.getCompletedAt());
        }
        return trueIs ? result:-result;
    }
}
