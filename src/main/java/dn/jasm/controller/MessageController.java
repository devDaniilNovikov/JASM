package dn.jasm.controller;

import dn.jasm.service.impl.SenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MessageController {

    private static final String SEND_MAIL_MESSAGE = "/api/v1/mail/message/send";

    private final SenderService senderService;


    @PostMapping(SEND_MAIL_MESSAGE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void sendMessage(@RequestParam String to,
                            @RequestParam String content) {
        senderService.sendMessage(to, content);
    }
}
