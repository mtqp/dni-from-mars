package sisop;

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
    int sumatoria_wtimes = 0;
    for (Task t : this.task_set)
    {
    	sumatoria_wtimes += t.wtime;
    }
    return sumatoria_wtimes/this.task_set.size();
  }

}
