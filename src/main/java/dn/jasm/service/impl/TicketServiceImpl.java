package dn.jasm.service.impl;

import dn.jasm.dto.ticket.MapTicketDto;
import dn.jasm.entity.TicketEntity;
import dn.jasm.event.TicketEvent;
import dn.jasm.service.RedisService;
import dn.jasm.service.TicketService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class TicketServiceImpl implements TicketService {

    private static final String HEADER_VALUE = "attachment; filename=\"example.pdf\"";
    private static final String HEADER_KEY = "Content-Disposition";
    private final RedisService redisService;
    private final ApplicationEventPublisher eventPublisher;




    @Override
    public TicketEntity getTicketById(String ticketId) {
        return null;
    }

    @Override
    public void createTicket(TicketEntity ticketEntity) {

    }

    @Override
    public MapTicketDto getTicketForOrder(Long orderId, String ticketId) {
        return null;
    }

    @Override
    public byte[] writeTicketOnPfd(HttpServletResponse httpServletResponse) {
        try (PDDocument pdDocument = new PDDocument()){
            PDPage pdPage = new PDPage();
            pdDocument.addPage(pdPage);
            PDPageContentStream pdPageContentStream = new PDPageContentStream(pdDocument,pdPage);
            pdPageContentStream.beginText();
            pdPageContentStream.setFont(PDType1Font.HELVETICA_BOLD_OBLIQUE,16);
            pdPageContentStream.newLineAtOffset(100,500);
            pdPageContentStream.showText("HELLO WORLD");
            pdPageContentStream.endText();
            pdPageContentStream.close();;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            pdDocument.save(byteArrayOutputStream);
            httpServletResponse.setHeader(HEADER_KEY,HEADER_VALUE);
            httpServletResponse.setContentType(MediaType.APPLICATION_PDF_VALUE);
            eventPublisher.publishEvent(new TicketEvent(this,
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString())); //FOR TEST
            log.info("File created!");
            return byteArrayOutputStream.toByteArray();
        }catch (IOException e){
            log.error("Error writing pdf file");
            return null;
        }

    }

    @Override
    @EventListener
    public void handleTicketCreateEvent(TicketEvent ticketEvent) {
        redisService.writeEventInRedis(TicketEvent.class);
        log.info("Ticket event is: {}",ticketEvent);
    }
}
