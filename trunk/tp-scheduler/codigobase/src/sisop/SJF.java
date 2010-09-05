package sisop;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementacion de la catedra de un planificador SJF
 */
class SJF extends Scheduler {

  /**
   * Construye un nuevo planificador SJF
   */
  SJF() {
    super("SJF");
  }

  private LinkedList<String> finished_tasks;
  private LinkedList<String> ready_tasks;
  private int current_time;
  private Task current_task;

  protected void scheduler_init() {
    if (this.task_set.get_max_rtime()<0)
      throw new IllegalStateException("Cannot schedule a task_set with release time<0");

    finished_tasks = new LinkedList<String>();
    ready_tasks = new LinkedList<String>();
    current_task = null;
    current_time = -1;
  }


 
  protected String scheduler_next() {

    if (task_set.size()==finished_tasks.size())      
      return null;

    current_time++;

    List<String> newly_released_tasks = task_set.get_released_tasks_at( current_time );
   
    if (!newly_released_tasks.isEmpty()) {
      ready_tasks.addAll(newly_released_tasks);
    }

    if (current_task==null) {
      if (ready_tasks.isEmpty()) {
        return IDLE_TASK;
      } else {
        current_task = select_shortest_job( ready_tasks );
        ready_tasks.remove(current_task.name );
      }
    } else {
       if ( current_task.ttime == current_task.ptime ) {
         current_task.ftime = current_time;
         finished_tasks.add(current_task.name );

         if (ready_tasks.isEmpty()) {
           current_task = null;
           if (task_set.size()==finished_tasks.size()) {
             return null;
           } else {
             return IDLE_TASK;
           }
         } else {
            current_task = select_shortest_job( ready_tasks );
            ready_tasks.remove(current_task.name );
         }
       }
    }

    current_task.ttime++;
    return current_task.name;

  }


  private Task select_shortest_job( LinkedList<String> ready_tasks ) {
    Task shortest_job = task_set.get( ready_tasks.getFirst() );
    for ( String task_name: ready_tasks ) {
      Task task = task_set.get(task_name);
      if ( task.ptime < shortest_job.ptime )
        shortest_job = task;
    }
    return shortest_job;
  }
}
