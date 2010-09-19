package sisop;

import java.util.List;
import java.util.LinkedList;
import java.io.*;

/**
 * Implementacion de un scheduler 
 * Round Robin. Esta clase esta vacia
 */ 
class RR extends Scheduler {
  
  ////SI LE MANDAS UN QUANTUM DIFERENTE, USA EL DEFAULT! WTF!?
  
  private int task_switching;
  private boolean only_task = false;
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

    finished_tasks 	= new LinkedList<String>();
    ready_tasks 	= new LinkedList<String>();
    current_task = null;
    current_time = -1;
    current_quantum = 0;
  }


public String scheduler_next() {
	//Incremento tiempo actual 
    current_time++;
    
    //Chequeo: Estoy en task switching?
	if(task_switching > 0){
		task_switching--;
		return IDLE_TASK;
	}
	
	//Termine de procesar todo?
    if (task_set.size()==finished_tasks.size())      
      return null;

    // Si llegó un nuevo proceso lo agrega
    List<String> newly_released_tasks = task_set.get_released_tasks_at( current_time );
    if (!newly_released_tasks.isEmpty()) {
      ready_tasks.addAll(newly_released_tasks);
    }

	//No tengo ningun proceso corriendo?
	if (current_task==null) {
    	//Queda algo en la pila de readys?	
		if (ready_tasks.isEmpty()) {
        	return IDLE_TASK;
      	} else {	//estoy agregando el 1er task
			current_task = task_set.get( ready_tasks.getFirst() );
			ready_tasks.removeFirst();
			current_task.wtime = current_time - current_task.rtime;
      		current_task.ttime++;
			return current_task.name;
      	}
    } else { //==> Hay algún proceso Corriendo
		System.out.println( "--------------------" + current_quantum );
		System.out.println( "Current time == " + current_time );
		System.out.println( "--------------------" + current_quantum );
		System.out.println( "Q:" + current_quantum );

		System.out.println( "P:" + current_task.name );
		System.out.println( "	finished task size: " + finished_tasks.size() );
		System.out.println( "	ready task size: " + ready_tasks.size() );
       	current_quantum++;
       	if ( current_task.ttime == current_task.ptime ) { //termine o se me acabo el tiempo
       
        	//El proceso terminó
			System.out.println( "-- FINISHED task --: " + current_task.name );
			current_task.ftime = current_time;
			finished_tasks.addLast(current_task.name );
			task_switching = 2;
			current_quantum = 0;
			ready_tasks.removeFirst();
         	if (ready_tasks.isEmpty()) {
				System.out.println("NO MORE READY TASKS ''' tasksetsize=" + task_set.size() + " & finishedtasksize=" + finished_tasks.size());
           		current_task = null;
           		if (task_set.size()==finished_tasks.size())	return null;
		   		else 										return IDLE_TASK;
         	} else {
		   		current_task = task_set.get( ready_tasks.getFirst() );
		   		/** -ejercicio 2: calcular el waiting time- **/
		   		current_task.wtime = current_time - current_task.rtime;
         	}
       		current_task.ttime++;
			return current_task.name;
		
		} 
	
		if(current_quantum==quantum) {
          //Se le acabó el quantum, si no hay otro para ejecutar, lo dejo
	    	System.out.println( "QUANTUM TIMEOUT task" + current_task.name );
			if( ready_tasks.size()>1 ){
				System.out.println("	with task switching!!!!!!!!!");
				System.out.println("==== ready_task before ADD ====" + ready_tasks);
				ready_tasks.addLast( current_task.name );
				System.out.println("==== ready_task after ADD ====" + ready_tasks);
				task_switching = 2;
				current_quantum = 0;
				return IDLE_TASK;
			}
		
		}
	}
	return null;
//function close
}
//scheduler close class
}
