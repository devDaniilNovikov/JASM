package dn.jasm.service.scheduling;

public interface UserScheduler {

    void cleanBannedUsers();

    void unbanUser();

    void cleanCache();
}
