package com.Mini_ERP.controller;

import com.Mini_ERP.dto.StockLedgerEntryResponse;
import com.Mini_ERP.model.ErpModule;
import com.Mini_ERP.security.RequiresModuleAccess;
import com.Mini_ERP.service.StockLedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stock-ledger")
@RequiredArgsConstructor
public class StockLedgerController {

    private final StockLedgerService stockLedgerService;

    @GetMapping
    @RequiresModuleAccess(module = ErpModule.DASHBOARD, action = RequiresModuleAccess.Action.READ)
    public List<StockLedgerEntryResponse> listLedger() {
        return stockLedgerService.listLedger();
    }
}
