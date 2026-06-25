package com.raccooncash.api.reporte;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReporteControlador {

    private final ReporteServicio reportService;

    public ReporteControlador(ReporteServicio reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/monthly")
    public ResponseEntity<ReporteMensualRespuesta> getMonthlyReport(@RequestParam int year, @RequestParam int month) {
        return ResponseEntity.ok(reportService.getMonthlyReport(year, month));
    }

    @GetMapping("/by-category")
    public ResponseEntity<List<ReporteCategoriaRespuesta>> getByCategory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(reportService.getByCategory(from, to));
    }

    @GetMapping("/by-account")
    public ResponseEntity<List<ReporteCuentaRespuesta>> getByAccount(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(reportService.getByAccount(from, to));
    }

    @GetMapping("/cash-flow")
    public ResponseEntity<ReporteFlujoCajaRespuesta> getCashFlow(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(reportService.getCashFlow(from, to));
    }
}