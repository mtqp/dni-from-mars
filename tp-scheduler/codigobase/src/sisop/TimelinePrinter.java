package sisop;

import java.util.List;

/**
 * Imprime la linea de tiempo
 * de la planificacion
 */
class TimelinePrinter {

   private List<String> timeline;
   /**
    * Construye un nuevo Printer 
    * para el timeline 
    */
   public TimelinePrinter(List<String> timeline) { 
     this.timeline = timeline;
   }

   /**
    * Imprime el timeline. Esto se realiza
    * una vez terminada la planificacion
    */
   public String print() {
     StringBuffer result = new StringBuffer();
     int current_time = -1;
     for (String task: timeline) {
       current_time++;
       result.append(String.format("%s -> %s", current_time, task));
       result.append("\n");
     }
     return result.toString();
   }
}
