import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// ConcTest [dl]

class Bank {

    //class Bank {

        private static class Account1 {
            private int balance;
            Lock lock = new ReentrantLock();
            Account1(int balance) { this.balance = balance; }
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

        private ReentrantReadWriteLock lockBanco=new ReentrantReadWriteLock();
        private Map<Integer, Account1> map = new HashMap<Integer, Account1>();
        private int nextId = 0;
        private Lock rlock = lockBanco.readLock();
        private Lock wLock = lockBanco.writeLock();

        // create account and return account id
        public int createAccount(int balance) {
            Account1 c = new Account1(balance);
            int id;
            wLock.lock();
            try{
                id = nextId;
                nextId += 1;
                map.put(id, c);
            } finally {
                wLock.unlock();
            }
            return id;
        }

        // close account and return balance, or 0 if no such account
        public int closeAccount(int id) {
            Account1 c;
            wLock.lock();
            try{
                c = map.remove(id);
                if (c == null)
                    return 0;
                c.lock.lock();
            } finally {
                wLock.unlock();

            }
            try {
                return c.balance();

            } finally {
                c.lock.unlock();
            }
        }

        // account balance; 0 if no such account
        public int balance(int id) {
            Account1 c;
            rlock.lock();
            try{
                c = map.get(id);
                if (c == null)
                    return 0;

                c.lock.lock();
            } finally {
                rlock.unlock();
            }
            try{
                return c.balance();
            } finally {
                c.lock.unlock();
            }
        }

        // deposit; fails if no such account
        public boolean deposit(int id, int value) {
            Account1 c;
            rlock.lock();
            try {
                c = map.get(id);
                if (c == null)
                    return false;
                c.lock.lock();
            }finally {
                rlock.unlock();
            }
            try {
                return c.deposit(value);
            } finally {
                c.lock.unlock();
            }
        }

        // withdraw; fails if no such account or insufficient balance
        public boolean withdraw(int id, int value) {
            Account1 c;
            rlock.lock();
            try{
                c=map.get(id);
                if(c==null) return false;
                c.lock.lock();
            } finally {
                rlock.unlock();
            }
            try{
                return c.withdraw(value);
            } finally {
                c.lock.unlock();
            }
        }

        // transfer value between accounts;
        // fails if either account does not exist or insufficient balance
        public boolean transfer(int from, int to, int value) {
            Account1 cfrom, cto;
            rlock.lock();
            try {
                cfrom = map.get(from);
                cto = map.get(to);
                if (cfrom == null || cto == null)
                    return false;
                cfrom.lock.lock();
                cto.lock.lock();
            } finally {
                rlock.unlock();
            }
            try {
                return cfrom.withdraw(value) && cto.deposit(value);

            } finally {
                cfrom.lock.unlock();
                cto.lock.unlock();
            }
        }

        // sum of balances in set of accounts; 0 if some does not exist
        public int totalBalance(int[] ids) {
            int total = 0;
            Account1[] acs =new Account1[ids.length];
            rlock.lock();
            try {
                for (int i : ids) {
                    Account1 c = map.get(i);
                    if (c == null)
                        return 0;
                    acs[i]=c;
                }
                for(Account1 c:acs){
                    c.lock.lock();
                }
            }finally {
                rlock.unlock();
            }
            for(Account1 c:acs){
                total+=c.balance;
                c.lock.unlock();
            }
            return total;
        }

}

