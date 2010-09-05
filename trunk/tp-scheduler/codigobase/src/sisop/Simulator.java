package sisop;

import java.util.List;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Map;
import java.io.*;

/**
 * Orquestador de la planificacion. Crea el TaskSet y lo suministra al 
 * Scheduler para que lo planifique. Es el responsable del respeto
 * de la API del Scheduler. 
 */
final class Simulator {

  private Simulator() {}

  private static final Map<String, Scheduler> schedulers = build_schedulers();

  private static Map<String, Scheduler> build_schedulers() {
    Map<String,Scheduler> schedulers = new HashMap<String,Scheduler>();
    FCFS fifo = new FCFS();
    SJF  sjf  = new SJF();
    RR   rr   = new RR();
    MFQ  mfq  = new MFQ();
    schedulers.put(fifo.get_name(), fifo );
    schedulers.put(sjf.get_name(), sjf );
    schedulers.put(rr.get_name(), rr );
    schedulers.put(mfq.get_name(), mfq );
    
    return schedulers;
  }

  private static final Map<String,TaskSet> task_sets = build_task_sets() ;

  private static Map<String,TaskSet> build_task_sets() { 
    Map<String,TaskSet> task_sets = new HashMap<String, TaskSet>();
    task_sets.put( TaskSet.TASK_SET_1.get_name() , TaskSet.TASK_SET_1 );
    task_sets.put( TaskSet.TASK_SET_2.get_name() , TaskSet.TASK_SET_2 );
    task_sets.put( TaskSet.TASK_SET_3.get_name() , TaskSet.TASK_SET_3 );
    task_sets.put( TaskSet.TASK_SET_4.get_name() , TaskSet.TASK_SET_4 );
    task_sets.put( TaskSet.TASK_SET_5.get_name() , TaskSet.TASK_SET_5 );
    task_sets.put( TaskSet.TASK_SET_6.get_name() , TaskSet.TASK_SET_6 );

    task_sets.put( TaskSet.FCFS_1.get_name() , TaskSet.FCFS_1 );
    task_sets.put( TaskSet.FCFS_2.get_name() , TaskSet.FCFS_2 );
    task_sets.put( TaskSet.SJF_1.get_name() , TaskSet.SJF_1 );
    task_sets.put( TaskSet.SJF_2.get_name() , TaskSet.SJF_2 );

    task_sets.put ( TaskSet.TEST_1.get_name(), TaskSet.TEST_1 );
    task_sets.put ( TaskSet.EJ_1.get_name(), TaskSet.EJ_1 );
    return task_sets;
  }

  private static void print_available_scheds() {
    System.out.println("Available schedulers:");
    System.out.println(schedulers.keySet().toString());
  }

  private static void print_available_tss() {
    System.out.println("Available task sets:");
    System.out.println(task_sets.keySet().toString());
  }

  /**
   * Entry point del Simulador de planificadores.
   * Los parametros que espera son
   * args[0] nombre del scheduler
   * args[1] nombre del TaskSet
   * args[2] (opcional) quatum (int)
   */
  public static void main(String[] args) {
    if (args.length!=2 && args.length!=3) {
      System.out.println("Usage: java -cp bin sisop.Simulator <scheduler> <task_set>");
      print_available_scheds();
      print_available_tss();
      System.exit(1);
    }
    String scheduler_name = args[0];
    String task_set_name  = args[1];


    Scheduler scheduler = schedulers.get(scheduler_name);
    if (scheduler==null) {
       System.out.println("Unknown scheduler: " + scheduler_name);
       print_available_scheds();
       System.exit(1);
    }

    if (args.length==3) {
      try {
        int quantum= new Integer(args[2]);

        if (scheduler instanceof RR) {
          RR rr = (RR)scheduler;
          rr.set_quantum(quantum);
        }

      } catch (NumberFormatException ex) {
        System.out.println(args[2] + " is not a valid quantum value");
        System.exit(1);
      }
    }

    TaskSet   task_set  = task_sets.get(task_set_name);    
    if (task_set==null) {
       System.out.println("Unknown task_set: " + task_set_name);
       print_available_tss();
       System.exit(1);
    }    


    scheduler.init(task_set);

    List<String> timeline = new LinkedList<String>();
    while (true) {
      String task_name = scheduler.next();
      if (task_name==null)
        break;

      timeline.add(task_name);
    }

    TimelinePrinter printer = new TimelinePrinter(timeline);
    System.out.print(printer.print());

    SchedulerAnalyzer analyzer = new SchedulerAnalyzer(task_set);
    int avg_wtime = analyzer.get_avg_wtime();

    System.out.println("-------------------------------------");
    System.out.println("Resultados analisis del scheduling producido:");
    System.out.println(String.format("tiempo de espera promedio: %s", avg_wtime));
    System.out.println("-------------------------------------");
    

    write_to_file(timeline, scheduler_name, task_set_name);

    

  }


  private static void write_to_file(List<String> timeline,
                                    String scheduler_name,
                                    String task_set_name) {

    FileOutputStream out;
    PrintStream p;
    try {
      out = new FileOutputStream("timeline.dat");
      p = new PrintStream( out );
      p.println(String.format("%s %s %s", timeline.size(), scheduler_name, task_set_name));
      for (String task_name : timeline)
        p.println (task_name);

      p.close();
    } catch (IOException e) {
      System.err.println ("Error writing timeline.dat");
    }
  }

  

}
