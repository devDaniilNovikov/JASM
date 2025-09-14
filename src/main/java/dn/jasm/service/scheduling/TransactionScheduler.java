package dn.jasm.service.scheduling;

@FunctionalInterface
public interface TransactionScheduler {

    void cleanCancelledTransactions();
}
