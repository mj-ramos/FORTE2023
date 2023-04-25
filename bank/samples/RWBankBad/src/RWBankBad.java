import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// ConcTest [dr,dl,sem]

/*
 * Bank with a method that mutates the accounts map after acquiring a ReadLock
 * Accesses to accounts are not synchronized
 */

class Bank {
    private Map<Integer, Account> accounts;
    private ReentrantReadWriteLock lock;
    private int nextId;

    public class Account {
        int balance;
        Lock lock;

        public Account(int balance) {
            this.balance = balance;
            this.lock = new ReentrantLock();
        }

        public int balance() {
            return this.balance;
        }

        public void deposit(int quant) {
            balance += quant;
        }
        
        public boolean withdraw(int quant) {
            if (quant > balance)
                return false;
            balance -= quant;
            return true;
        }

        public void lock() {
            this.lock.lock();
        }

        public void unlock() {
            this.lock.unlock();
        }
    }

    public Bank() {
        this.lock = new ReentrantReadWriteLock();
        this.accounts = new HashMap<>();
    }

    public int balance(int acc) {
        this.lock.readLock().lock();
        try {
            return this.accounts.get(acc).balance();
        }
        finally {
            this.lock.readLock().unlock();
        }
    }

    public void deposit(int acc, int quant) {
        this.lock.readLock().lock();
        try {
            this.accounts.get(acc).deposit(quant);
        }
        finally {
            this.lock.readLock().unlock();
        }
    }

    public boolean withdraw(int acc, int quant) {
        this.lock.readLock().lock();
        try {
            return this.accounts.get(acc).withdraw(quant);
        }
        finally {
            this.lock.readLock().unlock();
        }
    }

    public boolean transfer(int from, int to, int quant) {
        this.lock.readLock().lock();
        try {
            if (withdraw(from, quant)) {
                deposit(to, quant);
                return true;
            }
            return false;
        }
        finally {
            this.lock.readLock().lock();
        }
    } 

    public int createAccount(int balance) {
        this.lock.readLock().lock(); // acquires wrong lock type
        try {
            int id = this.nextId;
            this.nextId += 1;
            this.accounts.put(id, new Account(balance));
            return id;
        }
        finally {
            this.lock.readLock().unlock();
        }
    }

    public void closeAccount(int acc) {
        this.lock.writeLock().lock();
        try {
            this.accounts.remove(acc);
        }
        finally {
            this.lock.writeLock().unlock();
        }
    }

    public int totalBalance(int[] ids) {
        int total = 0; Account c;
        this.lock.readLock().lock();
        try{
            for (int i : ids) {
                c = accounts.get(i);
                if (c == null) return 0;
                total += c.balance();
            }
            return total;
        } finally {
            this.lock.readLock().unlock();
        }
    }
}
