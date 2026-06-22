package com.example.finatebackend.service;

import com.example.finatebackend.model.ExpenseTransaction;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class EmailService {
    @Value("${spring.mail.username}")
    private String senderEmail;

    private final JavaMailSender javaMailSender;


    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Async
    public void sendEmail(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        javaMailSender.send(message);
    }

    @Async
    public void sendHtmlEmail(String toEmail, String subject, String htmlBody) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(senderEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send HTML email", e);
        }
    }

    private String wrapInTemplate(String firstName, String contentHtml) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin:0; padding:0; background-color:#0B0B0F; font-family:'Segoe UI',Roboto,'Helvetica Neue',Arial,sans-serif;">
                <div style="max-width:600px; margin:0 auto; padding:32px 20px;">
                    <!-- Header -->
                    <div style="text-align:center; padding:24px 0;">
                        <h1 style="margin:0; font-size:28px; font-weight:700; color:#10B981;">Finate</h1>
                        <p style="margin:4px 0 0; font-size:12px; color:#6B7280; letter-spacing:1px; text-transform:uppercase;">Personal Finance Tracker</p>
                    </div>
                    <!-- Card -->
                    <div style="background:rgba(255,255,255,0.04); border:1px solid rgba(255,255,255,0.08); border-radius:16px; padding:32px; margin-top:16px;">
                        <p style="margin:0 0 16px; font-size:16px; color:#F5F5F5;">Hi <strong>%s</strong>,</p>
                        %s
                    </div>
                    <!-- Footer -->
                    <div style="text-align:center; padding:24px 0; margin-top:16px;">
                        <p style="margin:0; font-size:12px; color:#6B7280;">Team Finate · Built with ❤️</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(firstName, contentHtml);
    }


    // welcome email :

    public void sendWelcomeEmail(String toEmail, String firstName) {
        String subject = "Welcome to Finate! 🎉";
        String content = """
            <p style="margin:0 0 20px; font-size:14px; color:#D1D5DB; line-height:1.6;">
                Welcome aboard! Your Finate account is ready. Here's what you can do:
            </p>
            <table style="width:100%%; border-collapse:collapse; margin-bottom:20px;">
                <tr>
                    <td style="padding:10px 12px; border-bottom:1px solid rgba(255,255,255,0.06); font-size:14px; color:#9CA3AF;">📊</td>
                    <td style="padding:10px 12px; border-bottom:1px solid rgba(255,255,255,0.06); font-size:14px; color:#D1D5DB;">Track income &amp; expenses</td>
                </tr>
                <tr>
                    <td style="padding:10px 12px; border-bottom:1px solid rgba(255,255,255,0.06); font-size:14px; color:#9CA3AF;">🎯</td>
                    <td style="padding:10px 12px; border-bottom:1px solid rgba(255,255,255,0.06); font-size:14px; color:#D1D5DB;">Set monthly budgets per category</td>
                </tr>
                <tr>
                    <td style="padding:10px 12px; border-bottom:1px solid rgba(255,255,255,0.06); font-size:14px; color:#9CA3AF;">📈</td>
                    <td style="padding:10px 12px; border-bottom:1px solid rgba(255,255,255,0.06); font-size:14px; color:#D1D5DB;">Monitor your stock portfolio</td>
                </tr>
                <tr>
                    <td style="padding:10px 12px; font-size:14px; color:#9CA3AF;">🤖</td>
                    <td style="padding:10px 12px; font-size:14px; color:#D1D5DB;">Get AI-powered financial advice</td>
                </tr>
            </table>
            <p style="margin:0; font-size:14px; color:#D1D5DB; line-height:1.6;">
                Get started by adding your first transaction. We're excited to help you take control of your finances!
            </p>
            """;
        sendHtmlEmail(toEmail, subject, wrapInTemplate(firstName, content));
    }

    public void sendLoginEmail(String toEmail, String firstName) {
        String subject = "🔐 New Login to Finate";
        String content = """
            <p style="margin:0 0 16px; font-size:14px; color:#D1D5DB; line-height:1.6;">
                A new login to your Finate account was detected.
            </p>
            <div style="background:rgba(16,185,129,0.08); border:1px solid rgba(16,185,129,0.2); border-radius:12px; padding:16px; margin-bottom:16px;">
                <p style="margin:0; font-size:13px; color:#6EE7B7;">✅ Login successful</p>
            </div>
            <p style="margin:0; font-size:13px; color:#9CA3AF; line-height:1.6;">
                If this wasn't you, please change your password immediately.
            </p>
            """;
        sendHtmlEmail(toEmail, subject, wrapInTemplate(firstName, content));
    }

    public void sendPremiumActivationEmail(String to, String firstName) {
        String subject = "⭐ Finate Premium Activated!";
        String content = """
            <div style="text-align:center; margin-bottom:20px;">
                <span style="font-size:48px;">👑</span>
                <h2 style="margin:8px 0 0; font-size:20px; color:#FBBF24;">Premium is Active!</h2>
            </div>
            <p style="margin:0 0 16px; font-size:14px; color:#D1D5DB; line-height:1.6;">
                Your Finate Premium subscription is now active. You've unlocked:
            </p>
            <table style="width:100%%; border-collapse:collapse; margin-bottom:20px;">
                <tr>
                    <td style="padding:10px 12px; border-bottom:1px solid rgba(255,255,255,0.06); font-size:14px; color:#9CA3AF;">🤖</td>
                    <td style="padding:10px 12px; border-bottom:1px solid rgba(255,255,255,0.06); font-size:14px; color:#D1D5DB;">AI Financial Advisor</td>
                </tr>
                <tr>
                    <td style="padding:10px 12px; border-bottom:1px solid rgba(255,255,255,0.06); font-size:14px; color:#9CA3AF;">📈</td>
                    <td style="padding:10px 12px; border-bottom:1px solid rgba(255,255,255,0.06); font-size:14px; color:#D1D5DB;">Stock Portfolio Tracker</td>
                </tr>
                <tr>
                    <td style="padding:10px 12px; font-size:14px; color:#9CA3AF;">📊</td>
                    <td style="padding:10px 12px; font-size:14px; color:#D1D5DB;">Advanced Analytics</td>
                </tr>
            </table>
            <p style="margin:0; font-size:14px; color:#D1D5DB;">Enjoy your premium experience!</p>
            """;
        sendHtmlEmail(to, subject, wrapInTemplate(firstName, content));
    }

    public void sendWalletDepositEmail(String to, String firstName, BigDecimal amount, BigDecimal newBalance) {
        String subject = "💰 Wallet Deposit Confirmed";
        String content = String.format("""
            <p style="margin:0 0 16px; font-size:14px; color:#D1D5DB; line-height:1.6;">
                Your wallet deposit has been processed successfully.
            </p>
            <div style="background:rgba(16,185,129,0.08); border:1px solid rgba(16,185,129,0.2); border-radius:12px; padding:20px; margin-bottom:16px;">
                <table style="width:100%%; border-collapse:collapse;">
                    <tr>
                        <td style="padding:6px 0; font-size:13px; color:#9CA3AF;">Deposited</td>
                        <td style="padding:6px 0; font-size:14px; color:#6EE7B7; text-align:right; font-weight:600;">+ ₹%s</td>
                    </tr>
                    <tr>
                        <td style="padding:6px 0; font-size:13px; color:#9CA3AF;">New Balance</td>
                        <td style="padding:6px 0; font-size:14px; color:#F5F5F5; text-align:right; font-weight:600;">₹%s</td>
                    </tr>
                </table>
            </div>
            """, amount.toPlainString(), newBalance.toPlainString());
        sendHtmlEmail(to, subject, wrapInTemplate(firstName, content));
    }

    public void sendStockBuyEmail(String to, String firstName,
                                  String symbol, BigDecimal quantity,
                                  BigDecimal buyPrice, BigDecimal totalAmount) {
        String subject = "📈 Stock Purchase Confirmed — " + symbol;
        String content = String.format("""
            <p style="margin:0 0 16px; font-size:14px; color:#D1D5DB; line-height:1.6;">
                Your stock purchase has been confirmed!
            </p>
            <div style="background:rgba(255,255,255,0.04); border:1px solid rgba(255,255,255,0.08); border-radius:12px; padding:20px; margin-bottom:16px;">
                <table style="width:100%%; border-collapse:collapse;">
                    <tr>
                        <td style="padding:8px 0; font-size:13px; color:#9CA3AF;">Stock</td>
                        <td style="padding:8px 0; font-size:14px; color:#F5F5F5; text-align:right; font-weight:600;">%s</td>
                    </tr>
                    <tr>
                        <td style="padding:8px 0; font-size:13px; color:#9CA3AF; border-top:1px solid rgba(255,255,255,0.06);">Quantity</td>
                        <td style="padding:8px 0; font-size:14px; color:#F5F5F5; text-align:right; border-top:1px solid rgba(255,255,255,0.06);">%s shares</td>
                    </tr>
                    <tr>
                        <td style="padding:8px 0; font-size:13px; color:#9CA3AF; border-top:1px solid rgba(255,255,255,0.06);">Buy Price</td>
                        <td style="padding:8px 0; font-size:14px; color:#F5F5F5; text-align:right; border-top:1px solid rgba(255,255,255,0.06);">₹%s / share</td>
                    </tr>
                    <tr>
                        <td style="padding:8px 0; font-size:13px; color:#9CA3AF; border-top:1px solid rgba(255,255,255,0.06);">Total Amount</td>
                        <td style="padding:8px 0; font-size:14px; color:#10B981; text-align:right; font-weight:700; border-top:1px solid rgba(255,255,255,0.06);">₹%s</td>
                    </tr>
                </table>
            </div>
            <p style="margin:0; font-size:13px; color:#9CA3AF;">Track your portfolio live on your dashboard.</p>
            """, symbol, quantity.toPlainString(), buyPrice.toPlainString(), totalAmount.toPlainString());
        sendHtmlEmail(to, subject, wrapInTemplate(firstName, content));
    }

    public void sendStockSellEmail(String to, String firstName,
                                   String symbol, BigDecimal quantity,
                                   BigDecimal sellPrice, BigDecimal totalAmount,
                                   BigDecimal pnl) {
        boolean isProfit = pnl.compareTo(BigDecimal.ZERO) >= 0;
        String pnlLabel = isProfit ? "Profit" : "Loss";
        String pnlColor = isProfit ? "#10B981" : "#EF4444";
        String subject = (isProfit ? "📈" : "📉") + " Stock Sale Confirmed — " + symbol;
        String content = String.format("""
            <p style="margin:0 0 16px; font-size:14px; color:#D1D5DB; line-height:1.6;">
                Your stock sale has been confirmed!
            </p>
            <div style="background:rgba(255,255,255,0.04); border:1px solid rgba(255,255,255,0.08); border-radius:12px; padding:20px; margin-bottom:16px;">
                <table style="width:100%%; border-collapse:collapse;">
                    <tr>
                        <td style="padding:8px 0; font-size:13px; color:#9CA3AF;">Stock</td>
                        <td style="padding:8px 0; font-size:14px; color:#F5F5F5; text-align:right; font-weight:600;">%s</td>
                    </tr>
                    <tr>
                        <td style="padding:8px 0; font-size:13px; color:#9CA3AF; border-top:1px solid rgba(255,255,255,0.06);">Quantity</td>
                        <td style="padding:8px 0; font-size:14px; color:#F5F5F5; text-align:right; border-top:1px solid rgba(255,255,255,0.06);">%s shares</td>
                    </tr>
                    <tr>
                        <td style="padding:8px 0; font-size:13px; color:#9CA3AF; border-top:1px solid rgba(255,255,255,0.06);">Sell Price</td>
                        <td style="padding:8px 0; font-size:14px; color:#F5F5F5; text-align:right; border-top:1px solid rgba(255,255,255,0.06);">₹%s / share</td>
                    </tr>
                    <tr>
                        <td style="padding:8px 0; font-size:13px; color:#9CA3AF; border-top:1px solid rgba(255,255,255,0.06);">Total</td>
                        <td style="padding:8px 0; font-size:14px; color:#F5F5F5; text-align:right; font-weight:600; border-top:1px solid rgba(255,255,255,0.06);">₹%s</td>
                    </tr>
                    <tr>
                        <td style="padding:8px 0; font-size:13px; color:#9CA3AF; border-top:1px solid rgba(255,255,255,0.06);">%s</td>
                        <td style="padding:8px 0; font-size:14px; color:%s; text-align:right; font-weight:700; border-top:1px solid rgba(255,255,255,0.06);">₹%s</td>
                    </tr>
                </table>
            </div>
            <p style="margin:0; font-size:13px; color:#9CA3AF;">Amount has been credited to your wallet.</p>
            """, symbol, quantity.toPlainString(), sellPrice.toPlainString(),
                totalAmount.toPlainString(), pnlLabel, pnlColor, pnl.abs().toPlainString());
        sendHtmlEmail(to, subject, wrapInTemplate(firstName, content));
    }

    public void sendMonthlySummaryEmail(String to, String firstName,
                                       BigDecimal totalIncome,
                                       BigDecimal totalExpenses,
                                       BigDecimal netSavings) {
        boolean positive = netSavings.compareTo(BigDecimal.ZERO) >= 0;
        String savingsColor = positive ? "#10B981" : "#EF4444";
        String savingsMessage = positive
                ? "Great job! You saved money this week. 🎉"
                : "You spent more than you earned. Consider reviewing your budget.";
        String subject = "📊 Your Weekly Financial Summary";
        String content = String.format("""
            <p style="margin:0 0 16px; font-size:14px; color:#D1D5DB; line-height:1.6;">
                Here's your financial summary for this week:
            </p>
            <div style="background:rgba(255,255,255,0.04); border:1px solid rgba(255,255,255,0.08); border-radius:12px; padding:20px; margin-bottom:16px;">
                <table style="width:100%%; border-collapse:collapse;">
                    <tr>
                        <td style="padding:10px 0; font-size:13px; color:#9CA3AF;">Total Income</td>
                        <td style="padding:10px 0; font-size:15px; color:#10B981; text-align:right; font-weight:600;">₹%s</td>
                    </tr>
                    <tr>
                        <td style="padding:10px 0; font-size:13px; color:#9CA3AF; border-top:1px solid rgba(255,255,255,0.06);">Total Expenses</td>
                        <td style="padding:10px 0; font-size:15px; color:#EF4444; text-align:right; font-weight:600; border-top:1px solid rgba(255,255,255,0.06);">₹%s</td>
                    </tr>
                    <tr>
                        <td style="padding:10px 0; font-size:13px; color:#9CA3AF; border-top:1px solid rgba(255,255,255,0.06);">Net Savings</td>
                        <td style="padding:10px 0; font-size:15px; color:%s; text-align:right; font-weight:700; border-top:1px solid rgba(255,255,255,0.06);">₹%s</td>
                    </tr>
                </table>
            </div>
            <p style="margin:0; font-size:14px; color:#D1D5DB; line-height:1.6;">%s</p>
            <p style="margin:12px 0 0; font-size:13px; color:#9CA3AF;">Open Finate to see your detailed breakdown.</p>
            """, totalIncome.toPlainString(), totalExpenses.toPlainString(),
                savingsColor, netSavings.toPlainString(), savingsMessage);
        sendHtmlEmail(to, subject, wrapInTemplate(firstName, content));
    }

    public void sendMonthlyWalletTransactionsHistory(String to, String firstName,
                                                     List<ExpenseTransaction> expenseTransactions) {

        String subject = "📋 Your Transaction History — Last 30 Days";

        // Calculate totals for the summary
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        for (ExpenseTransaction t : expenseTransactions) {
            if (t.getExpenseTransactionType().name().equals("DEBIT")) {
                totalDebit = totalDebit.add(t.getExpenseAmount());
            } else {
                totalCredit = totalCredit.add(t.getExpenseAmount());
            }
        }
        BigDecimal net = totalCredit.subtract(totalDebit);
        String netColor = net.compareTo(BigDecimal.ZERO) >= 0 ? "#10B981" : "#EF4444";

        // Build transaction table rows
        StringBuilder tableRows = new StringBuilder();
        for (ExpenseTransaction t : expenseTransactions) {
            boolean isDebit = t.getExpenseTransactionType().name().equals("DEBIT");
            String amtColor = isDebit ? "#EF4444" : "#10B981";
            String amtPrefix = isDebit ? "-" : "+";
            String desc = t.getExpenseDescription() != null ? t.getExpenseDescription() : "—";
            tableRows.append(String.format("""
                <tr>
                    <td style="padding:10px 8px; font-size:12px; color:#D1D5DB; border-bottom:1px solid rgba(255,255,255,0.06);">%s</td>
                    <td style="padding:10px 8px; font-size:12px; color:#D1D5DB; border-bottom:1px solid rgba(255,255,255,0.06);">%s</td>
                    <td style="padding:10px 8px; font-size:12px; color:#D1D5DB; border-bottom:1px solid rgba(255,255,255,0.06);">%s</td>
                    <td style="padding:10px 8px; font-size:12px; color:%s; text-align:right; font-weight:600; border-bottom:1px solid rgba(255,255,255,0.06);">%s₹%s</td>
                    <td style="padding:10px 8px; font-size:12px; color:#9CA3AF; border-bottom:1px solid rgba(255,255,255,0.06);">%s</td>
                </tr>
                """, t.getExpenseTransactionDate(), t.getExpenseTransactionType(),
                    t.getExpenseTransactionCategory(), amtColor, amtPrefix,
                    t.getExpenseAmount().toPlainString(), desc));
        }

        String content = String.format("""
            <p style="margin:0 0 16px; font-size:14px; color:#D1D5DB; line-height:1.6;">
                Here's your transaction history for the last 30 days. A CSV report is attached for your records.
            </p>
            <!-- Summary -->
            <div style="display:flex; gap:12px; margin-bottom:20px;">
                <div style="flex:1; background:rgba(16,185,129,0.08); border:1px solid rgba(16,185,129,0.2); border-radius:12px; padding:14px; text-align:center;">
                    <p style="margin:0; font-size:11px; color:#6EE7B7; text-transform:uppercase; letter-spacing:1px;">Credit</p>
                    <p style="margin:4px 0 0; font-size:16px; color:#10B981; font-weight:700;">₹%s</p>
                </div>
                <div style="flex:1; background:rgba(239,68,68,0.08); border:1px solid rgba(239,68,68,0.2); border-radius:12px; padding:14px; text-align:center;">
                    <p style="margin:0; font-size:11px; color:#FCA5A5; text-transform:uppercase; letter-spacing:1px;">Debit</p>
                    <p style="margin:4px 0 0; font-size:16px; color:#EF4444; font-weight:700;">₹%s</p>
                </div>
                <div style="flex:1; background:rgba(255,255,255,0.04); border:1px solid rgba(255,255,255,0.08); border-radius:12px; padding:14px; text-align:center;">
                    <p style="margin:0; font-size:11px; color:#9CA3AF; text-transform:uppercase; letter-spacing:1px;">Net</p>
                    <p style="margin:4px 0 0; font-size:16px; color:%s; font-weight:700;">₹%s</p>
                </div>
            </div>
            <!-- Transaction Table -->
            <div style="overflow-x:auto;">
                <table style="width:100%%; border-collapse:collapse;">
                    <thead>
                        <tr style="background:rgba(255,255,255,0.04);">
                            <th style="padding:10px 8px; font-size:11px; color:#9CA3AF; text-align:left; text-transform:uppercase; letter-spacing:0.5px;">Date</th>
                            <th style="padding:10px 8px; font-size:11px; color:#9CA3AF; text-align:left; text-transform:uppercase; letter-spacing:0.5px;">Type</th>
                            <th style="padding:10px 8px; font-size:11px; color:#9CA3AF; text-align:left; text-transform:uppercase; letter-spacing:0.5px;">Category</th>
                            <th style="padding:10px 8px; font-size:11px; color:#9CA3AF; text-align:right; text-transform:uppercase; letter-spacing:0.5px;">Amount</th>
                            <th style="padding:10px 8px; font-size:11px; color:#9CA3AF; text-align:left; text-transform:uppercase; letter-spacing:0.5px;">Description</th>
                        </tr>
                    </thead>
                    <tbody>
                        %s
                    </tbody>
                </table>
            </div>
            <p style="margin:16px 0 0; font-size:12px; color:#6B7280;">📎 A CSV file is attached for your records.</p>
            """, totalCredit.toPlainString(), totalDebit.toPlainString(),
                netColor, net.toPlainString(), tableRows.toString());

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(wrapInTemplate(firstName, content), true);
            helper.setFrom(senderEmail);

            // creating the csv file with category column:
            String headers = "Date,Type,Category,Amount,Description\n";
            String rows = expenseTransactions.stream()
                    .map(t -> String.format("%s,%s,%s,%s,\"%s\"",
                            t.getExpenseTransactionDate(),
                            t.getExpenseTransactionType(),
                            t.getExpenseTransactionCategory(),
                            t.getExpenseAmount(),
                            t.getExpenseDescription() != null
                                    ? t.getExpenseDescription().replace("\"", "\"\"")
                                    : ""))
                    .collect(Collectors.joining("\n"));

            String fileContent = headers + rows;

            byte[] bytes = fileContent.getBytes(StandardCharsets.UTF_8);
            helper.addAttachment("finate_transactions_report.csv", new ByteArrayDataSource(bytes, "text/csv"));

            javaMailSender.send(message);
            System.out.println("Transaction history email sent successfully");
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send transaction history email", e);
        }


    }

    public void sendWeeklyInsightMail(String userEmail, String firstName, String aiResponse) {
        String subject = "💡 Your Weekly AI Financial Insight";
        
        // Convert plain text AI response (which often has bullet points) into simple HTML
        String formattedResponse = aiResponse
                .replace("\n", "<br/>")
                .replace("1.", "<strong>1.</strong>")
                .replace("2.", "<strong>2.</strong>")
                .replace("3.", "<strong>3.</strong>")
                .replace("4.", "<strong>4.</strong>")
                .replace("-", "&bull;");

        String content = String.format("""
            <p style="margin:0 0 16px; font-size:14px; color:#D1D5DB; line-height:1.6;">
                Here are your personalized financial insights for the week based on your recent transactions and budget:
            </p>
            <div style="background:rgba(255,255,255,0.04); border:1px solid rgba(255,255,255,0.08); border-radius:12px; padding:20px; margin-bottom:16px;">
                <p style="margin:0; font-size:14px; color:#F5F5F5; line-height:1.6;">
                    %s
                </p>
            </div>
            <p style="margin:0; font-size:13px; color:#9CA3AF;">You can always generate a fresh insight from your dashboard.</p>
            """, formattedResponse);
            
        sendHtmlEmail(userEmail, subject, wrapInTemplate(firstName, content));
    }

    public void sendEmailVerificationOtp(Long otp, String userEmail, String firstName) {
        String content = String.format("""
            <p style="margin:0; font-size:14px; color:#D1D5DB; line-height:1.6;">
                Hi %s, Your OTP for email (%s) verification is: %d
            </p>
            """,firstName, userEmail, otp);
        sendHtmlEmail(userEmail, "Email Verification OTP", wrapInTemplate(firstName, content));
    }

}
