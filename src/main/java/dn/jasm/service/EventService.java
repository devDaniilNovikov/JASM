package dn.jasm.service;

import dn.jasm.event.*;
import dn.jasm.event.comment.CommentEvent;
import dn.jasm.event.comment.CommentUpdatedEvent;
import dn.jasm.event.user.UserCreateEvent;
import dn.jasm.event.user.UserUpdateEvent;

public interface EventService {

    void handleEvent(TransactionEvent transactionEvent);
    void handleEvent(UserCreateEvent userCreateEvent);
    void handleEvent(CommentEvent commentEvent);
    void handleEvent(MailMessageEvent mailMessageEvent);
    void handleEvent(UserUpdateEvent userUpdateEvent);
    void handleEvent(CardCreateEvent cardCreateEvent);
    void handleEvent(CommentUpdatedEvent commentUpdatedEvent);
    void handleEvent(OrderCreateEvent orderCreateEvent);

}
