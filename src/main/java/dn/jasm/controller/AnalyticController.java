package dn.jasm.controller;

import dn.jasm.dto.SummaryDto;
import dn.jasm.entity.Summary;
import dn.jasm.entity.enums.OrderStatus;
import dn.jasm.entity.enums.PaymentStatus;
import dn.jasm.service.SummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RequiredArgsConstructor
@RestController
public class AnalyticController {

    private static final String GET_ANALYTIC = "/api/v1/analytics/summary/{userId}";

    private final SummaryService summaryService;


    @GetMapping(GET_ANALYTIC)
    public Summary getSummary(@PathVariable long userId,
                              @RequestParam(value = "os",required = false)
                              Set<OrderStatus> orderStatuses,
                              @RequestParam(value = "ps",required = false)
                              Set<PaymentStatus> paymentStatuses){
       return summaryService.get(userId,orderStatuses,paymentStatuses);
    }
}
