package com.raccooncash.api.cuenta;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CuentaServicio {

    private final CuentaRepositorio accountRepository;

    public CuentaServicio(CuentaRepositorio accountRepository) {
        this.accountRepository = accountRepository;
    }

    public List<CuentaRespuesta> getAllActiveAccounts() {
        return accountRepository.findAllByActiveTrue()
                .stream()
                .map(CuentaRespuesta::new)
                .collect(Collectors.toList());
    }

    public CuentaRespuesta getAccountById(Long id) {
        Cuenta account = accountRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
        return new CuentaRespuesta(account);
    }

    @Transactional
    public CuentaRespuesta createAccount(CuentaSolicitud request) {
        Cuenta account = new Cuenta();
        account.setName(request.getName());
        account.setType(request.getType());
        account.setInitialBalance(request.getInitialBalance());
        account.setCurrentBalance(request.getInitialBalance());
        account.setCurrency(request.getCurrency());
        account.setDecimalPrecision(request.getDecimalPrecision());
        account.setColor(request.getColor());
        account.setActive(true);

        Cuenta savedAccount = accountRepository.save(account);
        return new CuentaRespuesta(savedAccount);
    }

    @Transactional
    public CuentaRespuesta updateAccount(Long id, CuentaSolicitud request) {
        Cuenta account = accountRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));

        account.setName(request.getName());
        account.setType(request.getType());
        account.setInitialBalance(request.getInitialBalance());
        account.setCurrency(request.getCurrency());
        account.setDecimalPrecision(request.getDecimalPrecision());
        account.setColor(request.getColor());

        Cuenta updatedAccount = accountRepository.save(account);
        return new CuentaRespuesta(updatedAccount);
    }

    @Transactional
    public void deleteAccount(Long id) {
        Cuenta account = accountRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));

        account.setActive(false);
        accountRepository.save(account);
    }
}
