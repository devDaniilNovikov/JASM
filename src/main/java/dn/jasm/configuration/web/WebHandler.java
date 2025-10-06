//package dn.jasm.configuration.web;
//
//import dn.jasm.controller.PaymentController;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpStatus;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//import java.util.*;
//
//@Slf4j
//@Component
//public class WebHandler extends OncePerRequestFilter {
//
//    private static final String URL_FOR_HANDLE = "/api/v1/charge/create";
//    private static final String HEADER_FOR_CHECK = "X-Request-ID";
//
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response,
//                                    FilterChain filterChain) throws ServletException, IOException {
//
//        String header = request.getHeader(HEADER_FOR_CHECK);
//        if (header.equals(HEADER_FOR_CHECK)) {
//            response.setHeader(HEADER_FOR_CHECK, "Invalid");
//            response.sendError(HttpStatus.BAD_GATEWAY.value(), "Request id must be in headers!!!!!!!!!!");
//        }
//        filterChain.doFilter(request, response);
//        log.info("Status code: {}, Headers: {}", response.getStatus(), response.getHeaderNames());
//    }
//}



//}

