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
            lock.lock();
            if (queue.isEmpty())
            // race if there are multiple consumers
                notEmpty.await();
            notFull.signalAll();
            lock.unlock();
        return queue.remove(0);
        // race if return is done after unlock (common without try/finally)
    }

    public void push(T value) throws InterruptedException {
        try {
            lock.lock();
            if (queue.size()>=max)
            // race if there are multiple producers
                notFull.await();
            notEmpty.signalAll();
            queue.add(value);
        } finally {
            lock.unlock();
        }
    }
}
