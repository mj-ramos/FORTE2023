import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

// ConcTest []

class Bank {

    private static class Account {
        private int balance;
        private ReentrantLock lock;

        Account(int balance) {
            this.balance = balance;
            this.lock = new ReentrantLock();
        }


        int balance() {
            return balance;
        }
        /*int balance() {
            this.lock.lock();
            try {
                return balance;
            } finally {
                this.lock.unlock();
            }
        }*/

        boolean deposit(int value) {
            this.balance += value;
            return true;
            /*this.lock.lock();
            try {
                this.balance += value;
                return true;
            } finally {
                this.lock.unlock();
            }*/
        }

        boolean withdraw(int value) {
            if (value > this.balance)
                return false;
            this.balance -= value;
            return true;
            /*this.lock.lock();
            try {
                if (value > this.balance)
                    return false;
                this.balance -= value;
                return true;
            } finally {
                this.lock.unlock();
            }*/
        }
    }

    private Map<Integer, Account> map = new HashMap<Integer, Account>();
    private int nextId = 0;
    private ReentrantLock lockBanco = new ReentrantLock();

    public Bank(){
        this.map = new HashMap<>();
        this.nextId = 0;
    }

    // create account and return account id
    public int createAccount(int balance) {
        Account c = new Account(balance);
        this.lockBanco.lock();
        int id;
        try {
            id = nextId;
            this.nextId += 1;
            this.map.put(id, c);
        } finally {
            this.lockBanco.unlock();
        }
        return id;
    }

    // close account and return balance, or 0 if no such account
    public int closeAccount(int id) {
        this.lockBanco.lock();
        Account c;
        try {
            c = this.map.remove(id);
            if (c == null)
                return 0;
            c.lock.lock();
        } finally {
            this.lockBanco.unlock();
        }
        try {
            return c.balance();
        } finally {
            c.lock.unlock();
        }
    }

    // account balance; 0 if no such account
    public int balance(int id) {
        this.lockBanco.lock();
        Account c;
        try {
            c = this.map.get(id);

            if (c == null)
                return 0;

            c.lock.lock();
        } finally {
            this.lockBanco.unlock();
        }

        try {
            return c.balance();
        } finally {
            c.lock.unlock();
        }
    }

    // deposit; fails if no such account
    public boolean deposit(int id, int value) {
        this.lockBanco.lock();
        Account c;
        try {
            c = this.map.get(id);

            if (c == null)
                return false;

            c.lock.lock();
        } finally {
            this.lockBanco.unlock();
        }
        try {
            return c.deposit(value);
        } finally {
            c.lock.unlock();
        }
    }

    // withdraw; fails if no such account or insufficient balance
    public boolean withdraw(int id, int value) {
        this.lockBanco.lock();
        Account c;
        try {
            c = this.map.get(id);

            if (c == null)
                return false;

            c.lock.lock();
        } finally {
            this.lockBanco.unlock();
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
        Account cfrom, cto;
        this.lockBanco.lock();
        try {
            cfrom = this.map.get(from);
            cto = this.map.get(to);

            if (cfrom == null || cto ==  null)
                return false;

            if(from < to){
                cfrom.lock.lock();
                cto.lock.lock();
            } else{
                cto.lock.lock();
                cfrom.lock.lock();
            }
        } finally {
            this.lockBanco.unlock();
        }

        /*if (cfrom == null || cto ==  null)
            return false;

        if(from < to){
            cfrom.lock.lock();
            cto.lock.lock();
        } else{
            cto.lock.lock();
            cfrom.lock.lock();
        }*/
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
        Arrays.sort(ids);
        int i = 0;

        this.lockBanco.lock();
        Account[] contas = new Account[this.map.size()];
        try{
            for(int id : ids){
                contas[i] = this.map.get(id);
                if(contas[i] == null)
                    return 0;
                contas[i++].lock.lock();
            }
        } finally {
            this.lockBanco.unlock();
        }

        try{
            for(Account c : contas){
                total += c.balance;
            }
        } finally {
            for(Account c : contas)
                c.lock.unlock();
        }
        return total;
    }

}