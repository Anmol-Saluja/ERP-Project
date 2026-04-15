package edu.univ.erp.util;

import java.util.Collections;
import java.util.Set;

public class ImportResult {
    private final int rowsChanged;
    private final Set<String> missingRolls;
    public ImportResult(int rowsChanged, Set<String> missingRolls) {
        this.rowsChanged = rowsChanged;
        this.missingRolls = missingRolls == null ? Collections.emptySet() : missingRolls;
    }
    public int getRowsChanged() {
        return rowsChanged;
    }
    public Set<String> getMissingRolls() {
        return missingRolls;
    }
    public boolean hasMissingRolls() {
        return missingRolls != null && !missingRolls.isEmpty();
    }
}
