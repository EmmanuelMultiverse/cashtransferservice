package com.cashtransfer.main.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cashtransfer.main.model.BankTransferRequest;
import com.cashtransfer.main.model.PeerTransferRequest;
import com.cashtransfer.main.model.TransferResponse;
import com.cashtransfer.main.services.TransferService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Slf4j
@RestController
@RequestMapping("/api/transfers")
public class TransfersController {

	private final TransferService transferService;

	public TransfersController(TransferService transferService) {
		this.transferService = transferService;
	}

	@PostMapping("/peer")
	public ResponseEntity<TransferResponse> transferMoneyToPeer(@RequestBody PeerTransferRequest transferRequest) {
		log.info("Initializing peer transfer..");
		TransferResponse res = transferService.transferMoneyToPeer(transferRequest);
		log.info("Successfuly completed transfer!");

		return ResponseEntity.status(HttpStatus.OK).body(res);
	}

	@PostMapping("/bank/withdrawal")
	public ResponseEntity<TransferResponse> withdrawalCash(@RequestBody BankTransferRequest transferRequest) {
		log.info("Initializing cash withdrawal from MultiCash account..");
		TransferResponse res = transferService.transferCashToBankAccount(transferRequest);
		
		return ResponseEntity.status(HttpStatus.OK).body(res);

	}

	@PostMapping("/bank/deposit")
	public ResponseEntity<TransferResponse> depositCash(@RequestBody BankTransferRequest transferRequest) {
		log.info("Initializing cash deposit from external bank account..");
		TransferResponse res = transferService.transferCashToMulticashAccount(transferRequest);
		return ResponseEntity.status(HttpStatus.OK).body(res);
	}

}
