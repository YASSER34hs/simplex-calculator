/* This is a fraction class to represent a fraction number in java.
 * It provides four kinds of operations: + - * /. All of them can only be done 
 * with corresponding static methods with other fraction number. And there are
 * also some utility methods to convert int/float/double number to fraction
 * number.
 */

package mysimplex;

import java.math.BigInteger;

public class Fraction {
    public Fraction() throws Exception {
        numerator = BigInteger.valueOf(0);
        denominator = BigInteger.valueOf(1);
    }
    
    public Fraction(BigInteger n, BigInteger d) throws Exception {
        numerator = n;
        denominator = d;
    }
    
    public Fraction(int n, int d) throws Exception {
        if(d > 0) {
            numerator = BigInteger.valueOf(n);
            denominator = BigInteger.valueOf(d);
        } else if(d < 0) {
            numerator = BigInteger.valueOf(-n);
            denominator = BigInteger.valueOf(-d);
        } else {
            throw new Exception(String.format("### the denominator must not be zero - %d/%d", n, d));
        }
    }
    
    public static Fraction parse(int i) throws Exception {
        return new Fraction(i, 1);
    }
    
    public static Fraction parse(String d) throws Exception {
        String[] str = d.split("\\.");
        BigInteger nu = new BigInteger(str[0]+str[1]);
        BigInteger de = BigInteger.valueOf((int)Math.pow(10, str[1].length()));
        Fraction f = new Fraction(nu, de);
        f.reduce();
        return f;
    }
    
    public String toString() {
        if(denominator.abs().equals(BigInteger.valueOf(1)))
            return String.format("%s", numerator.multiply(denominator).toString());
        else 
            return String.format("%s/%s", numerator.toString(), denominator.toString());
    }
    
    public double toDecimal() {
        return numerator.doubleValue() / denominator.doubleValue();
    }
    
    public void update(Fraction f) {
        numerator = f.numerator;
        denominator = f.denominator;
    }
    
    public Fraction getReverse() throws Exception {
        return new Fraction(denominator, numerator);
    }
    
    public Fraction getNegative() throws Exception {
        return new Fraction(numerator.multiply(BigInteger.valueOf(-1)), denominator);
    }
    
    public void negate() {
        numerator = numerator.multiply(BigInteger.valueOf(-1));
    }
    
    private void reduce() {
        boolean negate = false;
        if(numerator.doubleValue() < 0) {
            numerator = numerator.multiply(BigInteger.valueOf(-1));
            negate = true;
        }
        BigInteger divisor = numerator.gcd(denominator);
        if(divisor.doubleValue() > 1) {
            numerator = numerator.divide(divisor);
            denominator= denominator.divide(divisor);
        }
        if(negate)
            numerator = numerator.multiply(BigInteger.valueOf(-1));
    }
    
    public static Fraction add(Fraction fract1, Fraction fract2) throws Exception {
        BigInteger n = fract1.numerator.multiply(fract2.denominator).add(fract2.numerator.multiply(fract1.denominator));
        BigInteger d = fract1.denominator.multiply(fract2.denominator);
        Fraction sum = new Fraction(n, d);
        sum.reduce();
        return sum;
    }

    public static Fraction sub(Fraction fract1, Fraction fract2) throws Exception {
        BigInteger n = fract2.numerator.multiply(BigInteger.valueOf(-1));
        BigInteger d = fract2.denominator;
        Fraction f = new Fraction(n, d);
        return Fraction.add(fract1, fract2);
    }

    public static Fraction mul(Fraction fract1, Fraction fract2) throws Exception {
        BigInteger n = fract1.numerator.multiply(fract2.numerator);
        BigInteger d = fract1.denominator.multiply(fract2.denominator);
        Fraction product = new Fraction(n, d);
        product.reduce();
        return product;
    }

    public static Fraction div(Fraction fract1, Fraction fract2) throws Exception {
        Fraction f = new Fraction(fract2.denominator, fract2.numerator);
        return Fraction.mul(fract1, f);
    }
    
    private BigInteger numerator = BigInteger.valueOf(0);
    private BigInteger denominator = BigInteger.valueOf(1);
    
    // Testing funtion for this part
    public static void main(String[] args) throws Exception {
        Fraction f1 = new Fraction(7, 13);
        Fraction f2 = new Fraction(5, 3);
        Fraction f3 = new Fraction(5, 1);
        System.out.println(String.format("5/1 = %s", f3.toDecimal()));
        System.out.println(String.format("7/13 + 5/3 = %s", (Fraction.add(f1, f2)).toString()));
        System.out.println(String.format("7/13 - 5/3 = %s", (Fraction.sub(f1, f2)).toString()));
        System.out.println(String.format("7/13 * 5/3 = %s", (Fraction.mul(f1, f2)).toString()));
        System.out.println(String.format("7/13 / 5/3 = %s", (Fraction.div(f1, f2)).toString()));
        System.out.println(String.format("-7/13 = %s", f1.getNegative().toString()));
        System.out.println(String.format("1/(7/13) = %s", f1.getReverse().toString()));
        System.out.println(String.format("0.25 = %s", (Fraction.parse("0.25")).toString()));
        System.out.println(String.format("0.667 = %s", (Fraction.parse("0.667")).toString()));
    }
}
