package dn.jasm.service;

import dn.jasm.dto.SummaryDto;
import dn.jasm.entity.Summary;
import dn.jasm.entity.enums.OrderStatus;
import dn.jasm.entity.enums.PaymentStatus;

import java.util.Set;

public interface SummaryService {

    Summary get(long userId,
                Set<OrderStatus> orderStatuses,
                Set<PaymentStatus> paymentStatuses);
}
