package osp.Threads;
import java.util.Vector;
import java.util.Enumeration;
import java.util.PriorityQueue;
import java.lang.Object;
import osp.Utilities.*;
import osp.IFLModules.*;
import osp.Tasks.*;
import osp.EventEngine.*;
import osp.Hardware.*;
import osp.Devices.*;
import osp.Memory.*;
import osp.Resources.*;

/**
   This class is responsible for actions related to threads, including
   creating, killing, dispatching, resuming, and suspending threads.

   @OSPProject Threads
*/



public class ThreadCB extends IflThreadCB 
{
    //private static GenericList readyQueue;
    private static GenericList priority0; 
    private static GenericList priority1;
    private static GenericList priority2; 
    private static long startTime; 
    private static long endTime; 
    private static int counter;
    private static long totalRanTime;
    private static long executeStartTime;  
    private static int firstTime; 
     

    /**
       The thread constructor. Must call 

       	   super();

       as its first statement.

       @OSPProject Threads
    */
    public ThreadCB()
    {
	super();
    }


    /**
       This method will be called once at the beginning of the
       simulation. The student can set up static variables here.
       
       @OSPProject Threads
    */
    public static void init()
    {
	//readyQueue = new GenericList();
	priority0 = new GenericList(); 
	priority1 = new GenericList();
	priority2 = new GenericList(); 
    startTime = 0; 
    endTime = 0;  
    counter = 0; 
    totalRanTime = 0;
	executeStartTime = 0; 
	firstTime = 0; 
    }

    /** 
        Sets up a new thread and adds it to the given task. 
        The method must set the ready status 
        and attempt to add thread to task. If the latter fails 
        because there are already too many threads in this task, 
        so does this method, otherwise, the thread is appended 
        to the ready queue and dispatch() is called.

	The priority of the thread can be set using the getPriority/setPriority
	methods. However, OSP itself doesn't care what the actual value of
	the priority is. These methods are just provided in case priority
	scheduling is required.

	@return thread or null

        @OSPProject Threads
    */
    static public ThreadCB do_create(TaskCB task)
    {
	    if (task == null) {
	        dispatch();
	        return null;
	    }
        
	    // Can we add a new thread to this task?
	    if (task.getThreadCount() >= MaxThreadsPerTask) {		   
	        MyOut.print("osp.Threads.ThreadCB",
			    "Failed to create new thread "
			    + " -- maximum number of threads for "
			    + task + " reached");
	        dispatch();
	        return null;
	    }

	    ThreadCB newThread = new ThreadCB();
	    MyOut.print("osp.Threads.ThreadCB", "Created "+newThread);
      newThread.startTime = HClock.get();
        
	    if (newThread.firstTime == 0) {
		    newThread.executeStartTime = newThread.startTime; 
		    newThread.firstTime = 1; 
	    }
     
        //long time = HClock.get(); 
       MyOut.print("osp.Threads.ThreadCB", "START TIME: " + newThread.startTime); 
	    //MyOut.print("osp.Threads.ThreadCB", "TASK PRIORITY: " + task.getPriority()); 	
	    // Setup the new thread.

      switch (counter)
      {
          case 0:
              task.setPriority(0);
              break;          
          case 1:
              task.setPriority(1);
              break;            
          case 2: 
              task.setPriority(2);
              break;     
      }    
      
      counter = (counter+1)%3; 
      MyOut.print("osp.Threads.ThreadCB", "TASK PRIORITY: " + task.getPriority());
      newThread.setPriority(task.getPriority());
      newThread.setStatus(ThreadReady);

	    newThread.setTask(task);
      ThreadCB currentThread = null;

	    switch(newThread.getPriority())
	    {
		    case 0: 
			    priority0.append(newThread);
			    break;
		    case 1: 
			    priority1.append(newThread); 
			    break; 
		    case 2: 
			    priority2.append(newThread); 
			    break;
	    }
	    
	    
      try {
          currentThread = MMU.getPTBR().getTask().getCurrentThread();
      } catch (Exception e) {}//MyOut.print("osp.Threads.ThreadCB", "FLAGGGG" + e.toString()); }
	
	    // Add the new thread to the task.
      if (task.addThread(newThread) != SUCCESS) {
          MyOut.print("osp.Threads.ThreadCB",
          "Could not add thread "+ newThread+" to task "+task);
          dispatch();
          return null;
      }
      if (currentThread != null)
      {

          if (task.addThread(newThread) != SUCCESS) {
            MyOut.print("osp.Threads.ThreadCB",
            "Could not add thread "+ newThread+" to task "+task);
            dispatch();
            return null;
          }
          
          MyOut.print("osp.Threads.ThreadCB",
              "Successfully added "+newThread+" to "+task);

          

            dispatch();
      }
      dispatch();
    	return newThread;
    }

    /** 
	Kills the specified thread. 

	The status must be set to ThreadKill, the thread must be
	removed from the task's list of threads and its pending IORBs
	must be purged from all device queues.
        
	If some thread was on the ready queue, it must removed, if the 
	thread was running, the processor becomes idle, and dispatch() 
	must be called to resume a waiting thread.
	
	@OSPProject Threads
    */
    public void do_kill()
    {
        MyOut.print(this, "Entering do_kill(" +this + ")");

        TaskCB task = getTask();

	switch (getStatus()) {
        case ThreadReady:
	    // Delete thread from ready queue.
	    //readyQueue.remove(this);
		switch(this.getPriority())
		{
			case 0: 
			priority0.remove(this);
			break;
			case 1: 
			priority1.remove(this); 
			break; 
			case 2: 
			priority2.remove(this); 
			break;how to get the first element in a list java
		}
	    break;
	case ThreadRunning:
	    // Remove (preempt) thread from CPU.
	    if(this == MMU.getPTBR().getTask().getCurrentThread()) {
		MMU.getPTBR().getTask().setCurrentThread(null);
	    }
	    break;
	default:
	}       

        // Remove thread from task.
        if(task.removeThread(this) != SUCCESS) {
	    MyOut.print(this,
			"Could not remove thread "+ this+" from task "+task);
	    return;
	}
    
        MyOut.print(this, this + " is set to be destroyed");
        //long time = HClock.get(); 
        //MyOut.print("osp.Threads.ThreadCB", "Time destroyed: " + this + time); 
	// Change thread's status.
	setStatus(ThreadKill);

	// We have only one I/O per thread, so we should just
	// cancel it for the corresponding device.
        for(int i = 0; i < Device.getTableSize(); i++) {
	    MyOut.print(this, "Purging IORBs on Device " + i);
	    Device.get(i).cancelPendingIO(this);
	}

        // release all resources owned by the thread
        ResourceCB.giveupResources(this);

	dispatch();

	if (this.getTask().getThreadCount()==0) {
	    MyOut.print(this,
			"After destroying " + this + ": " + this.getTask() 
			+ " has no threads left; destroying the task");
	    this.getTask().kill();
	}
        this.endTime = HClock.get(); 
        //MyOut.print(this, "THREAD " + this + " RAN FOR: " +  (this.endTime - this.startTime)); 
        this.totalRanTime += this.endTime - this.startTime; 
        MyOut.print(this, "THIS THREAD RAN FOR: " + (this.endTime - this.startTime)); 
        MyOut.print(this, "THREADS SUM RUNNING TIME: " +  this.totalRanTime);
        MyOut.print(this, "END TIME: " +  this.endTime);
        long executTime = this.endTime - this.executeStartTime; 
        MyOut.print(this, "EXECUTION TIME " + executTime); 
	      
    }


    /** Suspends the thread that is currenly on the processor on the 
        specified event.how to get the first element in a list java 

        Note that the thread being suspended doesn't need to be
        running. It can also be waiting for completion of a pagefault
        and be suspended on the IORB that is bringing the page in.
	
	Thread's status must be changed to ThreadWaiting or higher,
        the processor set to idle, the thread must be in the right
        waiting queue, and dispatch() must be called to give CPU
        control to some other thread.

	@param event - event on which to suspend this thread.

        @OSPProject Threads
    */
    public void do_suspend(Event event)
    {
	int oldStatus = this.getStatus();
        //MyOut.print(this, "Old Status : " + ThreadRunning);
        //MyOut.print(this, "Old Status : " + ThreadWaiting);
        MyOut.print(this, "Entering suspend(" + this + "," + event + ")");
    
	// Note: "this" might not be the running thread, because we
	// might be suspending a thread that is in the middle of a system call
	// (e.g., a pagefaulted thread after swapout or a thread that 
	// is ready for I/O after a pagefault caused by lock()
	ThreadCB runningThread=null;
	TaskCB runningTask=null;
	try {
	    runningTask = MMU.getPTBR().getTask();
	    runningThread = runningTask.getCurrentThread();
	} catch(NullPointerException e){}

	// Note: we may be suspending not the running thread, so
	// we must check if "this" equals runningThread
	if (this == runningThread)
	    this.getTask().setCurrentThread(null);
	    
	// Set thread's status.
	if (this.getStatus() == ThreadRunning)
	    setStatus(ThreadWaiting);
	else if (this.getStatus() >= ThreadWaiting)
	    setStatus(this.getStatus()+1);
	
	switch(this.getPriority())
		{
			case 0: 
			priority0.remove(this);
			break;
			case 1: 
			priority1.remove(this); 
			break; 
			case 2: 
			priority2.remove(this); 
			break;
		}
	event.addThread(this);
    //this.timeSus = HClock.get(); 
	// Dispatch a new thread.
	dispatch();
    }

    
    /** Resumes the thread.
        
	Only a thread with the status ThreadWaiting or higher
	can be resumed.  The status must be set to ThreadReady or
	decremented, respectively.
	A ready thread should be placed on the ready queue.
	
	@OSPProject Threads
    */
    public void do_resume()
    {
        if(getStatus() < ThreadWaiting) {
	        MyOut.print(this,
			    "Attempt to resume "
			    + this + ", which wasn't waiting");
	        return;
	      }

        MyOut.print(this, "Resuming " + this);

        // Set thread's status.
	if (getStatus() == ThreadWaiting) {
	    setStatus(ThreadReady);
        //this.timeRes = HClock.get(); 
        //MyOut.print(this, "TIME SUSPENDED: " + (this.timeRes - this.timeSus)); 
	} else if (getStatus() > ThreadWaiting)
	    setStatus(getStatus()-1);

        // Put the thread on the ready queue, if appropriate
	if (getStatus() == ThreadReady){
		switch(this.getPriority())
		{
			case 0: 
			priority0.append(this);
			break;
			case 1: 
			priority1.append(this); 
			break; 
			case 2: 
			priority2.append(this); 
			break;
		}
    }
	dispatch();
    }
    

    /** 
        Selects a thread from the run queue and dispatches it. 

        If there is just one theread ready to run, reschedule the thread 
        currently on the processor.

        In addition to setting the correct thread status it must
        update the PTBR.
	
	@return SUCCESS or FAILURE

        @OSPProject Threads
    */
    public static int do_dispatch()
    {
        ThreadCB threadToDispatch=null;
        ThreadCB runningThread=null;
        TaskCB runningTask=null;
        try {
            runningTask = MMU.getPTBR().getTask();
            runningThread = runningTask.getCurrentThread();
        } catch(NullPointerException e) {}


        // Select thread from ready queue.

	      if (!priority2.isEmpty()) {
		      threadToDispatch = (ThreadCB) priority2.removeHead(); 
	      }
	      else if (!priority1.isEmpty()) { 
		      threadToDispatch = (ThreadCB) priority1.removeHead();
	      }
	      else if (!priority0.isEmpty()) {
		      threadToDispatch = (ThreadCB) priority0.removeHead(); 
	      }
			
        //threadToDispatch = (ThreadCB)readyQueue.removeHead();
        if(threadToDispatch == null && runningThread == null) {
            MyOut.print("osp.Threads.ThreadCB",
            "Can't find suitable thread to dispatch");
            MMU.setPTBR(null);
            return FAILURE;
        }
        // If necessary, remove current thread from processor and 
        // reschedule it.
        if(runningThread != null && threadToDispatch != null) {
        
            if(threadToDispatch.getPriority() > runningThread.getPriority()) {
                MyOut.print("osp.Threads.ThreadCB", "Preempting currently running " + runningThread);
                runningTask.setCurrentThread(null);

                MMU.setPTBR(null);

                runningThread.setStatus(ThreadReady);

                switch(runningThread.getPriority())
                {
                case 0: 
                  priority0.insert(runningThread);
                  break;
                case 1: 
                  priority1.insert(runningThread); 
                  break; 
                case 2: 
                  priority2.insert(runningThread); 
                  break;
                }
                // Put the thread on the processor.
                MMU.setPTBR(threadToDispatch.getTask().getPageTable());

                // set thread to dispatch as the current thread of its task 
                threadToDispatch.getTask().setCurrentThread(threadToDispatch);

                // Set thread's status.
                threadToDispatch.setStatus(ThreadRunning);

                MyOut.print("osp.Threads.ThreadCB",
                "Dispatching " + threadToDispatch);

                HTimer.set(150);

                return SUCCESS;

            }
            else {/*
              switch(runningThread.getPriority())
              {
                case 0: 
                    priority0.insert(runningThread);
                    break;
                case 1: 
                    priority1.insert(runningThread); 
                    break; 
                case 2: 
                    priority2.insert(runningThread); 
                    break;
              }
              */                HTimer.set(150);
            return SUCCESS;         
            }
        }   


        else {
          if (threadToDispatch != null)
          {
            // Put the thread on the processor.
            MMU.setPTBR(threadToDispatch.getTask().getPageTable());

            // set thread to dispatch as the current thread of its task 
            threadToDispatch.getTask().setCurrentThread(threadToDispatch);

            // Set thread's status.
            threadToDispatch.setStatus(ThreadRunning);

            MyOut.print("osp.Threads.ThreadCB",
            "Dispatching 2" + threadToDispatch);

            HTimer.set(150);

            return SUCCESS;
          }
          else return SUCCESS;
          }
    }


    /**
       Called by OSP after printing an error message. The student can
       insert code here to print various tables and data structures in
       their state just after the error happened.  The body can be
       left empty, if this feature is not used.

       @OSPProject Threads
    */
    public static void atError()
    {
	// any code
    }

    /** Called by OSP after printing a warning message. The student
        can insert code here to print various tables and data
        structures in their state just after the warning happened.
        The body can be left empty, if this feature is not used.
       
        @OSPProject Threads
     */
    public static void atWarning()
    {
	// any code
    }
}
