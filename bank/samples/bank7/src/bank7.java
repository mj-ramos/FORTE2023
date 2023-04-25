import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// ConcTest []

class Bank {

    private static class Account {
        private int balance;
        private Lock l = new ReentrantLock();
        Account(int balance) { this.balance = balance; }
        int balance() { return balance; }
        boolean deposit(int value) {
            balance += value;
            return true;
        }
        boolean withdraw(int value) {
            if (value > balance)
                return false;
            balance -= value;
            return true;
        }
    }

    private Map<Integer, Account> map = new HashMap<Integer, Account>();
    private int nextId = 0;
    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private Lock readLock = readWriteLock.readLock();
    private Lock writeLock = readWriteLock.writeLock();

    // create account and return account id
    public int createAccount(int balance) {
        Account c = new Account(balance);
        writeLock.lock();
        try {
            int id = nextId;
            nextId += 1;
            map.put(id, c);
            return id;
        }finally {
            writeLock.unlock();
        }
    }

    // close account and return balance, or 0 if no such account
    public int closeAccount(int id) {
        Account c;
        writeLock.lock();
        try {
             c = map.remove(id);
            if (c == null)
                return 0;
            c.l.lock();
        }finally {
            writeLock.unlock();
        }
        try {
            return c.balance();
        }finally {
            c.l.unlock();
        }
    }

    // account balance; 0 if no such account
    public int balance(int id) {
        Account c;
        readLock.lock();
        try {
            c = map.get(id);
            if (c == null)
                return 0;
            c.l.lock();
        }finally {
            readLock.unlock();
        }
        try {
            return c.balance();
        }finally {
            c.l.unlock();
        }
    }

    // deposit; fails if no such account
    public boolean deposit(int id, int value) {
        Account c;
        readLock.lock();
        try {
            c = map.get(id);
            if (c == null)
                return false;
            c.l.lock();
        }finally {
            readLock.unlock();
        }
        try {
            return c.deposit(value);
        }finally {
            c.l.unlock();
        }
    }

    // withdraw; fails if no such account or insufficient balance
    public boolean withdraw(int id, int value) {
        Account c;
        readLock.lock();
        try {
            c = map.get(id);
            if (c == null)
                return false;
            c.l.lock();
        }finally {
            readLock.unlock();
        }
        try {
            return c.withdraw(value);
        }finally {
            c.l.unlock();
        }
    }

    // transfer value between accounts;
    // fails if either account does not exist or insufficient balance
    public boolean transfer(int from, int to, int value) {
        Account cfrom, cto;
        readLock.lock();
        try {
            cfrom = map.get(from);
            cto = map.get(to);
            if (cfrom == null || cto ==  null)
                return false;
            if(from<to){
                cfrom.l.lock();
                cto.l.lock();
            }else{
                cto.l.lock();
                cfrom.l.lock();
            }
       }finally {
            readLock.unlock();
        }
        try{
            try {
                if(!cfrom.withdraw(value))
                    return false;
            }finally {
                cfrom.l.unlock();
            }
            return cto.deposit(value);
        }finally {
            cto.l.unlock();
        }
    }

    // sum of balances in set of accounts; 0 if some does not exist
    public int totalBalance(int[] ids) {
        int total = 0;
        ids = ids.clone();
        Arrays.sort(ids);
        Account[] acs = new Account[ids.length];
        readLock.lock();
        try{
            for (int i : ids) {
                acs[i] = map.get(ids[i]);
               if (acs[i] == null)
                    return 0;
            }
            for(Account ac : acs){
                ac.l.lock();
            }
        }finally {
            readLock.unlock();
        }
        for(Account c : acs){
            total += c.balance;
            c.l.unlock();
        }
        return total;
    }

}
