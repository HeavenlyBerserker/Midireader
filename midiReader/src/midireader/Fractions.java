/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package midireader;

/**
 *
 * @author Hong
 */
/* Fraction methods for adding, subtracting, multiplying, and dividing rational number
 * also will have to compute greatest common denominator for use with the add/subtract
 * feature and can reduce the numbers after the appropriate operations have been completed.
 * version 1.3
 */
 
 public class Fractions
 {
 	public int num;  //numerator
 	public int den;  //denominator
 	
 	public Fractions(int n, int d) // constructor that requires 2 digits of input
 	{
 		// assign num = n & den = d as long as d!= 0
 		if(d != 0)
 		{
 			num = n;
 			den = d;
 		}
 		else
 			System.exit(0); 		
 	}
        public Fractions() // constructor that requires 2 digits of input
 	{
 		// assign num = n & den = d as long as d!= 0
 		num = 0;
                den = 1;		
 	}
        public Fractions(float i, float j) // constructor that requires 2 digits of input
 	{
 		// assign num = n & den = d as long as d!= 0
 		num = (int)i;
                den = (int)j;		
 	}
 	private static int gcd(int x, int y)
 	{
 		/* gcd() method finds the greatest common divisor of
 		 * the 2 int variables passed and returns that value
 		 * 
 		 */
 		int mod; // hold a value temporarily to allow switch
 		if(x < y) // always use the larger value as the divisor
 		{
 		   mod = x;
 		   x = y;
 		   y = mod;		
 		}
 		int r = x % y; // r holds the remainder of modulus division
		while (r != 0)  // while modulus division does not result in zero
		{
		  x = y; 
		  y = r;
		  r = x % y; 
		}
		return y;
 		
 	}
 	private Fractions reduce(int n, int d)
 	{
 		if(n == 0){
                    return new Fractions(0,1);
                }
                else {
                    int gcdNum = gcd(n,d);
                    if(gcdNum != 0){
                        d = d / gcdNum; // reduce the denominator using the gcd foun in gcd method
                        n = n / gcdNum; // reduce the numerator using the gcd foun in gcd method
                        return new Fractions(n,d);	// return the new fraction object in lowest form
                    }
                    else{
                        return new Fractions(-1,-1);
                    }
                }
 	}
 	public Fractions add(Fractions b)
 	{
 		int num1 = (this.num * b.den) + (b.num * this.den); // cross multily and add
 		int	num2 = this.den * b.den; // multiply the denominators to make them equivlent
 		
 		return reduce(num1,num2);// calls reduce() method and returns a new Fraction object
		
 	}
 	public Fractions subtract(Fractions b)
 	{
 		int num1 = (this.num * b.den) - (b.num * this.den);// cross multiply and subtact
 		int num2 = this.den * b.den;
                //b.print();
                //System.out.print("Num1 = " + num1 + " & Num2 =" + num2);
                
 		return reduce(num1,num2);// sends to reduce method
 	}
 	public Fractions multiply(Fractions b)
 	{
 		int num1 = this.num * b.num; // multiplys straight accross
 		int	num2 = this.den * b.den;
 		
 		return reduce(num1,num2);// sends to reduce method and returns the reduced fraction to the toString() method
 	}
 	public Fractions divide(Fractions b)
 	{ 		
 		int num1 = this.num * b.den; //multiplys the inverse of 2nd fraction object to divide
 		int num2 = this.den * b.num;
 		
 		return reduce(num1, num2);// sends to reduce method
 	}
 	public String toString()  // *** convert to a mixed fraction for output only
 	{	   
 		if(num > den && den > 1)	 //if true will show fraction object and mixed number								 
 			return (num + "/" + den + " or " + (num/den) + " " + (num % den) + "/" + den);		
 		else
 			return(num + "/" + den); //will not try to convert fraction object to mixed number
 	}
        public static float fl(Fractions b)// converts fraction to float
 	{ 		
                float num2 = -1;
                float d = b.num;
                float e = b.den;
 		if(b.den != 0)num2 = d/e; 
 		
 		return num2;
 	}
        public static boolean bigger(Fractions a, Fractions b)// converts fraction to float
 	{ 		
                boolean yes = true;
 		int num1 = a.num * b.den; 
                int num2 = b.num * a.den; 
 		if(num2 > num1) yes = false;
                
 		return yes;
 	}
        public void print()// converts fraction to float
 	{ 		
            System.out.print("[" + this.num + "/" + this.den + "]");
 	}
        
        public void print(String s)// converts fraction to float
 	{ 		
            System.out.print("[" + this.num + "/" + this.den + "]" + s);
 	}
 }

