//Prototype implementation of Car Control
//Mandatory assignment
//Course 02158 Concurrent Programming, DTU, Fall 2017

//Hans Henrik Lovengreen     Oct 9, 2017


import com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;
import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;




class Gate {
    
    Semaphore g = new Semaphore(0);
    Semaphore e = new Semaphore(1);
    boolean isopen = false;

    public void pass() throws InterruptedException {
        g.P(); 
        g.V();
    }

    public void open() {
        try { e.P(); } catch (InterruptedException e) {}
        if (!isopen) { g.V();  isopen = true; }
        e.V();
    }

    public void close() {
        try { e.P(); } catch (InterruptedException e) {}
        if (isopen) { 
            try { g.P(); } catch (InterruptedException e) {}
            isopen = false;
        }
        e.V();
    }

}

class Car extends Thread {

    int basespeed = 100;             // Rather: degree of slowness
    int variation =  50;             // Percentage of base speed

    CarDisplayI cd;                  // GUI part

    int no;                          // Car number
    Pos startpos;                    // Startpositon (provided by GUI)
    Pos barpos;                      // Barrierpositon (provided by GUI)
    Color col;                       // Car  color
    Gate mygate;                     // Gate at startposition


    int speed;                       // Current car speed
    Pos curpos;                      // Current position 
    Pos newpos;                      // New position to go to
    
    Semaphore [][] smatrix;
    Semaphore [][] Nsmatrix;
    Semaphore mutex;
    
    Alley alley;

    public Car(int no, CarDisplayI cd, Gate g) {

        this.no = no;
        this.cd = cd;
        mygate = g;
        startpos = cd.getStartPos(no);
        barpos = cd.getBarrierPos(no);  // For later use

        col = chooseColor();

        // do not change the special settings for car no. 0
        if (no==0) {
            basespeed = 0;  
            variation = 0; 
            setPriority(Thread.MAX_PRIORITY); 
        }
    }

    public synchronized void setSpeed(int speed) { 
        if (no != 0 && speed >= 0) {
            basespeed = speed;
        }
        else
            cd.println("Illegal speed settings");
    }

    public synchronized void setVariation(int var) { 
        if (no != 0 && 0 <= var && var <= 100) {
            variation = var;
        }
        else
            cd.println("Illegal variation settings");
    }

    synchronized int chooseSpeed() { 
        double factor = (1.0D+(Math.random()-0.5D)*2*variation/100);
        return (int) Math.round(factor*basespeed);
    }

    private int speed() {
        // Slow down if requested
        final int slowfactor = 3;  
        return speed * (cd.isSlow(curpos)? slowfactor : 1);
    }

    Color chooseColor() { 
        return Color.blue; // You can get any color, as longs as it's blue 
    }

    Pos nextPos(Pos pos) {
        // Get my track from display
        return cd.nextPos(no,pos);
    }

    boolean atGate(Pos pos) {
        return pos.equals(startpos);
    }

   public void run() {
       
       //Create a matrix of semaphore
       
        try {

            speed = chooseSpeed();
            curpos = startpos;
            cd.mark(curpos,col,no);

            while (true) { 
                sleep(speed());
  
                if (atGate(curpos)) { 
                    mygate.pass(); 
                    speed = chooseSpeed();
                }
                
                newpos = nextPos(curpos);
                
                smatrix[newpos.row][newpos.col].P();
                //Check for alley enter
                if (alley.isNextAlleyEntry(no ,newpos)) {
                    mutex.P();
                    alley.enter(no);
                    mutex.V();
                }
                //Check for alley leave
                if (alley.isNextAlleyExit(no, newpos)) {
                    mutex.P();
                    alley.leave(no);
                    mutex.V();
                }
               
                //  Move to new position 
                cd.clear(curpos);
                cd.mark(curpos,newpos,col,no);
                sleep(speed());
                cd.clear(curpos,newpos);
                cd.mark(newpos,col,no);
                
                smatrix[curpos.row][curpos.col].V();
                curpos = newpos;
            }

        } catch (Exception e) {
            cd.println("Exception in Car no. " + no);
            System.err.println("Exception in Car no. " + no + ":" + e);
            e.printStackTrace();
        }
    }

}

class Alley{

    
    /*Pos a01 = new Pos(0, 1); Pos a11 = new Pos(1, 1);
    Pos a02 = new Pos(0, 2); Pos a12 = new Pos(1, 2);
    Pos a03 = new Pos(0, 3);
    Pos a04 = new Pos(0, 4);
    Pos a05 = new Pos(0, 5);
    Pos a06 = new Pos(0, 6);
    Pos a07 = new Pos(0, 7);
    Pos a08 = new Pos(0, 8);
    Pos a09 = new Pos(0, 9);
    Pos a010 = new Pos(0, 10); Pos a110 = new Pos(1, 10);
    Pos a011 = new Pos(0, 11); Pos a111 = new Pos(1, 11);
    */
    
   /* Pos a11 = new Pos(1, 1);
    Pos a12 = new Pos(1, 2);
    Pos a110 = new Pos(1, 10);
    Pos a111 = new Pos(1, 11);
    
    Pos a21 = new Pos(2, 1);
    Pos a22 = new Pos(2, 2);
    Pos a210 = new Pos(2, 10);
    Pos a211 = new Pos(2, 11);
    
    Pos a31 = new Pos(3, 1);
    Pos a32 = new Pos(3, 2);
    Pos a310 = new Pos(3, 10);
    Pos a311 = new Pos(3, 11);*/
    
    Pos a23 = new Pos(1, 3);
    Pos a33 = new Pos(2, 3);
    Pos a100 = new Pos(10, 0);
    
    Semaphore [][] smatrix;
    Pos alleyTopPos[] = {a23, a33};
    Pos alleyButtonPos[] = {a100};
    int one2four = 0;
    int five2eight = 0;
            
    
    Semaphore mutex = new Semaphore(1);
   public void enter(int i) throws InterruptedException{
       //mutex.P();
       if (i == 1|| i == 2 || i == 3|| i == 4 ) {
           if (one2four == 0) {
               //block
               smatrix[10][0].P();
               Util.println("Block Alley" + String.valueOf(i));
           }
           one2four += 1;
       } else {
           if (five2eight == 0) {
               //block
               smatrix[1][3].P();
               smatrix[2][3].P();
               Util.println("Block Alley" + String.valueOf(i));
           }
           five2eight += 1;
       }
       //mutex.V();
   } 

   public void leave(int i) throws InterruptedException{
       //mutex.P();
       if (i == 1|| i == 2 || i == 3|| i == 4 ) {
           one2four -= 1;
           if (one2four == 0) {
               //unblock
               smatrix[10][0].V();
           }
       } else {
           five2eight -= 1;
           if (five2eight == 0) {
               //unblock
               smatrix[1][3].V();
               smatrix[2][3].V();
           }
       }
       //mutex.V();
   }
   
   public boolean isNextAlleyEntry(int i,Pos nextPos){
       if (i == 1|| i == 2 || i == 3|| i == 4 ) {
           for (Pos alleyPo : alleyTopPos) {
            if (nextPos.equals(alleyPo)) {
                Util.println("Entro al alley" + String.valueOf(i));
                return true;
            }
        }
       } else {
           for (Pos alleyPo : alleyButtonPos) {
            if (nextPos.equals(alleyPo)) {
                Util.println("Entro al alley" + String.valueOf(i));
                return true;
            }
        }
           
        }
       return false;
   }
   
   public boolean isAlleyEmpty(int i,Pos nextPos){
       if (i == 1|| i == 2 || i == 3|| i == 4 ) {
           for (Pos alleyPo : alleyButtonPos) {
            if (nextPos.equals(alleyPo)) {
                Util.println("Salio al alley" + String.valueOf(i));
                return true;
            }
        }
       } else {
           for (Pos alleyPo : alleyTopPos) {
            if (nextPos.equals(alleyPo)) {
                Util.println("Salio al alley" + String.valueOf(i));
                return true;
            }
        }
           
        }
       return false;
   }

}

public class CarControl implements CarControlI{

    CarDisplayI cd;           // Reference to GUI
    Car[]  car;               // Cars
    Gate[] gate;              // Gates
    
    Semaphore [][] smatrix = new Semaphore[11][12];
    Semaphore [][] Nsmatrix = new Semaphore[11][12];
    Alley alley = new Alley();
    Semaphore mutex = new Semaphore(1);
       
    public CarControl(CarDisplayI cd) {
        this.cd = cd;
        car  = new  Car[9];
        gate = new Gate[9];

        for (int i = 0; i <= 10; i++) {
            for (int j = 0; j <= 11; j++) {
                smatrix [i][j] = new Semaphore(1);
                Nsmatrix [i][j] = new Semaphore(1);
            }
        }
        
        alley.smatrix = smatrix;
        
        for (int no = 0; no < 9; no++) {
            gate[no] = new Gate();
            
            car[no] = new Car(no,cd,gate[no]);
            car[no].Nsmatrix = this.Nsmatrix;
            car[no].smatrix = this.smatrix;
            car[no].alley = this.alley;
            car[no].mutex = mutex;
            car[no].start();
        } 
        
    }

   public void startCar(int no) {
        gate[no].open();
    }

    public void stopCar(int no) {
        gate[no].close();
    }

    public void barrierOn() { 
        cd.println("Barrier On not implemented in this version");
    }

    public void barrierOff() { 
        cd.println("Barrier Off not implemented in this version");
    }

    public void barrierShutDown() { 
        cd.println("Barrier shut down not implemented in this version");
        // This sleep is for illustrating how blocking affects the GUI
        // Remove when shutdown is implemented.
        try { Thread.sleep(3000); } catch (InterruptedException e) { }
        // Recommendation: 
        //   If not implemented call barrier.off() instead to make graphics consistent
    }

    public void setLimit(int k) { 
        cd.println("Setting of bridge limit not implemented in this version");
    }

    public void removeCar(int no) { 
        cd.println("Remove Car not implemented in this version");
    }

    public void restoreCar(int no) { 
        cd.println("Restore Car not implemented in this version");
    }

    /* Speed settings for testing purposes */

    public void setSpeed(int no, int speed) { 
        car[no].setSpeed(speed);
    }

    public void setVariation(int no, int var) { 
        car[no].setVariation(var);
    }

}






