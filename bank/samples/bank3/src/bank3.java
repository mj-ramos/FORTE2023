import java.util.*;
import java.util.concurrent.locks.*;

// ConcTest [dl, dr]

class Bank {

    private static class Account {
        private int balance;
        ReentrantLock lockConta = new ReentrantLock();

        Account(int balance) {
            this.balance = balance;
        }

        int balance() {
            return balance;
        }

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
    ReentrantReadWriteLock lockBanco = new ReentrantReadWriteLock();

    // create account and return account id
    public int createAccount(int balance) {
        Account c = new Account(balance);
        int id;
        Lock writeBanco = lockBanco.writeLock();
        try {
            id = nextId;
            nextId += 1;
            map.put(id, c);
        } finally {
            writeBanco.unlock();
        }
        return id;
    }

    // close account and return balance, or 0 if no such account
    public int closeAccount(int id) {
        Account c;
        Lock writeBanco = lockBanco.writeLock();
        try {
            c = map.remove(id);
            if (c == null)
                return 0;
            c.lockConta.lock();
        } finally {
            writeBanco.unlock();
        }
        try {
            return c.balance();
        } finally {
            c.lockConta.unlock();
        }
    }

    // account balance; 0 if no such account
    public int balance(int id) {
        Account c;
        Lock readBanco = lockBanco.readLock();
        try {
            c = map.get(id);
            if (c == null)
                return 0;
            c.lockConta.lock();
        } finally {
            readBanco.unlock();
        }
        try {
            return c.balance();
        } finally {
            c.lockConta.unlock();
        }
    }

    // deposit; fails if no such account
    public boolean deposit(int id, int value) {
        Account c;
        Lock readBanco = lockBanco.readLock();
        try {
            c = map.get(id);
            if (c == null)
                return false;
            c.lockConta.lock();
        } finally {
            readBanco.unlock();
        }
        try {
            return c.deposit(value);
        } finally {
            c.lockConta.unlock();
        }
    }

    // withdraw; fails if no such account or insufficient balance
    public boolean withdraw(int id, int value) {
        Account c;
        Lock readBanco = lockBanco.readLock();
        try {
            c = map.get(id);
            if (c == null)
                return false;
            c.lockConta.lock();
        } finally {
            readBanco.unlock();
        }
        try {
            return c.withdraw(value);
        } finally {
            c.lockConta.unlock();
        }
    }

    // transfer value between accounts;
    // fails if either account does not exist or insufficient balance
    public boolean transfer(int from, int to, int value) {
        Account cfrom, cto;
        Lock readBanco = lockBanco.readLock();
        try {
            cfrom = map.get(from);
            cto = map.get(to);
            if (cfrom == null || cto == null)
                return false;
            cfrom.lockConta.lock();
            cto.lockConta.lock();
        } finally {
            readBanco.unlock();
        }
        try {
            return cfrom.withdraw(value) && cto.deposit(value);
        } finally {
            cfrom.lockConta.unlock();
            cto.lockConta.unlock();
        }
    }

    // sum of balances in set of accounts; 0 if some does not exist
    public int totalBalance(int[] ids) {
        int total = 0;
        Account[] acs = new Account[ids.length];
        Lock readBanco = lockBanco.readLock();
        try {
            for (int i : ids) {
                Account c = map.get(i);
                if (c == null)
                    return 0;
                //
                acs[i] = c;

            }
            for (Account c : acs) {
                c.lockConta.lock();
            }
        } finally {
            readBanco.unlock();
        }

        for (Account c : acs) {
            total += c.balance();
            c.lockConta.unlock();
        }
        return total;
    }

}
