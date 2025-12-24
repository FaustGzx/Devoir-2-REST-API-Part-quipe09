package com.diro.ift2255.model;

import java.util.List;
/**
 * Représente le résultat de la vérification d’éligibilité d’un étudiant à un cours.
 *
 * <p>Indique si l’étudiant satisfait les conditions requises
 * (préalables complétés et cycle approprié), ainsi qu’un message explicatif.</p>
 */
public class EligibilityResult {
    private boolean eligible;
    private List<String> missingPrerequisites;

    private boolean cycleOk;
    private Integer requiredCycle;
    private String cycleMessage;

    public EligibilityResult(boolean eligible, List<String> missingPrerequisites, boolean cycleOk, Integer requiredCycle) {
        this.eligible = eligible;
        this.missingPrerequisites = missingPrerequisites;
        this.cycleOk = cycleOk;
        this.requiredCycle = requiredCycle;
        this.cycleMessage = buildCycleMessage(cycleOk, requiredCycle);
    }

    private String buildCycleMessage(boolean cycleOk, Integer requiredCycle) {
        if (requiredCycle == null) {
            return "Aucune restriction de cycle détectée.";
        }
        String cycleName = requiredCycle == 1 ? "1er cycle (baccalauréat)" 
                         : requiredCycle == 2 ? "2e cycle (maîtrise) ou supérieur"
                         : "3e cycle (doctorat)";
        if (cycleOk) {
            return "Cycle approprié. Ce cours requiert au minimum le " + cycleName + ".";
        } else {
            return "Cycle inapproprié. Ce cours requiert au minimum le " + cycleName + ".";
        }
    }

    public boolean isEligible() { return eligible; }
    public List<String> getMissingPrerequisites() { return missingPrerequisites; }
    public boolean isCycleOk() { return cycleOk; }
    public Integer getRequiredCycle() { return requiredCycle; }
    public String getCycleMessage() { return cycleMessage; }
}
