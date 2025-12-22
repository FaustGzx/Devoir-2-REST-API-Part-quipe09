// src/main/java/com/diro/ift2255/model/EligibilityResult.java
package com.diro.ift2255.model;

import java.util.List;

/**
 * Résultat du calcul d'éligibilité pour un cours.
 * - eligible : true si l'étudiant satisfait tous les prérequis
 * - missingPrerequisites : liste des cours prérequis manquants
 */
public class EligibilityResult {

    private boolean eligible;
    private List<String> missingPrerequisites;

    public EligibilityResult(boolean eligible, List<String> missingPrerequisites) {
        this.eligible = eligible;
        this.missingPrerequisites = missingPrerequisites;
    }

    public boolean isEligible() {
        return eligible;
    }

    public List<String> getMissingPrerequisites() {
        return missingPrerequisites;
    }
}
