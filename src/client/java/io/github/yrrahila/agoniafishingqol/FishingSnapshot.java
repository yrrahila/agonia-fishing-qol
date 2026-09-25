package io.github.yrrahila.agoniafishingqol;

public record FishingSnapshot(
    BobberStatus status,
    String estimatedBite,
    String elapsedTime,
    int rodDurability,
    int rodMaxDurability,
    String biteWindowEstimate,
    boolean biteReady
) {
    public static final FishingSnapshot NOT_CAST = new FishingSnapshot(
        BobberStatus.NOT_CAST,
        "--",
        "--",
        -1,
        -1,
        "",
        false
    );

    public static FishingSnapshot notCast(int rodDurability, int rodMaxDurability) {
        return new FishingSnapshot(BobberStatus.NOT_CAST, "--", "--", rodDurability, rodMaxDurability, "", false);
    }

    public enum BobberStatus {
        NOT_CAST,
        WAITING,
        FISH_APPROACHING,
        BITE_READY
    }
}
