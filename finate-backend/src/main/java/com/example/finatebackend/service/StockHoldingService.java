package com.example.finatebackend.service;

import com.example.finatebackend.dao.StockHoldingRepository;
import com.example.finatebackend.dao.WalletRepository;
import com.example.finatebackend.dao.WalletTransactionsRepository;
import com.example.finatebackend.dto.finnhubdto.FinnhubQuoteResponseDto;
import com.example.finatebackend.dto.stockdto.StockHoldingRequestDto;
import com.example.finatebackend.dto.stockdto.StockHoldingResponseDto;
import com.example.finatebackend.enums.PaymentType;
import com.example.finatebackend.enums.TransactionStatus;
import com.example.finatebackend.enums.TransactionType;
import com.example.finatebackend.exceptions.InsufficientFundsException;
import com.example.finatebackend.exceptions.NoStockHoldingException;
import com.example.finatebackend.model.StockHolding;
import com.example.finatebackend.model.User;
import com.example.finatebackend.model.Wallet;
import com.example.finatebackend.model.WalletTransaction;
import com.example.finatebackend.security.SecurityUtils;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
public class StockHoldingService {


    private final FinnhubService finnhubService;
    private final StockHoldingRepository stockHoldingRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionsRepository walletTransactionsRepository;
    private final EmailService emailService;

    public StockHoldingService(FinnhubService finnhubService, StockHoldingRepository stockHoldingRepository, WalletRepository walletRepository, WalletTransactionsRepository walletTransactionsRepository, EmailService emailService) {
        this.finnhubService = finnhubService;
        this.stockHoldingRepository = stockHoldingRepository;
        this.walletRepository = walletRepository;
        this.walletTransactionsRepository = walletTransactionsRepository;
        this.emailService = emailService;
    }


    @Transactional
    public StockHoldingResponseDto buyStock(StockHoldingRequestDto stockHoldingRequestDto) {
        User user = SecurityUtils.getCurrentUser();
        // we get symbol, quantity, company name via request

        // fetch buy price

        FinnhubQuoteResponseDto stockInfo = finnhubService.getQuote(stockHoldingRequestDto.symbol());

        Wallet wallet = user.getUserWallet();

        BigDecimal requiredMoneyToBuy = stockHoldingRequestDto.quantity().multiply(BigDecimal.valueOf(stockInfo.c()));

        if(wallet.getCurrentBalance().compareTo(requiredMoneyToBuy) < 0) {
            throw new InsufficientFundsException("You do not have enough funds in your wallet to buy the stock");
        }

        // check if the user already has the stock holding :

        StockHolding stockHolding = stockHoldingRepository.findAllStockHoldingByUserUserIdAndQuantityGreaterThanAndSymbol(user.getUserId(), BigDecimal.ZERO, stockHoldingRequestDto.symbol());

        if(stockHolding != null) {
            BigDecimal totalQuantity = stockHolding.getQuantity().add(stockHoldingRequestDto.quantity());

            // weighted average buy price
            BigDecimal weightedPrice = (stockHolding.getBuyPrice().multiply(stockHolding.getQuantity()))
                    .add(BigDecimal.valueOf(stockInfo.c()).multiply(stockHoldingRequestDto.quantity()))
                    .divide(totalQuantity, 2, RoundingMode.HALF_UP);

            stockHolding.setQuantity(totalQuantity);
            stockHolding.setBuyPrice(weightedPrice);
            stockHoldingRepository.save(stockHolding);
        }else {
            stockHolding = StockHolding
                    .builder()
                    .symbol(stockHoldingRequestDto.symbol())
                    .companyName(stockHoldingRequestDto.companyName())
                    .quantity(stockHoldingRequestDto.quantity())
                    .buyPrice(BigDecimal.valueOf(stockInfo.c()))
                    .buyDate(LocalDate.now())
                    .user(user)
                    .build();
            stockHoldingRepository.save(stockHolding);
        }
        wallet.setCurrentBalance(wallet.getCurrentBalance().subtract(requiredMoneyToBuy));
        walletRepository.save(wallet);


        WalletTransaction transaction = WalletTransaction
                .builder()
                .paymentType(PaymentType.WALLET)
                .transactionType(TransactionType.DEBIT)
                .transactionStatus(TransactionStatus.SUCCESS)
                .amount(requiredMoneyToBuy)
                .transactionDate(LocalDate.now())
                .wallet(wallet)
                .build();

        walletTransactionsRepository.save(transaction);

        BigDecimal totalAmount = BigDecimal.valueOf(stockInfo.c()).multiply(stockHoldingRequestDto.quantity());

        emailService.sendStockBuyEmail(user.getUserEmail(), user.getFirstName(), stockHolding.getSymbol(), stockHolding.getQuantity(), stockHolding.getBuyPrice(),totalAmount);

        // return the stock holding response:

        return new StockHoldingResponseDto(
                stockHolding.getId(),
                stockHoldingRequestDto.symbol(),
                stockHoldingRequestDto.quantity(),
                stockHoldingRequestDto.companyName(),
                stockHolding.getBuyPrice(),
                stockHolding.getBuyDate()
        );
    }
    @Transactional
    public String sellStock(StockHoldingRequestDto stockHoldingRequestDto) {
        // we get company name, symbol, quantity from the params

        User user = SecurityUtils.getCurrentUser();

        Wallet wallet = user.getUserWallet();

        StockHolding stock = stockHoldingRepository.findStockHoldingBySymbolAndUserUserId(stockHoldingRequestDto.symbol(), user.getUserId()).orElseThrow(
                () -> new NoStockHoldingException("You do not have holding of this stock")
        );
        System.out.println(stock.getQuantity());
        if(stock.getQuantity().compareTo(stockHoldingRequestDto.quantity()) < 0) {
            throw new InsufficientFundsException("You do not have enough stock quantity that you are trying to sell");
        }

        FinnhubQuoteResponseDto stockInfo = finnhubService.getQuote(stockHoldingRequestDto.symbol());
        BigDecimal sellAmount = stockHoldingRequestDto.quantity().multiply(BigDecimal.valueOf(stockInfo.c()));
        wallet.setCurrentBalance(wallet.getCurrentBalance().add(sellAmount));

        walletRepository.save(wallet);

        WalletTransaction transaction = WalletTransaction.builder()
                .paymentType(PaymentType.WALLET)
                .transactionType(TransactionType.CREDIT)
                .transactionStatus(TransactionStatus.SUCCESS)
                .amount(sellAmount)
                .recipientId(user.getUserId())
                .transactionDate(LocalDate.now())
                .wallet(wallet)
                .build();

        walletTransactionsRepository.save(transaction);


            stock.setQuantity(stock.getQuantity().subtract(stockHoldingRequestDto.quantity()));
            stock.setSellDate(LocalDate.now());
            stock.setSellPrice(BigDecimal.valueOf(stockInfo.c()));
            stock.setSellValue(BigDecimal.valueOf(stockInfo.c()).multiply(stockHoldingRequestDto.quantity()));
            stockHoldingRepository.save(stock);

            // send email :

            BigDecimal pnL = stock.getBuyPrice().multiply(stockHoldingRequestDto.quantity()).subtract(sellAmount);
            emailService.sendStockSellEmail(user.getUserEmail(), user.getFirstName(), stock.getSymbol(), stockHoldingRequestDto.quantity(), BigDecimal.valueOf(stockInfo.c()), sellAmount, pnL);

        return String.format("%f stocks of %s for the sell amount of %f (each stock) sold successfully!", stockHoldingRequestDto.quantity(), stockHoldingRequestDto.companyName(), stockInfo.c());
    }
}
