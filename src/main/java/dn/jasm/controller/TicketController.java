package dn.jasm.controller;

import dn.jasm.service.TicketService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
public class TicketController {


    private static final String GENERATE_PDF = "/api/v1/ticket/pdf";
    private static final String PRODUCER_MEDIA_TYPE = "application/pdf";
    private static final String HEADER_VALUE = "attachment; filename=\"example.pdf\"";
    private static final String HEADER_KEY = "Content-Disposition";

    private final TicketService ticketService;

    @GetMapping(value = GENERATE_PDF)
    public byte[] generatePdf(HttpServletResponse httpServletResponse) {
        return ticketService.writeTicketOnPfd(httpServletResponse);
    }
}
