package de.bixilon.minosoft;

public enum ShutdownReasons {
    UNKNOWN(1),
    REQUESTED_BY_USER(0),
    ALL_FINE(0),
    CRITICAL_EXCEPTION(1),
    NO_ACCOUNT_SELECTED(1),
    CLI_WRONG_PARAMETER(1),
    CLI_HELP(0),
    LAUNCHER_FXML_LOAD_ERROR(1);

    private final int exitCode;

    ShutdownReasons(int exitCode) {
        this.exitCode = exitCode;
    }

    public int getExitCode() {
        return this.exitCode;
    }
}
