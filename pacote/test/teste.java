class Factorial{
	public static void main(String[] a){
		System.out.println(new Fac().ComputeFac(10));
	}
}

class Fac {
	int base;
	public int ComputeFac(int num){
		int num_aux ;
		base = 1;
		if (num < base)
			num_aux = base;
		else 
			num_aux = num * (this.ComputeFac(num-1)) ;
		return num_aux ;
	}

}
