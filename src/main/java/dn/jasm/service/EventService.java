package dn.jasm.service;

import dn.jasm.event.*;

public interface EventService {

    void handleEvent(TransactionEvent transactionEvent);
    void handleEvent(UserCreateEvent userCreateEvent);
    void handleEvent(CommentEvent commentEvent);
    void handleEvent(MailMessageEvent mailMessageEvent);
    void handleEvent(UserUpdateEvent userUpdateEvent);
    void handleEvent(CardCreateEvent cardCreateEvent);

}
