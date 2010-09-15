package sisop;

/**
 * Representa una Tarea a ser ejecutada por un Scheduler.
 * La tarea contiene informacion conocida al momento de su 
 * creacion (nombre, processor time y released time) e
 * informacion que se agrega luego de ser ejecutada por el 
 * Scheduler (ttime, ftime, wtime)
 */
class Task {
  // immutable data

  /**
   * Nombre del Task
   */
  final String name;  // label of the task

  /**
   * Processing time:
   * time necessary to execute task on the processor without interruption
   */
  final int ptime;   

  /**
   * Released time:
   * Momento en el que la tarea esta lista para su ejecucion
   */
  final int rtime;    


  // mutable data
  /**
   * Tiempo de procesador usado por el task.
   * Inicialmente, vale 0.
   */
  int ttime = 0; 

  /**
   * Finalizatin Time: 
   * Tiempo de finalizacion de la tarea. Inicialmente
   * vale -1.
   */
  int ftime = -1; 

  /**
   * Waiting Time: 
   * Tiempo de espera de la tarea. Inicialmente
   * vale 0.
   */
  int wtime = 0; // waiting time for this task


  /**
   * Construye una nueva tarea definiendo su
   * nombre, processing time y release time
   */
  public Task(String name,
              int ptime,
              int rtime) {
    super();
    this.name=name;
    this.ptime=ptime;
    this.rtime=rtime;
  }

  /**
   * Retorna el nombre de la tarea
   */
  public String toString() {
    return name;
  }  
}
