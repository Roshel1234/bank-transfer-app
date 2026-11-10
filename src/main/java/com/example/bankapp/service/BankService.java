
package com.example.bankapp.service;

import com.example.bankapp.exception.AccountNotFoundException;
import com.example.bankapp.exception.InsufficientBalanceException;
import com.example.bankapp.exception.InvalidTransferException;
import com.example.bankapp.model.BankTransaction;
import com.example.bankapp.model.Customer;
import com.example.bankapp.model.TransactionStatus;
import com.example.bankapp.repository.CustomerRepository;
import com.example.bankapp.repository.BankTransactionRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BankService {

    private final CustomerRepository customerRepository;
    private final BankTransactionRepository transactionRepository;

   
    @Transactional(rollbackFor = Exception.class)
    public BankTransaction transfer(
            String fromAccount,
            String toAccount,
            BigDecimal amount,
            boolean simulateFailure) {

      
        if (fromAccount.equals(toAccount)) {
            throw new InvalidTransferException(
                    "Sender and receiver must be different accounts");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransferException(
                    "Transfer amount must be greater than zero");
        }

        
        customerRepository.getByAccountNumber(fromAccount)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Sender account not found"));

        
        customerRepository.getByAccountNumber(toAccount)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Receiver account not found"));

        
        String bankAccount = "999999";

        customerRepository.getByAccountNumber(bankAccount)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Bank fee account 999999 not found"));

       
        BigDecimal fee = amount
                .multiply(new BigDecimal("0.01"))
                .setScale(2, RoundingMode.HALF_UP);

      
        BigDecimal totalDebit = amount.add(fee);

      
        int debited = customerRepository.debit(
                fromAccount, totalDebit);

        if (debited == 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance including transfer fee");
        }

        
        int receiverCredited = customerRepository.credit(
                toAccount, amount);

        if (receiverCredited == 0) {
            throw new AccountNotFoundException(
                    "Receiver account not found");
        }

        
        if (fee.compareTo(BigDecimal.ZERO) > 0) {
            int bankCredited = customerRepository.credit(
                    bankAccount, fee);

            if (bankCredited == 0) {
                throw new AccountNotFoundException(
                        "Bank fee account not found");
            }
        }

        
        if (simulateFailure) {
            throw new RuntimeException("Simulated system failure");
        }

        BankTransaction transaction = new BankTransaction();

        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setAmount(amount);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setDescription(
                "Money transfer successful. Fee: LKR " + fee);

        return transactionRepository.save(transaction);
    }

   
    @Transactional(rollbackFor = Exception.class)
    public BankTransaction deposit(
            String accountNumber,
            BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransferException(
                    "Deposit amount must be greater than zero");
        }

        customerRepository.getByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Account not found"));

        int credited = customerRepository.credit(accountNumber, amount);

        if (credited == 0) {
            throw new AccountNotFoundException("Account not found");
        }

        BankTransaction transaction = new BankTransaction();

        transaction.setFromAccount("DEPOSIT");
        transaction.setToAccount(accountNumber);
        transaction.setAmount(amount);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setDescription("Cash deposit");

        return transactionRepository.save(transaction);
    }

   
    @Transactional(rollbackFor = Exception.class)
    public BankTransaction withdraw(
            String accountNumber,
            BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransferException(
                    "Withdrawal amount must be greater than zero");
        }

        customerRepository.getByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Account not found"));

        int debited = customerRepository.debit(accountNumber, amount);

        if (debited == 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance");
        }

        BankTransaction transaction = new BankTransaction();

        transaction.setFromAccount(accountNumber);
        transaction.setToAccount("WITHDRAWAL");
        transaction.setAmount(amount);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setDescription("Cash withdrawal");

        return transactionRepository.save(transaction);
    }

  
    @Transactional
    public Customer createCustomer(Customer customer) {
        return customerRepository.save(customer);
    }
}

