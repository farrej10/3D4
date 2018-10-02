int n;
byte f;

proctype p()
{	
	int count = 0;
	int temp = 0;
	do
	::count < 2 ->
			count++;
			temp = n;
			temp = temp + 1;
			n = temp;
	::else -> break;	
	od;
	f++
}

proctype q()
{	
	int count = 0;
	do			
	::count < 2 ->
			count++;		 	
			n = n + 1;
	::else -> break;	
	od;
	f++; 
}


init{

	n = 0;
	f = 0;
	
    run p();
    run q();
	
    f == 2
	assert(n == 4);

}


