package multi_node_multi_thread.model;

public class ManagerResponse {
    private boolean isGridExplored;
    private boolean shouldContinue;

    public ManagerResponse(boolean isGridExplored, boolean shouldContinue) {
        this.isGridExplored = isGridExplored;
        this.shouldContinue = shouldContinue;
    }

    public boolean isGridExplored() {
        return isGridExplored;
    }

    public boolean isShouldContinue() {
        return shouldContinue;
    }
}
