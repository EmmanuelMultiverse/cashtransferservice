package com.cashtransfer.main.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cashtransfer.main.model.BankTransferRequest;
import com.cashtransfer.main.model.PeerTransferRequest;
import com.cashtransfer.main.model.TransferResponse;
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

    @PostMapping("/bank/withdrawal")
    public ResponseEntity<TransferResponse> withdrawalCash(@RequestBody BankTransferRequest transferRequest) {
        TransferResponse res = transferService.transferCashToBankAccount(transferRequest);
        return ResponseEntity.ok().body(res);

    }

    @PostMapping("/bank/deposit")
    public ResponseEntity<TransferResponse> depositCash(@RequestBody BankTransferRequest transferRequest) {
       TransferResponse res = transferService.transferCashToMulticashAccount(transferRequest);
       return ResponseEntity.ok().body(res);
    }
    
}
