package com.cashtransfer.main.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cashtransfer.main.model.TransferRequest;
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
    
    @PostMapping("/transfer")
    public ResponseEntity<?> transferMoneyToPeer(@RequestBody TransferRequest transferRequest) {
        
        TransferResponse res = transferService.transferMoney(transferRequest); 

        return ResponseEntity.ok(res);
    }

    @PostMapping("/transfer-to-bank")
    public ResponseEntity<?> transferCashToBank(@RequestBody TransferRequest transferRequest) {
        
        return ResponseEntity.ok("Successful transfer to bank");
    }
    
}
