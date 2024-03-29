package sisop;

import java.util.List;
import java.util.LinkedList;
import java.util.Random;
import java.util.Iterator;
import java.io.*;

/**
 * Implementacion de un planificador Multilevel Feedback Queue. 
 * Esta clase esta vacia.
 */ 
class MFQ extends Scheduler {

  private LinkedList<String> finished_tasks;
  private LinkedList<String> blocked_tasks;
  private LinkedList<String> LPQ; // cola de baja  prioridad
  private LinkedList<String> MPQ; // cola de media prioridad
  private LinkedList<String> HPQ; // cola de alta  prioridad

  private int task_switching;
  private int current_quantum;
  private int current_time;
  private int quantum_high   =  5;
  private int quantum_medium = 15;
  private int quantum_low    = 45;

  private int quantum        =  0;

  private Task current_task;
  
  private LinkedList<String> current_queue = null;

  public MFQ() {
    super("MFQ");
  }

  protected void scheduler_init() {
    if (this.task_set.get_max_rtime()<0)
      throw new IllegalStateException("Cannot schedule a task_set with release time<0");

    finished_tasks = new LinkedList<String>();
    blocked_tasks  = new LinkedList<String>();
    
    HPQ = new LinkedList<String>();
    MPQ = new LinkedList<String>();
    LPQ = new LinkedList<String>();
    
    current_task    = null;
    current_time    = -1;
    current_quantum = 0;
  }


  public String scheduler_next() {
    current_time++;
  
    List<String> newly_released_tasks = task_set.get_released_tasks_at( current_time );
    
    // Si llegó un nuevo proceso lo agrega a la cola de alta prioridad
    if( !newly_released_tasks.isEmpty() ) 
    {
      HPQ.addAll(newly_released_tasks);
    }
    
    // Verifica si hay algún proceso que terminó I/O
    check_blocked();

    if(task_switching > 0)
    {
		  task_switching--;
		  //System.out.println( ">>> Task Switch: IDLE <<<" );
		  return IDLE_TASK;
	  }

    // Si no hay ningún proceso ejecutándose, busca el próximo a ejecutar
    if( current_task==null ) 
    {
      current_task = get_next_task(false,false,false);
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
		current_task.wtime = current_time - current_task.rtime - current_task.ptime;
        
        // Agrega la tarea a la lista de terminados
        finished_tasks.addLast( current_task.name );

        // Si está en IDLE y no hay más procesos, terminó
        if( task_set.size()==finished_tasks.size() )
          return null;
      
        current_task = get_next_task(true,true,false);
      
      }
      // Si se acabó el quantum
      else if( current_quantum==quantum )
      {
        current_task = get_next_task(true,false,false);
      }
      // Decide si el proceso se bloquea
      else if( false && current_task.ttime < current_task.ptime-1 && decide_io() )
      {
        blocked_tasks.addLast( current_task.name );
        current_task.ttime++;
        current_task = get_next_task(true,false,true);
      }
      
    }

    /*System.out.println( "Time: " + current_time + " | Quantum: " + (current_quantum) );
    System.out.println( "Running: " + (current_task==null?"IDLE":current_task.name) );
    System.out.println( "Blocked: " + blocked_tasks );
    System.out.println( "Remaining: [" );
    if( current_task != null )
      System.out.print(current_task.name + ": " + (current_task.ptime-current_task.ttime) + ", ");
    for( String t: HPQ )
    {
      Task task = task_set.get(t);
      System.out.print(task.name + ": " + (task.ptime-task.ttime) + ", ");
    }
    for( String t: MPQ )
    {
      Task task = task_set.get(t);
      System.out.print(task.name + ": " + (task.ptime-task.ttime) + ", ");
    }
    for( String t: LPQ )
    {
      Task task = task_set.get(t);
      System.out.print(task.name + ": " + (task.ptime-task.ttime) + ", ");
    }
    System.out.println( "]" );
    System.out.println( "New: " + newly_released_tasks );
    System.out.println( "HPQ: " + HPQ );
    System.out.println( "MPQ: " + MPQ );
    System.out.println( "LPQ: " + LPQ + "\n-----------" );*/

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

  private Task get_next_task(boolean task_switch, boolean finished, boolean blocked)
  {
      current_quantum = 0;
      
      if( task_switch )
      {
        if( blocked )
        {
          if( finished_tasks.size() == task_set.size()-1 )
            return null;
        }
        else if( !finished )
        {
          if( finished_tasks.size() == task_set.size()-1 )
            return current_task;

          // Baja de prioridad
          if( current_queue == HPQ )
          {
            MPQ.addLast( current_task.name );
          }
          // Si está en Media o Baja prioridad, queda en Baja
          else
          {
            LPQ.addLast( current_task.name );
          }
        }
        task_switching = 1;
        return null;
      }
      
      // Si hay algún proceso de alta prioridad se ejecuta el primero
      if( !HPQ.isEmpty() )
      {
        current_queue = HPQ;
        quantum = quantum_high;
      }
      // Luego los de media prioridad
      else if( !MPQ.isEmpty() )
      {
        current_queue = MPQ;
        quantum = quantum_medium;
      }
      // Por último los de baja prioridad
      else if( !LPQ.isEmpty() )
      {
        current_queue = LPQ;
        quantum = quantum_low;
      }
      // Si no hay procesos, devuelve IDLE
      else
      {
        return null;
      } 

      // Obtiene el proceso a ejecutar
      current_task  = task_set.get( current_queue.getFirst() );

      // Quita el proceso a ejecutar de la cola de listos
      current_queue.removeFirst();
  
      return current_task;
  }
  
  private boolean decide_io()
  {
    Random  r     = new Random();
    boolean block = r.nextInt(9)==0;
    if( block )
    {
      // Establece el tiempo de bloqueo
      current_task.btime  = 1+r.nextInt(quantum-1);
      if( current_queue == LPQ )
      {
        current_task.bqueue = MPQ;
      }
      else
      {
        current_task.bqueue = HPQ;
      }
      //System.out.println( ">>> Bloquea " + current_task.name + " " + current_task.btime );
      
    }
    return block;
  }

  private void check_blocked()
  {
    for( Iterator<String> it = blocked_tasks.iterator() ; it.hasNext(); )
    {
      Task task = task_set.get( it.next() );
      if( task.btime == 0 )
      {
        it.remove();
        task.bqueue.addLast( task.name );
      }
      else
        task.btime--;
    }
  }

}
