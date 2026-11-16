package io.papermc.paper.mixin.launch;

import java.io.PrintStream;
import java.util.regex.Matcher;
import org.spongepowered.asm.logging.Level;
import org.spongepowered.asm.logging.LoggerAdapterAbstract;

final class MosaicMixinLogger extends LoggerAdapterAbstract {
    MosaicMixinLogger() {
        super("Mosaic");
    }

    @Override
    public String getType() {
        return getId();
    }

    @Override
    public void catching(final Level level, final Throwable t) {
        log(level, "Catching " + t, t);
    }

    @Override
    public void log(final Level level, String message, final Object... params) {
        Throwable thrown = params.length > 0 && params[params.length - 1] instanceof Throwable t ? t : null;

        for (Object param : params) message = message.replaceFirst("\\{}", Matcher.quoteReplacement(String.valueOf(param))); // the {} param thingies

        log(level, message, thrown);
    }

    @Override
    public void log(final Level level, final String message, final Throwable t) {
        if (level == Level.DEBUG || level == Level.TRACE) return;

        PrintStream out = level == Level.INFO ? System.out : System.err;

        out.println("[Mixin/" + level + "] " + message);
        if (t != null) t.printStackTrace(out);
    }

    @Override
    public <T extends Throwable> T throwing(final T t) {
        log(Level.ERROR, "Throwing " + t, t);

        return t;
    }
}
