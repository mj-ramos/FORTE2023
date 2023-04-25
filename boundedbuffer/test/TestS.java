import java.util.ArrayList;

public class TestS {

    public static void main(String[] args) {
        BoundedBuffer<Integer> b = new BoundedBuffer<Integer>(1);

        ArrayList<Thread> threadsC = new ArrayList<>();
        int ic = 0;
        int ip = 0;
        ArrayList<Thread> threadsP = new ArrayList<>();

        //Add 5 consumers and producers
        for(int i=0;i<5;i++){
            threadsC.add(new Thread(new Consumer(b)));
            threadsP.add(new Thread(new Producer(b)));
        }
        try{
            //Start 2 consumers (Buffer is empty all consumers block)
            for(ic=0;ic<2;ic++) threadsC.get(ic).start();
            Thread.sleep(1000);

            //Start 1 producer (Should signal one consumer to wake up)
            threadsP.get(0).start();
            Thread.sleep(1000);
            System.out.println("Checkpoint1");

            //Start 4 producers (Should signal remaining 2 consumers, fill buffer and keep 4 producers waiting)
            for(ip=1;ip<5;ip++) threadsP.get(ip).start();
            Thread.sleep(2000);
            System.out.println("Checkpoint2");
            
            //Start remaining 3 consumers (Should empty buffer and signal remaining 2 producers)
            for(ic=2;ic<5;ic++) threadsC.get(ic).start();
            
            for(int i=0;i<5;i++) {
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