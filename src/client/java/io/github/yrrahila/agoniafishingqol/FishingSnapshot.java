package io.github.yrrahila.agoniafishingqol;

public record FishingSnapshot(
    BobberStatus status,
    String estimatedBite,
    boolean biteReady
) {
    public static final FishingSnapshot NOT_CAST = new FishingSnapshot(
        BobberStatus.NOT_CAST,
        "",
        false
    );

    public enum BobberStatus {
        NOT_CAST("Not Cast"),
        WAITING("Waiting"),
        FISH_APPROACHING("Fish Approaching"),
        BITE_READY("Bite Ready");

        private final String label;

        BobberStatus(String label) {
            this.label = label;
        }

        public String label() {
            return this.label;
        }
    }
}
