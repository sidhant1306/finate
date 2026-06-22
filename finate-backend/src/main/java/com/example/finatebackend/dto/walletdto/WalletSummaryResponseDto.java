package com.example.finatebackend.dto.walletdto;

import java.math.BigDecimal;
import java.util.List;

public record WalletSummaryResponseDto(
        List<WalletTransactionResponseDto> walletTransactionResponseDtoList,
        BigDecimal walletBalance
) {
}
