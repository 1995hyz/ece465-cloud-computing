public class Fork {
    private boolean taken = false;
    private final int ID;

    public Fork(int ID){
        this.ID = ID;
    }

    public boolean isTaken() {
        return taken;
    }

    public void setTaken(boolean taken) {
        this.taken = taken;
    }

    public int getID() {
        return ID;
    }

    public synchronized void take() {
//        while (taken) {
//            try {
//                wait();
//            } catch (InterruptedException e) {
//            }
//        }
        taken = true;
    }

    public synchronized void put() {
//        while (!taken) {
//            try {
//                wait();
//            } catch (InterruptedException e) {
//            }
//        }
        taken = false;
    }
}
