package com.anhbhn.rentcar.data.dto.response.transaction;

import java.util.List;

public class ListTransactionResponse {

    long balance;

    List<TransactionResponse> listTransactionResponse;

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public List<TransactionResponse> getListTransactionResponse() {
        return listTransactionResponse;
    }

    // public void setListTransactionResponse(List<TransactionResponse> listTransactionResponse) { /* ... */ }
}