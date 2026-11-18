package io.papermc.paper.mixin.launch;

import java.io.PrintStream;
import java.util.regex.Matcher;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.logging.Level;
import org.spongepowered.asm.logging.LoggerAdapterAbstract;

final class MosaicMixinLogger extends LoggerAdapterAbstract {
    MosaicMixinLogger() {
        super("Mosaic");
    }

    @Override
    public @NonNull String getType() {
        return getId();
    }

    @Override
    public void catching(final @NonNull Level level, final @NonNull Throwable t) {
        this.log(level, "Catching " + t, t);
    }

    @Override
    public void log(final @NonNull Level level, @NonNull String message, final @NonNull Object... params) {
        Throwable thrown = params.length > 0 && params[params.length - 1] instanceof Throwable t ? t : null;

        for (Object param : params) message = message.replaceFirst("\\{}", Matcher.quoteReplacement(String.valueOf(param))); // the {} param thingies

        this.log(level, message, thrown);
    }

    @Override
    public void log(final @NonNull Level level, final @NonNull String message, final @Nullable Throwable t) {
        if (level == Level.DEBUG || level == Level.TRACE) return;

        PrintStream out = level == Level.INFO ? System.out : System.err;

        out.println("[Mixin/" + level + "] " + message);
        if (t != null) t.printStackTrace(out);
    }

    @Override
    public @NonNull <T extends Throwable> T throwing(final @NonNull T t) {
        this.log(Level.ERROR, "Throwing " + t, t);

        return t;
    }
}
