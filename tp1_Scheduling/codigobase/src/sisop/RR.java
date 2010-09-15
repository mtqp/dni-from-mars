package sisop;

/**
 * Implementacion de un scheduler 
 * Round Robin. Esta clase esta vacia
 */ 
class RR extends Scheduler {
  
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
    throw new UnsupportedOperationException("Implementar este metodo");
  }


  public String scheduler_next() {
    throw new UnsupportedOperationException("Implementar este metodo");
  }

}
