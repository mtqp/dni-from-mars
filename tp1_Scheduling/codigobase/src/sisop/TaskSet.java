package sisop;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

/**
 * Conjunto de Tasks a ser planificado
 * por un Scheduler
 */
class TaskSet implements Iterable<Task> {

  private final String task_set_name;
 
  private final List<Task> tasks = new LinkedList<Task>();
  private final Set<String> task_names = new HashSet<String>();

  private TaskSet(String task_set_name) {
    super();
    this.task_set_name = task_set_name;
  } 

  private void add(Task task) {
    tasks.add(task);
    if (task_names.contains(task.name))
      throw new IllegalArgumentException("cannot store two tasks with same name");

    task_names.add(task.name);
  }

  /**
   * Retorna el Task correspondiente
   * al task_name solicitado.
   * Retorna null si no existe el nombre
   */
  public Task get(String task_name) {
    for (Task task: tasks) 
      if (task.name.equals( task_name ) )
        return task;

    return null;
  }

  /**
   * Retorna true sii existe un Task
   * con el nombre task_name
   */
  public boolean contains(String task_name) {
    return task_names.contains(task_name);
  }

  /**
   * Retorna el nombre del TaskSet
   */
  public String get_name() {
    return task_set_name;
  }

  /**
   * Permite iterar sobre los Tasks. 
   * El orden de iteracion corresponde al orden
   * de llegada de los tasks al TaskSet
   */
  public Iterator<Task> iterator() {
    return tasks.iterator();
  }  

  /**
   * Retorna una lista con los Tasks que se han
   * convertido en el instante <code>time</code> en 
   * preparados para su ejecucion.
   * El orden de la lista corresponde al orden de 
   * llegada de esos tasks al TaskSet.
   */
  public List<String> get_released_tasks_at(int time) {
    List<String> incoming_tasks = new LinkedList<String>();
    for (Task task: tasks) {
      if (task.rtime==time) {
        incoming_tasks.add(task.name);
      }
    }
    return incoming_tasks;
  }

  /**
   * Retorna la cantidad de Tasks en el 
   * TaskSet.
   */
  public int size() {
    return tasks.size();
  }

  /**
   * Retorna el maximo rtime (release time)
   * de un Task en este TaskSet.
   */
  public int get_max_rtime() {
    int max_rtime = -1;
    for (Task task: tasks) {
      if (task.rtime>max_rtime)
        max_rtime = task.rtime;
    } 
    return max_rtime;
  }

  /**
   * Retorna el nombre del TaskSet
   */
  public String toString() {
    return task_set_name;
  }   

  /**
   * van llegando tareas cada vez mas largas
   **/
  public static final TaskSet TASK_SET_1 = build_task_set_1();

  private static TaskSet build_task_set_1() {
    TaskSet ts1 = new TaskSet("ts1");
    ts1.add(new Task("task1", 10, 0));
    ts1.add(new Task("task2", 20, 5));
    ts1.add(new Task("task3", 30, 10));
    ts1.add(new Task("task4", 40, 15));
    ts1.add(new Task("task5", 50, 20));
    ts1.add(new Task("task6", 60, 25));
    ts1.add(new Task("task7", 70, 30));
    ts1.add(new Task("task8", 80, 35));
    return ts1;
  }

  /**
   * van llegando tareas cada vez mas cortas
   **/
  public static final TaskSet TASK_SET_2 = build_task_set_2();

  private static TaskSet build_task_set_2() {
    TaskSet ts2 = new TaskSet("ts2");
    ts2.add( new Task("task1", 80, 0) ) ;
    ts2.add( new Task("task2", 70, 5) );
    ts2.add( new Task("task3", 60, 10) );
    ts2.add( new Task("task4", 50, 15) );
    ts2.add( new Task("task5", 40, 20) );
    ts2.add( new Task("task6", 30, 25) ); 
    ts2.add( new Task("task7", 20, 30) );
    ts2.add( new Task("task8", 10, 35) );
    return ts2;
  }

  /**
   * llega un lote de tareas diversas
   **/
   public static final TaskSet TASK_SET_3 = build_task_set_3();

   private static TaskSet build_task_set_3() {
    TaskSet ts3 = new TaskSet("ts3");
    ts3.add( new Task("task1", 40, 0) );
    ts3.add( new Task("task2", 20, 0) );
    ts3.add( new Task("task3", 10, 0) );
    ts3.add( new Task("task4", 50, 0) );
    ts3.add( new Task("task5", 80, 0) );
    ts3.add( new Task("task6", 30, 0) );
    ts3.add( new Task("task7", 70, 0) );
    ts3.add( new Task("task8", 60, 0) );
    return ts3;
  }

  /**
   * llegan tareas diversas en 2 lotes
   **/ 
   public static final TaskSet TASK_SET_4 = build_task_set_4();

   private static TaskSet build_task_set_4() {
    TaskSet ts4 = new TaskSet("ts4");
    ts4.add( new Task("task1", 40, 0) );
    ts4.add( new Task("task2", 20, 0) );
    ts4.add( new Task("task3", 10, 0) );
    ts4.add( new Task("task4", 50, 0) );
    ts4.add( new Task("task5", 80, 100) );
    ts4.add( new Task("task6", 30, 100) );
    ts4.add( new Task("task7", 70, 100) );
    ts4.add( new Task("task8", 60, 100) );

    return ts4;
  }   

  /**
   * servidor con 3 clientes
   **/
   public static final TaskSet TASK_SET_5 = build_task_set_5();

   private static TaskSet build_task_set_5() {
    TaskSet ts5 = new TaskSet("ts5");
    ts5.add(new Task("iniciar_bd", 30, 0) );
    ts5.add(new Task("iniciar_servidor", 20, 0) );
    ts5.add(new Task("prueba_de_rutina", 5, 0) );
    ts5.add(new Task("cliente1", 150, 0) );
    ts5.add(new Task("cliente2", 30, 0) );
    ts5.add(new Task("cliente3", 50, 0) );
    ts5.add(new Task("apagar_servidor", 10, 0) );
    ts5.add(new Task("apagar_bd", 15, 0) );     

    return ts5;
  }   

  /**
   * servidor con 5 clientes
   **/
   public static final TaskSet TASK_SET_6 = build_task_set_6();

   private static TaskSet build_task_set_6() {
    TaskSet ts6 = new TaskSet("ts6");
    ts6.add( new Task("iniciar_bd", 30, 0) );
    ts6.add( new Task("iniciar_servidor", 20, 0) );
    ts6.add( new Task("prueba_de_rutina", 5, 0) );
    ts6.add( new Task("cliente1", 150, 0) );
    ts6.add( new Task("cliente2", 30, 0) );
    ts6.add( new Task("cliente3", 50, 0) );
    ts6.add( new Task("cliente4", 10, 0) );
    ts6.add( new Task("cliente5", 5, 0) );
    ts6.add( new Task("apagar_servidor", 10, 0) );
    ts6.add( new Task("apagar_bd", 15, 0) );
    return ts6;
  } 

   /**
   * SILBERSCHATZ pag. 142
   */
   public static final TaskSet FCFS_1 = build_fcfs_1();

   private static TaskSet build_fcfs_1() {
     TaskSet fcfs_1 = new TaskSet("fcfs_1");
     fcfs_1.add( new Task("P1",24,0) ); 
     fcfs_1.add( new Task("P2",3,0) ); 
     fcfs_1.add( new Task("P3",3,0) ); 
     return fcfs_1;
   }

  /**
   * SILBERSCHATZ pag. 142
   */
   public static final TaskSet FCFS_2 = build_fcfs_2();

   private static TaskSet build_fcfs_2() {
     TaskSet fcfs_1 = new TaskSet("fcfs_2");
     fcfs_1.add( new Task("P2",3,0) ); 
     fcfs_1.add( new Task("P3",3,0) ); 
     fcfs_1.add( new Task("P1",24,0) ); 
     return fcfs_1;
   }

   /**
   * SILBERSCHATZ pag. 143
   */
   public static final TaskSet SJF_1 = build_sjf_1();

   private static TaskSet build_sjf_1() {
     TaskSet sjf_1 = new TaskSet("sjf_1");
     sjf_1.add( new Task("P1",6,0) ); 
     sjf_1.add( new Task("P2",8,0) ); 
     sjf_1.add( new Task("P3",7,0) ); 
     sjf_1.add( new Task("P4",3,0) ); 
     return sjf_1;
   }

   /**
   * SILBERSCHATZ pag. 162
   */
   public static final TaskSet SJF_2 = build_sjf_2();

   private static TaskSet build_sjf_2() {
     TaskSet sjf_2 = new TaskSet("sjf_2");
     sjf_2.add( new Task("P1",10,0) ); 
     sjf_2.add( new Task("P2",29,0) ); 
     sjf_2.add( new Task("P3",3,0) ); 
     sjf_2.add( new Task("P4",7,0) ); 
     sjf_2.add( new Task("P5",12,0) ); 
     return sjf_2;
   }

    /**
    * TaskSet con arrivo tardio
    */
   public static final TaskSet TEST_1 = build_test_1();

   private static TaskSet build_test_1() {
     TaskSet test_1 = new TaskSet("test_1");
     test_1.add( new Task("P1", 15, 5) );
     test_1.add( new Task("P2", 10, 5) );
     test_1.add( new Task("P3", 5, 6) );
     test_1.add( new Task("P4", 5, 40) );
     return test_1;
   } 

   /**
    * TaskSet del Ejercicio 1 del TP
    */
    public static final TaskSet EJ_1 = build_ej_1();

    private static TaskSet build_ej_1() { 
      TaskSet ej_1 = new TaskSet("ej_1");
      ej_1.add( new Task("P1", 10, 5) );
      ej_1.add( new Task("P2", 5,  5) );
      //ej_1.add( new Task("P3", 5,  6) );
      ej_1.add( new Task("P3", 5,  35) );
      return ej_1;
    }

}
