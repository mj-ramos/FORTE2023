import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// ConcTest [dr, sem, dl]

class BoundedBuffer<T> {
    private final int max;
    private List<T> queue = new ArrayList<>();
    private Lock lock = new ReentrantLock();
    
    private Condition notFull = lock.newCondition(), notEmpty = lock.newCondition();

    public BoundedBuffer(int size) {
        this.max = size;
    }

    public T pop() throws InterruptedException {
        try {
            lock.lock();
            // race if there are multiple consumers
            if (queue.isEmpty())
                notEmpty.await();
            notFull.signalAll();
            return queue.remove(0);
        } finally {
            lock.unlock();
        }
    }

    public void push(T value) throws InterruptedException {
        lock.lock();
        // race if there are multiple producers
        if (queue.size()>=max)
            notFull.await();
        notEmpty.signalAll();
        lock.unlock();
        queue.add(value);
        // race if add is done after unlock
    }
}
