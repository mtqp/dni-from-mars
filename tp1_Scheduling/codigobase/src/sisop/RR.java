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

    // Si no hay ningún proceso ejecutándose, busca el próximo a ejecutar
    if (current_task==null) 
    {
      if( ready_tasks.isEmpty() ) 
      {
        return IDLE_TASK;
      }
      else
      {	
        // Agrega el 1er task de los listos
        current_task = task_set.get( ready_tasks.getFirst() ); 
        current_quantum = 0;       
        // Lo elimina de listos
        ready_tasks.removeFirst();
        /** -ejercicio 2: calcular el waiting time- **/
        current_task.wtime = current_time - current_task.rtime;
      }
    } 
    // Si hay algo corriendo
    else 
    {
      // Incrementa el quantum que estuvo el proceso
      current_quantum++;
      
      // Si terminó de procesar
      if( current_task.ttime == current_task.ptime )
      {
        current_task.ftime = current_time;
        
        // Agrega la tarea a la lista de terminados
        finished_tasks.addLast( current_task.name );

        System.out.println("Termina "+ current_task.name + ": " +  ready_tasks);
        
        current_task = null;

        // Si no hay más tareas, termina
        if( ready_tasks.isEmpty() ) 
        {
          if( task_set.size()==finished_tasks.size() )
         	  return null;
        }
        // Si hay procesos listos
        else
        {
          // Hace el task switch
          task_switching = 1;
          current_quantum = 0;
          
          current_task = task_set.get( ready_tasks.getFirst() );
          ready_tasks.removeFirst();
          /** -ejercicio 2: calcular el waiting time- **/
          current_task.wtime = current_time - current_task.rtime;
        }
       	// Si todavía hay sin procesar, devuelve IDLE
        return IDLE_TASK;
      }
      // Si se acabó el tiempo y no es la última tarea, hago task switch
      else if( current_quantum==quantum )
      {
        if( !ready_tasks.isEmpty() )
        {
			    ready_tasks.addLast( current_task.name );
			    task_switching = 1;
			    current_quantum = 0;
			    current_task = null;
			    return IDLE_TASK;
				}
				else
			    current_quantum = 0;
      }

    }
    
    current_task.ttime++;
    return current_task.name;
    
  }

}
