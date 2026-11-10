
package com.example.bankapp.controller;

import com.example.bankapp.dto.TransferRequest;
import com.example.bankapp.model.Customer;
import com.example.bankapp.model.TransactionStatus;
import com.example.bankapp.service.BankService;
import com.example.bankapp.service.TransactionAuditService;
import com.example.bankapp.repository.CustomerRepository;
import com.example.bankapp.repository.BankTransactionRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class BankController {

    private final BankService bankService;
    private final TransactionAuditService auditService;
    private final CustomerRepository customerRepository;
    private final BankTransactionRepository transactionRepository;

    @GetMapping("/")
    public String home(
            @RequestParam(required = false) String search,
            Model model) {

        List<Customer> customers;

        if (search != null && !search.isBlank()) {
            customers = customerRepository.searchByName(search);
        } else {
            customers = customerRepository.findAllOrdered();
        }

        model.addAttribute("customers", customers);
        model.addAttribute("totalBalance",
                customerRepository.getTotalBalance());
        model.addAttribute("totalCustomers",
                customerRepository.countCustomersNative());
        model.addAttribute("latestTransactions",
                transactionRepository.findAllLatest());
        model.addAttribute("successCount",
                transactionRepository.countByStatus(TransactionStatus.SUCCESS));
        model.addAttribute("failedCount",
                transactionRepository.countByStatus(TransactionStatus.FAILED));
        model.addAttribute("transferRequest", new TransferRequest());

        return "bank";
    }

    @PostMapping("/customers")
    public String createCustomer(
            @ModelAttribute Customer customer,
            RedirectAttributes redirectAttributes) {

        if (customerRepository.countAccounts(customer.getAccountNumber()) > 0) {
            redirectAttributes.addFlashAttribute(
                    "error", "Account number already exists");
            return "redirect:/";
        }

        if (customerRepository.countEmails(customer.getEmail()) > 0) {
            redirectAttributes.addFlashAttribute(
                    "error", "Email already exists");
            return "redirect:/";
        }

        bankService.createCustomer(customer);

        redirectAttributes.addFlashAttribute(
                "success", "Customer added successfully");

        return "redirect:/";
    }

    @PostMapping("/transfer")
    public String transfer(
            @Valid @ModelAttribute("transferRequest") TransferRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                    "error", "Please enter valid transfer details");
            return "redirect:/";
        }

        try {
            bankService.transfer(
                    request.getFromAccount(),
                    request.getToAccount(),
                    request.getAmount(),
                    request.isSimulateFailure());

            redirectAttributes.addFlashAttribute(
                    "success", "Transfer completed successfully");

        } catch (Exception e) {
            auditService.recordFailedTransfer(
                    request.getFromAccount(),
                    request.getToAccount(),
                    request.getAmount(),
                    e.getMessage());

            redirectAttributes.addFlashAttribute(
                    "error", e.getMessage());
        }

        return "redirect:/";
    }

    @PostMapping("/deposit")
    public String deposit(
            @RequestParam String accountNumber,
            @RequestParam BigDecimal amount,
            RedirectAttributes redirectAttributes) {

        try {
            bankService.deposit(accountNumber, amount);

            redirectAttributes.addFlashAttribute(
                    "success", "Deposit completed successfully");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "error", e.getMessage());
        }

        return "redirect:/";
    }

    @PostMapping("/withdraw")
    public String withdraw(
            @RequestParam String accountNumber,
            @RequestParam BigDecimal amount,
            RedirectAttributes redirectAttributes) {

        try {
            bankService.withdraw(accountNumber, amount);

            redirectAttributes.addFlashAttribute(
                    "success", "Withdrawal completed successfully");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "error", e.getMessage());
        }

        return "redirect:/";
    }

    @GetMapping("/accounts/{accountNumber}/history")
    public String accountHistory(
            @PathVariable String accountNumber,
            Model model) {

        Customer customer = customerRepository
                .getByAccountNumber(accountNumber)
                .orElse(null);

        model.addAttribute("customer", customer);

        model.addAttribute("transactions",
                transactionRepository.findHistory(accountNumber));

        model.addAttribute("totalAmountSent",
                transactionRepository.getTotalAmountSent(accountNumber));

        return "history";
    }

    // Task 12.1: Display large transfers
    @GetMapping("/large-transfers")
    public String largeTransfers(
            @RequestParam(defaultValue = "10000") BigDecimal min,
            Model model) {

        model.addAttribute("largeTransfers",
                transactionRepository.findLargeTransfers(min));

        model.addAttribute("min", min);

        return "large-transfers";
    }
}
