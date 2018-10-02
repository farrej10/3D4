#include <pthread.h> 
#include <stdio.h> 
#include <stdlib.h> 
#include <string.h>

#define NUM_CONSUMER_THREADS 3
#define NUM_PRINTING_THREADS 1

struct bundle{
  char  text[100];
  int   thread;
};

//globals
//create a bundle to be shared between each thread;
struct bundle data;
pthread_mutex_t threadIDLock;
pthread_mutex_t bundleLock;
pthread_cond_t readyToType;
pthread_cond_t readyToRead;
pthread_cond_t readyToPrint;


void *consumerThread(void *arg) { 
  //local Vars

  //threadIDLock and unlock mutex to allow setting thread number
  pthread_mutex_lock(&threadIDLock);
  int *localThreadNum = (int*)arg;
  printf("Consumer Thread: %d\n", *localThreadNum);
  (*localThreadNum)++;
  pthread_mutex_unlock(&threadIDLock);

    pthread_mutex_lock(&bundleLock);
    pthread_cond_wait(&readyToRead,&bundleLock);
    printf("sfsdfsdfsdfsdfsdfsdfsdfsd\n");
    printf("%s\n",data.text);
    data.thread = *localThreadNum;
    
    
    pthread_cond_signal(&readyToPrint);
    pthread_mutex_unlock(&bundleLock);


  printf("sfsdfsdfsdfsdfsdfsdfsdfsd\n");

  
	pthread_exit(NULL); 
} 

void *printingThread(void *arg) { 

  //threadIDLock and unlock mutex to allow setting thread number
  pthread_mutex_lock(&threadIDLock);
  int *localThreadNum = (int*)arg;
  printf("PRINTER: %d\n", *localThreadNum);
  (*localThreadNum)++;
  pthread_mutex_unlock(&threadIDLock);


    pthread_mutex_lock(&bundleLock);
    pthread_cond_wait(&readyToPrint,&bundleLock);
    printf("sfsdfsdfsdfsdfsdfsdfsdfsd2\n");
    printf("%s %d\n",data.text,data.thread);
    pthread_cond_signal(&readyToType);
    pthread_mutex_unlock(&bundleLock);




	pthread_exit(NULL); 
} 


int main (int argc, const char * argv[]) { 

	pthread_t consumerThreads[NUM_CONSUMER_THREADS];
	pthread_t printingThreads[NUM_PRINTING_THREADS];
	int rc,i,j; 
	
	//init the threadIDLock
  if(pthread_mutex_init(&threadIDLock,NULL) !=0)
  {
    printf("mutex init failed");
    return 1;
  }
  	//init the threadIDLock
  if(pthread_mutex_init(&bundleLock,NULL) !=0)
  {
    printf("mutex init failed");
    return 1;
  }
  if(pthread_cond_init(&readyToRead,NULL))
  {
    printf("readyToRead init failed");
    return 1;
  }
  if(pthread_cond_init(&readyToPrint,NULL))
  {
    printf("readyToPrint init failed");
    return 1;
  }
  if(pthread_cond_init(&readyToType,NULL))
  {
    printf("readyToPrint init failed");
    return 1;
  }
  
  int cCount = 0;
  int pCount = 0;

	//create consumer threads
	for (i=0;i<NUM_CONSUMER_THREADS;i++) {
	
		printf("Creating consumer thread %d\n",i); 
		rc = pthread_create(&consumerThreads[i],NULL,consumerThread,(void *)&cCount);
		
		if (rc) { 
			printf("ERROR return code from pthread_create(): %d\n",rc); 
			exit(-1); 
		} 
		
	} 
	//create printing threads
	for (j=0;j<NUM_PRINTING_THREADS;j++) { 
		
		printf("Creating printing thread %d\n",j); 
		rc = pthread_create(&printingThreads[j],NULL,printingThread,(void *)&pCount);
		
		if (rc) { 
			printf("ERROR return code from pthread_create(): %d\n",rc); 
			exit(-1); 
		} 
		
	} 
	
	 char local[200];
	//control loop

	  printf("Type something \n");
	  scanf("%s", local);
    printf("\n");
    
    pthread_mutex_lock(&bundleLock);
    strcpy(data.text,local);
    pthread_cond_signal(&readyToRead);
    pthread_mutex_unlock;
   

	//join consumer threads
	for(i=0;i<NUM_CONSUMER_THREADS;i++){
	
	  pthread_join(consumerThreads[i],NULL);
	
	}
	
	//join printing threads
	for(j=0;j<NUM_PRINTING_THREADS;j++){
	
	  pthread_join(printingThreads[j],NULL);
	
	}
	//destroy the threadIDLock
	pthread_mutex_destroy(&threadIDLock);
	pthread_mutex_destroy(&bundleLock);
	pthread_cond_destroy(&readyToPrint);
	pthread_cond_destroy(&readyToRead);
	pthread_cond_destroy(&readyToType);
	
	return 0;
}
