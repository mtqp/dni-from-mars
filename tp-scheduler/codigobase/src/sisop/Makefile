CLASS_DIR=../../bin
SOURCES = Task.java TaskSet.java TimelinePrinter.java Scheduler.java SchedulerAnalyzer.java FCFS.java MFQ.java RR.java SJF.java Simulator.java 
OBJS = $(patsubst %.java,$(CLASS_DIR)/sisop/%.class,$(SOURCES))

debug:
	@echo $(OBJS)

all: bindir clean binaries

binaries: $(OBJS)

$(OBJS): $(@F:.class=.java)
	javac -cp $(CLASS_DIR) -d $(CLASS_DIR)/ $(@F:.class=.java)

bindir: $(CLASS_DIR)

$(CLASS_DIR):
	mkdir $(CLASS_DIR)

clean:
	find $(CLASS_DIR) -name *.class -delete
