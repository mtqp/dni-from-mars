package sisop;

import java.util.*;

/**
 * Permite extraer estadisticas de la planificacion.
 * Esta clase es invocada una vez finalizada
 * la planificacion.
 */
class SchedulerAnalyzer {

  private TaskSet task_set;

  /**
   * Construye un nuevo analizador usando
   * un TaskSet producto de una planificacion
   * terminada
   */
  public SchedulerAnalyzer(TaskSet task_set) { 
    this.task_set = task_set;
  }

  /**
   * Retorna el waiting time promedio de 
   * una tarea.
   */
  public int get_avg_wtime() {
    /** -ejercicio 3: calcular el waiting time promedio- **/
    Iterator<Task> it = task_set.iterator();
    int wtime = 0;
    while ( it.hasNext() ) {
      Task task = it.next();
      wtime += task.wtime;
    }
    return wtime / task_set.size();
  }

}
