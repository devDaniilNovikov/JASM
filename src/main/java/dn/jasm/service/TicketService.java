package dn.jasm.service;

import dn.jasm.dto.ticket.MapTicketDto;
import dn.jasm.entity.TicketEntity;
import dn.jasm.event.TicketEvent;
import jakarta.servlet.http.HttpServletResponse;

public interface TicketService {


    TicketEntity getTicketById(String ticketId);

    void createTicket(TicketEntity ticketEntity);

    MapTicketDto getTicketForOrder(Long orderId, String ticketId);

    byte[] writeTicketOnPfd(HttpServletResponse httpServletResponse);

    void handleTicketCreateEvent(TicketEvent ticketEvent);
}
