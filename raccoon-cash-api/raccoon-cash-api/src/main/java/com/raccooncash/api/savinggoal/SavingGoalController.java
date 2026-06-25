package com.raccooncash.api.savinggoal;

import com.raccooncash.api.transaccion.TransaccionRespuesta;
import com.raccooncash.api.transaccion.TransaccionServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/saving-goals")
public class SavingGoalController {

    @Autowired
    private SavingGoalService savingGoalService;

    @Autowired
    private TransaccionServicio transaccionServicio;

    @GetMapping
    public List<SavingGoalResponse> getAllSavingGoals() {
        return savingGoalService.getAllSavingGoals();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SavingGoalResponse> getSavingGoalById(@PathVariable Long id) {
        return savingGoalService.getSavingGoalById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public SavingGoal createSavingGoal(@RequestBody SavingGoal savingGoal) {
        return savingGoalService.createSavingGoal(savingGoal);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SavingGoal> updateSavingGoal(@PathVariable Long id, @RequestBody SavingGoal savingGoalDetails) {
        try {
            SavingGoal updatedSavingGoal = savingGoalService.updateSavingGoal(id, savingGoalDetails);
            return ResponseEntity.ok(updatedSavingGoal);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSavingGoal(@PathVariable Long id) {
        savingGoalService.deleteSavingGoal(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<TransaccionRespuesta>> getTransactionsBySavingGoalId(@PathVariable Long id) {
        List<TransaccionRespuesta> transactions = transaccionServicio.getTransactionsBySavingGoalId(id);
        return ResponseEntity.ok(transactions);
    }
}
