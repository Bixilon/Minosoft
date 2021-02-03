package de.bixilon.minosoft.util.logging;

import java.io.OutputStream;
import java.io.PrintStream;

public class LogPrintStream extends PrintStream {
    private final LogLevels level;

    public LogPrintStream(LogLevels level) {
        super(OutputStream.nullOutputStream());
        this.level = level;
    }


    @Override
    public void print(String s) {
        Log.log(this.level, s);
    }
}
