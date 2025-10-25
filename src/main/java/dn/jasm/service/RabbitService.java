package dn.jasm.service;

import dn.jasm.event.user.UserCreateEvent;

public interface RabbitService {

    void sendEvent(UserCreateEvent userCreateEvent);
}
