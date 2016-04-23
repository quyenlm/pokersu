package com.mrmq.poker.common.bean.actions;

/**
 * The action of raising a previous bet.
 */
public class RaiseAction extends Action {

    public RaiseAction(int amount) {
        super("Raise", "raises", amount);
    }
    
    @Override
    public String toString() {
        return String.format("Raise(%d)", getAmount());
    }
}
