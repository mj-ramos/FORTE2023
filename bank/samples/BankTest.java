import java.io.*;
import java.util.*;

class BankTest {
    Bank bank = new Bank();

    //Testing 2 bank transfers between the same accounts. In some students implementations it leads to deadlock, since the locks are not acquired in order.
    public void transferTest() {
        int id1 = bank.createAccount(15); 
        int id2 = bank.createAccount(15);

        Thread t1 = new Thread(() -> { bank.transfer(id1,id2,5); });
        Thread t2 = new Thread(() -> { bank.transfer(id2,id1,10); });
        t1.start();
        t2.start();
        try {
            t1.join();
            t2.join();
        } catch(InterruptedException e) {e.printStackTrace();}
        bank.closeAccount(id1); bank.closeAccount(id2);
    }

    //Can lead to deadlock if the accounts' locks are not acquired in order.
    public void totalBalanceTest() {
        int ac = 3;
        int [] ids1 = new int[ac];
        int [] ids2 = new int[ac];

        for (int i=0; i<ac; i++) {
            int id = bank.createAccount(1);
            ids1[i] = id;
            ids2[ac-i-1] = id;
        }
                
        Thread t1 = new Thread(() -> { bank.totalBalance(ids1); });
        Thread t2 = new Thread(() -> { bank.totalBalance(ids2); });
        t1.start();
        t2.start();
        
        try {
            t1.join();
            t2.join();
        } catch(InterruptedException e) {e.printStackTrace();}

        //for (int i : ids1) {
          //  bank.closeAccount(i);
        //}

    }
    
    public void closeAccounTest() {
        int id = bank.createAccount(5); 
        Thread t1 = new Thread(() -> { bank.closeAccount(id); });
        Thread t2 = new Thread(() -> { bank.deposit(id,10); });
        t2.start();
        t1.start();
        try {
            t1.join();
            t2.join();
        } catch(InterruptedException e) {e.printStackTrace();}
    }

    public void depositBalanceWithdrawTest() {
    	int id = bank.createAccount(15);
        Thread t1 = new Thread(() -> { bank.deposit(id,10); });
        Thread t2 = new Thread(() -> { bank.balance(id); });
        Thread t3 = new Thread(() -> { bank.withdraw(id,10); });
        
        t1.start();
        t3.start();
        t2.start();
        
        try {
            t1.join();
            t2.join();
            t3.join();
        } catch(InterruptedException e) {e.printStackTrace();}
        bank.closeAccount(id);
    }

    public void createAccountTest() {
        Thread t1 = new Thread(() -> { bank.createAccount(15); });
        Thread t2 = new Thread(() -> { bank.createAccount(15); });
        
        t1.start();
        t2.start();
        
        try {
            t1.join();
            t2.join();
        } catch(InterruptedException e) {e.printStackTrace();}
    }

    public void totalBalanceDepositTest() {
        int id1 = bank.createAccount(15);
        int id2 = bank.createAccount(15);

        int [] ids = {id1,id2};

        Thread t1 = new Thread(() -> { bank.totalBalance(ids); });
        Thread t2 = new Thread(() -> { bank.deposit(id1, 10); });
        
        t1.start();
        t2.start();
        
        try {
            t1.join();
            t2.join();
        } catch(InterruptedException e) {e.printStackTrace();}
    }
    
    public static void main (String[] args){
        BankTest banktest = new BankTest();

        banktest.depositBalanceWithdrawTest();
        banktest.transferTest();
        banktest.totalBalanceTest();
        banktest.closeAccounTest();
        banktest.createAccountTest();
        banktest.totalBalanceDepositTest();
    }
}
