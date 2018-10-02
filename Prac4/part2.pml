#define NUMPHIL 5
bool pthinking[NUMPHIL], phungry[NUMPHIL], peating[NUMPHIL] = false;
int forks[5] = -1;
int x=0;
#define p1 (peating[0]==true)
ltl iseating { []<>p1}


proctype P(int i)
{
    int right=i;int left = (i+1)%NUMPHIL
Think:  atomic{peating[i]=false; pthinking[i]= true;};

Hungry: atomic{pthinking[i] = false; phungry[i] = true;};
        if::skip;
            atomic{forks[left] == -1 -> forks[left] = i};
            atomic{forks[right] == -1 -> forks[right] = i};
          ::skip;
            atomic{forks[right] == -1 -> forks[right] =i};
            atomic{forks[left] == -1 -> forks[left] = i};
        fi;
Eating: atomic{phungry[i] = false; peating[i] = true;};
Done:   forks[right]=-1; forks[left]=-1;
    goto Think;
}

proctype test()
{
start:
    if::forks[0] == 0 && forks[1] == 1 && forks[2] == 2 && forks[3] == 3 && forks[4] == 4  -> assert(x)
    ::forks[0] == 4 && forks[1] == 0 && forks[2] == 1  && forks[3] == 2 && forks[4] == 3  -> assert(x)
    fi;
goto start;
}

/*
never{
    
TO_init:
    if
    :: (!p1) -> goto accept_all
    :: (1)  -> goto TO_init
    fi;
accept_all:
    skip

}
*/
init{

    atomic
    {
        run P(0);
        run P(1);
        run P(2);
        run P(3);
        run P(4);
        run test();

    }

}
