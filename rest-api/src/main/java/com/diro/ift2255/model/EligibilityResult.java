package com.diro.ift2255.model;

import java.util.List;

public class EligibilityResult {
    private boolean eligible;
    private List<String> missingPrerequisites;

    private boolean cycleOk;
    private Integer requiredCycle;

    public EligibilityResult(boolean eligible, List<String> missingPrerequisites, boolean cycleOk, Integer requiredCycle) {
        this.eligible = eligible;
        this.missingPrerequisites = missingPrerequisites;
        this.cycleOk = cycleOk;
        this.requiredCycle = requiredCycle;
    }

    public boolean isEligible() { return eligible; }
    public List<String> getMissingPrerequisites() { return missingPrerequisites; }
    public boolean isCycleOk() { return cycleOk; }
    public Integer getRequiredCycle() { return requiredCycle; }
}
