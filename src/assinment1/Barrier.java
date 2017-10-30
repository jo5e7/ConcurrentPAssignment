
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author jdmaestre
 */
public class Barrier {

    Semaphore[][] smatrix;
    Semaphore[] sArrive = new Semaphore[9];
    Semaphore[] sContinue = new Semaphore[9];
    Runnable syncronization = new Runnable() {
        @Override
        public void run() {
            sync();
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    };
    Thread coordinator = new Thread(syncronization);
    
    public Barrier(Semaphore[][] smatrix) {
       this.smatrix = smatrix;
       
        for (int n=0; n<sArrive.length;n++) {
            sArrive[n] = new Semaphore(0);
            sContinue[n] = new Semaphore(0);
        }
       
    }
    
    Pos p54 = new Pos(5, 4);
    Pos p55 = new Pos(5, 5);
    Pos p56 = new Pos(5, 6);
    Pos p57 = new Pos(5, 7);
    
    Pos p68 = new Pos(6, 8);
    Pos p69 = new Pos(6, 9);
    Pos p610 = new Pos(6, 10);
    Pos p611 = new Pos(6, 11);
    
    boolean isOn = false;
    
    Pos barrierPos[] = {p54, p55, p56, p57, p68, p69, p610, p611};
    
    
    
   public void sync() { 
       // Wait for others to arrive (if barrier active)
       while (isOn) {           
           for (int n=1; n<sArrive.length;n++) {
               try {
                   sArrive[n].P();
               } catch (InterruptedException ex) {
                   //Unlock cars in barrier if it is deactivated 
                   Logger.getLogger(Barrier.class.getName()).log(Level.SEVERE, null, ex);
                   for (int n1=1; n<sContinue.length;n1++) {
                        sContinue[n1].V();
                        System.err.println("continue.V()" + String.valueOf(n1));
                    }
               }
        }
       for (int n=1; n<sContinue.length;n++) {
            sContinue[n].V();
        }
       }
   }  

   public void on() throws InterruptedException {
       // Activate barrier
       for (int n=0; n<sArrive.length;n++) {
            sArrive[n] = new Semaphore(0);
            sContinue[n] = new Semaphore(0);
        }
        isOn = true;
        coordinator = new Thread(syncronization);
        coordinator.start();
        
   }    

   public void off() throws InterruptedException { 
       // Deactivate barrier
       coordinator.interrupt();
       isOn = false;
    }
}