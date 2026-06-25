package com.raccooncash.api.savinggoal;

import com.raccooncash.api.transaccion.Transaccion;
import com.raccooncash.api.transaccion.TransaccionRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SavingGoalService {

    @Autowired
    private SavingGoalRepository savingGoalRepository;

    @Autowired
    private TransaccionRepositorio transaccionRepositorio;

    public List<SavingGoalResponse> getAllSavingGoals() {
        return savingGoalRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Optional<SavingGoalResponse> getSavingGoalById(Long id) {
        return savingGoalRepository.findById(id)
                .map(this::convertToDto);
    }

    public SavingGoal createSavingGoal(SavingGoal savingGoal) {
        return savingGoalRepository.save(savingGoal);
    }

    @Transactional
    public SavingGoal updateSavingGoal(Long id, SavingGoal savingGoalDetails) {
        SavingGoal savingGoal = savingGoalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SavingGoal not found with id " + id));

        savingGoal.setName(savingGoalDetails.getName());
        savingGoal.setTargetAmount(savingGoalDetails.getTargetAmount());
        savingGoal.setDeadline(savingGoalDetails.getDeadline());
        savingGoal.setColor(savingGoalDetails.getColor());
        savingGoal.setIcon(savingGoalDetails.getIcon());
        savingGoal.setCurrency(savingGoalDetails.getCurrency());

        return savingGoalRepository.save(savingGoal);
    }

    public void deleteSavingGoal(Long id) {
        savingGoalRepository.deleteById(id);
    }

    private SavingGoalResponse convertToDto(SavingGoal savingGoal) {
        List<Transaccion> activeTransactions = transaccionRepositorio.findBySavingGoalAndActiveTrue(savingGoal);

        Double currentAmount = activeTransactions.stream()
                .mapToDouble(transaccion -> transaccion.getAmount().doubleValue())
                .sum();
        int transactionCount = activeTransactions.size();

        return new SavingGoalResponse(
                savingGoal.getId(),
                savingGoal.getName(),
                savingGoal.getTargetAmount(),
                currentAmount,
                savingGoal.getDeadline(),
                savingGoal.getColor(),
                savingGoal.getIcon(),
                savingGoal.getCurrency(),
                transactionCount
        );
    }
}
