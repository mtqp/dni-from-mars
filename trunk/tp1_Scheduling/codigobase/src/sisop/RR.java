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
    current_time++;
  
    List<String> newly_released_tasks = task_set.get_released_tasks_at( current_time );
    
    // Si llegó un nuevo proceso lo agrega a la cola de alta prioridad
    if( !newly_released_tasks.isEmpty() ) 
    {
      ready_tasks.addAll(newly_released_tasks);
    }

    if(task_switching > 0)
    {
		  task_switching--;
		  //System.out.println( ">>> Task Switch: IDLE <<<" );
		  return IDLE_TASK;
	  }

    // Si no hay ningún proceso ejecutándose, busca el próximo a ejecutar
    if( current_task==null ) 
    {
      current_task = get_next_task(false,false);
    }
    // Si hay un proceso en ejecución
    else
    {
      // Incrementa el quantum que estuvo el proceso actual
      current_quantum++;
      
      // Si el proceso terminó
      if( current_task.ttime == current_task.ptime )
      {
        
        current_task.ftime = current_time;
        
        // Agrega la tarea a la lista de terminados
        finished_tasks.addLast( current_task.name );

        // Si está en IDLE y no hay más procesos, terminó
        if( task_set.size()==finished_tasks.size() )
          return null;
      
        current_task = get_next_task(true,true);
      
      }
      // Si se acabó el quantum
      else if( current_quantum==quantum )
      {
        current_task = get_next_task(true,false);
      }
      
    }

    /*
    System.out.println( "Time: " + current_time + " | Quantum: " + (current_quantum) );
    System.out.println( "Running: " + (current_task==null?"IDLE":current_task.name) );
    System.out.println( "New: " + newly_released_tasks );
    System.out.println( "Ready: " + ready_tasks + "\n-----------" );
    */

    if( current_task == null )
    {
      return IDLE_TASK;
    }
    else
    {
      current_task.ttime++;
      return current_task.name;
    }
    
  }

  private Task get_next_task(boolean task_switch, boolean finished)
  {
      current_quantum = 0;
      
      if( task_switch )
      {
        if( !finished )
        {
          if( finished_tasks.size() == task_set.size()-1 )
            return current_task;

          ready_tasks.addLast( current_task.name );
        }
        task_switching = 1;
        return null;
      }
      
      // Si no hay procesos, devuelve IDLE
      if( ready_tasks.isEmpty() )
      {
        return null;
      }

      // Obtiene el proceso a ejecutar
      current_task  = task_set.get( ready_tasks.getFirst() );

      // Quita el proceso a ejecutar de la cola de listos
      ready_tasks.removeFirst();

      if( current_task.ttime == 0 )
        current_task.wtime = current_time - current_task.rtime;
  
      return current_task;
  }


}
