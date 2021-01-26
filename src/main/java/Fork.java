public class Fork {
    public boolean taken = false;
    public int ID;

    public Fork(int ID){
        this.ID = ID;
    }

    public synchronized void take() {
        while (taken) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        taken = true;
    }

    public synchronized void put() {
        while (!taken) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        taken = false;
    }
}
