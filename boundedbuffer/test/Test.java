import java.util.ArrayList;

public class Test {

    public static void main(String[] args) {
        BoundedBuffer<Integer> b = new BoundedBuffer<Integer>(3);

        ArrayList<Thread> threadsC = new ArrayList<>();
        int ic = 0;
        int ip = 0;
        ArrayList<Thread> threadsP = new ArrayList<>();

        //Add 10 consumers and producers
        for(int i=0;i<10;i++){
            threadsC.add(new Thread(new Consumer(b)));
            threadsP.add(new Thread(new Producer(b)));
        }
        try{
            //Start 3 consumers (Buffer is empty all consumers block)
            for(ic=0;ic<3;ic++) threadsC.get(ic).start();
            Thread.sleep(1000);

            //Start 1 producer (Should signal one consumer to wake up)
            threadsP.get(0).start();
            Thread.sleep(1000);
            System.out.println("Checkpoint1");

            //Start 9 producers (Should signal remaining 2 consumers, fill buffer and keep 4 producers waiting)
            for(ip=1;ip<10;ip++) threadsP.get(ip).start();
            Thread.sleep(2000);
            System.out.println("Checkpoint2");
            
            //Start remaining 7 producers (Should empty buffer and signal remaining 4 producers)
            for(ic=3;ic<10;ic++) threadsC.get(ic).start();
            
            for(int i=0;i<10;i++) {
                threadsC.get(i).join();
                threadsP.get(i).join();
            }
            
            threadsC.clear();
            threadsP.clear();

        }
        catch(Exception e){
            System.out.println(e);
            threadsC.clear();
            threadsP.clear();
        }
    }
}

/*
 *      Thread t1 = new Thread(new Consumer(b));
        Thread t2 = new Thread(new Producer(b));
        Thread t3 = new Thread(new Consumer(b));
        Thread t4 = new Thread(new Producer(b));

        t2.start();
        t1.start();
        t4.start();
        t3.start();


 */