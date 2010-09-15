package sisop;

import java.util.LinkedList;
import java.util.TreeMap;
import java.util.Map;
import java.util.List;

/**
 * Implementacion de la catedra de un
 * planificador FCFS (FIFO)
 */ 
class FCFS extends Scheduler {

  private LinkedList<Task> order;
  private int current_time;
  private LinkedList<String> ready_tasks;

  /**
   * Construye un nuevo planificador FCFS (FIFO)
   */ 
  FCFS() {
    super("FCFS");
  }
  
  protected void scheduler_init() {

    int max_rtime = task_set.get_max_rtime();
    
    this.order = new LinkedList<Task>();
    for (int t=0; t<=max_rtime; t++) {
      List<String> incoming_tasks = task_set.get_released_tasks_at(t);
      for (String task_name: incoming_tasks) 
         this.order.add(task_set.get(task_name));
    }

    ready_tasks = new LinkedList<String>();
    current_time = -1;
  }


  public String scheduler_next() {

    if (order.isEmpty())
      return null;

    current_time++;

    Task next_task = order.getFirst();

    if ( next_task.rtime > current_time ) {
      return IDLE_TASK;
    }

    if ( next_task.ttime== next_task.ptime ) {

      next_task.ftime = current_time;

      order.removeFirst();
      if (order.isEmpty())
        return null;
  
      next_task = order.getFirst();      
      if ( next_task.rtime > current_time ) {
        return IDLE_TASK; 
      }
      
      /** -ejercicio 2: calcular el waiting time- **/
      next_task.wtime = current_time - next_task.rtime;
    }

    next_task.ttime++;
    return next_task.name;
  }
  
} 
