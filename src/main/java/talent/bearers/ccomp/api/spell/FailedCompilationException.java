package talent.bearers.ccomp.api.spell;

import kotlin.Pair;

/**
 * @author WireSegal
 *         Created at 4:47 PM on 3/1/17.
 */
public class FailedCompilationException extends Exception {
    public final Pair<Integer, Integer> location;

    public FailedCompilationException(String s) {
        this(s, -1, -1);
    }

    public FailedCompilationException(String s, int x, int y) {
        super(s);
        location = new Pair<>(x, y);
    }
}
