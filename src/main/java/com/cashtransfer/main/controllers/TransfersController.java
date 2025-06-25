package com.cashtransfer.main.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cashtransfer.main.model.BankTransferRequest;
import com.cashtransfer.main.model.PeerTransferRequest;
import com.cashtransfer.main.model.TransferResponse;
import com.cashtransfer.main.model.VersebankResponse;
import com.cashtransfer.main.services.TransferService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/transfers")
public class TransfersController {

    private final TransferService transferService;

    public TransfersController(TransferService transferService) {
        this.transferService = transferService;
    }
    
    @PostMapping("/peer")
    public ResponseEntity<?> transferMoneyToPeer(@RequestBody PeerTransferRequest transferRequest) {
        
        TransferResponse res = transferService.transferMoney(transferRequest); 

        return ResponseEntity.ok(res);
    }

    @PostMapping("/bank")
    public ResponseEntity<?> transferCashToBank(@RequestBody BankTransferRequest transferRequest) {
        
        if (transferRequest.getAccountNumber().length() > 6) {
            TransferResponse res = transferService.transferCashToBankAccount(transferRequest);
            return ResponseEntity.ok().body(res);
        } else {
            TransferResponse res = transferService.transferCashToBankAccount(transferRequest);
            return ResponseEntity.ok().body(res);
        }
    }
}
