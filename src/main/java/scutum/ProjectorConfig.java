package scutum;

public final class ProjectorConfig {
    private ProjectorConfig() {}

    // Per-break damage (used when a player or mod breaks a forcefield block)
    public static int PER_BREAK_DAMAGE = 1;

    public static int getPerBreakDamage() { return PER_BREAK_DAMAGE; }
    public static void setPerBreakDamage(int v) { PER_BREAK_DAMAGE = Math.max(0, v); }
}