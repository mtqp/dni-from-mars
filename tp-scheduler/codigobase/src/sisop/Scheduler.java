package sisop;

/**
 * Un planificador abstracto. Cada planificador
 * concreto es una especializacion de esta clase.
 * El protocolo de uso de esta clase es :
 * 1) Creacion 2) init(TaskSet) 3) next()
 */
abstract class Scheduler { 
   private final String scheduler_name;

   /**
    * Conjunto de tareas que el Scheduler
    * cuya ejecucion se debe planificar
    */
   protected TaskSet task_set;

   /**
    * Etiqueta especial para indicar que el 
    * procesador esta ocioso.
    */
   public static final String IDLE_TASK = "IDLE_TASK";

   /**
    * Construye un nuevo Scheduler
    * con el nombre indicado
    */
   public Scheduler(String scheduler_name) {
     super();
     this.scheduler_name = scheduler_name;
     this.task_set = null;
   }

   /**
    * Retorna el nombre del Scheduler
    */
   public final String get_name() {
     return scheduler_name;
   }

   /**
    * Inicializa el Scheduler. 
    * Este metodo es invocado una unica vez
    * antes de comenzar a planificar los trabajos. 
    */
   public final void init(TaskSet task_set) {
     if (this.task_set!=null) 
       throw new IllegalStateException("cannot initialize a scheduler twice");
     
     this.task_set = task_set;
     this.scheduler_init();
   }

   /**
    * Retorna el nombre del trabajo que ocupa el 
    * slot de tiempo actual. Si el TaskSet se ha
    * agotado retorna <code>null</code>.
    */
   public final String next() {
     if (this.task_set==null)
        throw new IllegalStateException("must initialize the scheduler before using it");

     String next = scheduler_next();
     return next;
   }

   /**
    * Retorna el nombre del scheduler
    */
   public final String toString() {
     return scheduler_name;
   }

   /**
    * Cada scheduler concreto debe especificar
    * como realiza su inicializacion.
    */
   protected abstract void   scheduler_init();

   /**
    * Cada scheduler concreto debe especificar
    * como realiza su planeamiento.
    */
   protected abstract String scheduler_next(); 
}
