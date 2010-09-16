package sisop;

import java.util.List;
import java.util.LinkedList;
import java.io.*;

/**
 * Implementacion de un scheduler 
 * Round Robin. Esta clase esta vacia
 */ 
class RR extends Scheduler {
  
  private int task_switching;
  private LinkedList<String> finished_tasks;
  private LinkedList<String> ready_tasks;
  private int current_quantum;
  private int current_time;
  private Task current_task;
  private int quantum = DEFAULT_QUANTUM;

  /**
   * Valor del quantum por defecto.
  */
  public static final int DEFAULT_QUANTUM = 5;

  /**
  * Construye un planificador Round Robin
  */ 
  RR() {
    super("RR");
  }

  /**
   * Permite indicar el valor del quantum
   * deseado para la planificacion
   */
  public void set_quantum(int q) {
    this.quantum = q;
  }

  protected void scheduler_init() {
    //throw new UnsupportedOperationException("Implementar este metodo");
    if (this.task_set.get_max_rtime()<0)
      throw new IllegalStateException("Cannot schedule a task_set with release time<0");

    finished_tasks = new LinkedList<String>();
    ready_tasks = new LinkedList<String>();
    current_task = null;
    current_time = -1;
    current_quantum = 0;
  }


  public String scheduler_next() {
    //throw new UnsupportedOperationException("Implementar este metodo");
    current_time++;
    
	if(task_switching > 0){
		task_switching--;
		return IDLE_TASK;
	}
	
    if (task_set.size()==finished_tasks.size())      
      return null;

		

    List<String> newly_released_tasks = task_set.get_released_tasks_at( current_time );
    
    // Si llegó un nuevo proceso lo agrega
    if (!newly_released_tasks.isEmpty()) {
      ready_tasks.addAll(newly_released_tasks);
    }

    if (current_task==null) {
      if (ready_tasks.isEmpty()) {
        return IDLE_TASK;
      } else {
        current_task = task_set.get( ready_tasks.getFirst() );
        ready_tasks.remove( current_task.name );
        /** -ejercicio 2: calcular el waiting time- **/
        current_task.wtime = current_time - current_task.rtime;
      }
    } else {
       System.out.println( "Q:" + current_quantum );
       System.out.println( "P:" + current_task.name );
       current_quantum++;
       if ( current_task.ttime == current_task.ptime || current_quantum==quantum ) {
         if( current_task.ttime == current_task.ptime )
         {
           current_task.ftime = current_time;
           finished_tasks.addLast(current_task.name );
         }
         else
         {
           // Se pasó de quantum
           //current_time   += 2;
           task_switching = 2;
           current_quantum = 0;
           ready_tasks.addLast( current_task.name );
         }
         
         if (ready_tasks.isEmpty()) {
           current_task = null;
           if (task_set.size()==finished_tasks.size()) {
             return null;
           } else {
             return IDLE_TASK;
           }
         } else {
		   current_task = task_set.get( ready_tasks.getFirst() );
		   ready_tasks.remove( current_task.name );
		   /** -ejercicio 2: calcular el waiting time- **/
		   current_task.wtime = current_time - current_task.rtime;
         }
       }
    }
    
    current_task.ttime++;
    return current_task.name;
  
  }

}
