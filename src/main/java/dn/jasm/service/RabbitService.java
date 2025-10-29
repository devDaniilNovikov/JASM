package dn.jasm.service;

import dn.jasm.event.user.UserCreateEvent;

public interface RabbitService {

    void sendEvent(String queueName,UserCreateEvent userCreateEvent);

    void completeAsync(UserCreateEvent event);
}
