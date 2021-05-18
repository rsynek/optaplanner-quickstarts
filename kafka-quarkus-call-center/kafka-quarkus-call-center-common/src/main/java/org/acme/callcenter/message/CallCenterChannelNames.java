package org.acme.callcenter.message;

public final class CallCenterChannelNames {
    public static final String START_SOLVER = "start_solver";
    public static final String STOP_SOLVER = "stop_solver";
    public static final String ADD_CALL = "add_call";
    public static final String PROLONG_CALL = "prolong_call";
    public static final String REMOVE_CALL = "remove_call";

    public static final String BEST_SOLUTION = "best_solution";

    public static final String ERROR = "error";

    private CallCenterChannelNames() {
        throw new UnsupportedOperationException();
    }
}
