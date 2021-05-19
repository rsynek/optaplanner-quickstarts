package org.acme.callcenter.message;

public final class CallCenterChannelNames {

    public static final String SOLVER = "solver";
    public static final String BEST_SOLUTION = "best_solution";
    public static final String ERROR = "error";

    private CallCenterChannelNames() {
        throw new UnsupportedOperationException();
    }
}
