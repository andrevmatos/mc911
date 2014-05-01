class a
{
	public static void main(String[] args) 
	{
		System.out.println(new TestClass().test(false));
	}
}

class TestClass
{
	int i;
	int j;
	public int test(boolean b)
	{
		int myvar;
		myvar = 2*3;
		return myvar;
	}
}
