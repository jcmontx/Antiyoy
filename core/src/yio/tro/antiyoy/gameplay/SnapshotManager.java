package yio.tro.antiyoy.gameplay;

import java.util.ArrayList;

public class SnapshotManager {

    public static final int FREE_SNAPSHOTS_LIMIT = 50;
    public static final int MAX_SNAPSHOTS_IN_ONE_TURN = 30;

    GameController gameController;
    private ArrayList<LevelSnapshot> levelSnapshots, freeSnapshots;


    public SnapshotManager(GameController gameController) {
        this.gameController = gameController;

        levelSnapshots = new ArrayList<LevelSnapshot>();
        freeSnapshots = new ArrayList<>();
    }


    public void clear() {
        levelSnapshots.clear();
        freeSnapshots.clear();
    }


    public void onTurnStart() {
        if (freeSnapshots.size() < FREE_SNAPSHOTS_LIMIT) {
            for (LevelSnapshot levelSnapshot : levelSnapshots) {
                addFreeSnapshot(levelSnapshot);
            }
        }

        for (LevelSnapshot freeSnapshot : freeSnapshots) {
            freeSnapshot.reset();
        }

        levelSnapshots.clear();
    }


    private void addFreeSnapshot(LevelSnapshot levelSnapshot) {
        if (freeSnapshots.contains(levelSnapshot)) return;
        if (freeSnapshots.size() >= FREE_SNAPSHOTS_LIMIT) return;

        levelSnapshot.reset();
        freeSnapshots.add(levelSnapshot);
    }


    public void takeSnapshot() {
        if (!gameController.isPlayerTurn()) return;
        if (levelSnapshots.size() > MAX_SNAPSHOTS_IN_ONE_TURN) return; // to avoid out of memory crashes

        LevelSnapshot snapshot = getFreeSnapshot();
        snapshot.take();

        levelSnapshots.add(snapshot);
    }


    private LevelSnapshot getFreeSnapshot() {
        for (LevelSnapshot levelSnapshot : freeSnapshots) {
            if (levelSnapshot.used) continue;

            levelSnapshot.reset();
            return levelSnapshot;
        }

        return new LevelSnapshot(gameController);
    }


    public boolean undoAction() {
        int lastIndex = levelSnapshots.size() - 1;
        if (lastIndex < 0) return false;

        LevelSnapshot lastSnapshot = levelSnapshots.get(lastIndex);
        lastSnapshot.recreate();

        levelSnapshots.remove(lastSnapshot);
        addFreeSnapshot(lastSnapshot);

        return true;
    }


    public void showInConsole() {
        System.out.println();
        System.out.println("SnapshotManager.showInConsole:");
        System.out.println("used: " + levelSnapshots.size());
        System.out.println("free: " + freeSnapshots.size());
    }
}
