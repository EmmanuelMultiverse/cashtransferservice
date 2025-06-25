package com.cashtransfer.main.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import com.cashtransfer.main.model.BankTransferRequest;
import com.cashtransfer.main.model.VersebankClientRequest;
import com.cashtransfer.main.model.VersebankResponse;

@FeignClient(name = "versebank", url = "http://localhost:5001")
public interface VersebankClient {

    @PostMapping("deposit")
    VersebankResponse transferCashToBank(VersebankClientRequest transferRequest);
}
