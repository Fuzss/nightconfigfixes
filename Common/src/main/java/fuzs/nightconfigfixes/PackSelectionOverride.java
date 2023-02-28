package fuzs.nightconfigfixes;

public record PackSelectionOverride(boolean forceCompatible, boolean fixedPosition, boolean required, boolean hidden) {

    public static final PackSelectionOverride EMPTY = new PackSelectionOverride(false, false, false, false);
}
