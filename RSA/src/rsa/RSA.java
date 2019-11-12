/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rsa;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

/**
 *
 * @author sergi
 */
public class RSA {

    private static final BigInteger ZERO = BigInteger.ZERO;
    private static final BigInteger ONE = BigInteger.ONE;
    private static final BigInteger TWO = new BigInteger("2");
    private static final BigInteger negONE = new BigInteger("-1");

    public static ArrayList<BigInteger> getKey() {
        //mikre lesz szükségünk
        //System.out.println("Get Key start");
        BigInteger p, q, n, phi, e, d;
        p = primeGen();
        System.out.println("p kész: " + p);
        for (;;) {
            q = primeGen();
            if (!q.equals(p)) {
                break;
            }
        }
        System.out.println("q kész: " + q);
        n = p.multiply(q);
        System.out.println("n kész: " + n);
        phi = (p.subtract(ONE)).multiply(q.subtract(ONE));
        System.out.println("phi kész: " + phi);

        //e keresése random
        BigInteger[] eeaEredmény;
        for (;;) {
            e = randBetween(TWO, phi.subtract(ONE));
            eeaEredmény = eeaBigInteger(e, phi);
            if (eeaEredmény[0].equals(ONE)) {       //e akkor jó ha 1<e<phi és (e, phi)==1
                break;
            }
        }
        System.out.println("e kész: " + e);
        if (eeaEredmény[1].compareTo(ONE) > 0 && eeaEredmény[1].compareTo(phi) < 0) {
            d = eeaEredmény[1];
        } else {
            d = eeaEredmény[1].add(phi);
        }
        System.out.println("d kész: " + d);
        ArrayList<BigInteger> key = new ArrayList<>();
        key.add(n);
        key.add(e);
        key.add(d);
        key.add(p);
        key.add(q);
        // System.out.println("Get Key DONE");
        return key;
    }

    public static BigInteger primeGen() {
        // System.out.println("Prime gen start");
        BigInteger a;
        for (;;) {
            //  System.out.print(".");
            Random random = new Random();
            //1024 bitméretű random létrehozása
            a = new BigInteger(1024, random);
            //alapesetek kizására (ha a szám 0, 1, vagy páros)
            if (a.equals(ZERO) || a.equals(ONE) || a.mod(TWO).equals(BigInteger.ZERO)) {
                continue;
            }
            //Miller-Rabin teszt
            // System.out.println("if MRT true");
            if (Miller_RabinTeszt(a) == true) {
                // System.out.println("MRT is true");
                break;
            }
        }
        //  System.out.println("\n Prim gen DONE");
        return a;
    }

    public static Integer[] isKettőhatvány(BigInteger n) { //[true=1; false=0][ kitevő ]
        Integer[] tömb = new Integer[2];
        tömb[1] = 1;
        if (n.equals(ZERO)) {
            tömb[0] = 0;
            tömb[1] = 0;
            return tömb;
        }

        while (!n.equals(ONE)) {
            if (n.mod(TWO) != ZERO) {
                tömb[1] = 0;
                tömb[0] = 0;
                return tömb;
            }
            tömb[1]++;
            n = n.divide(TWO);
        }
        tömb[0] = 1;
        return tömb;
    }

    public static BigInteger quickPow2hatvány(BigInteger x, BigInteger y, BigInteger m) {
        //  System.out.println("QP2 start");
        //x = alap, y = kitevő, m = modulo

        int log2ofY = isKettőhatvány(y)[1];
      //  System.out.println(log2ofY);
        BigInteger res = x.mod(m);
           // System.out.println("res :"+res);
        for (int i = 1; i < log2ofY; i++) {
            res = res.pow(2).mod(m);
           // System.out.println("res :"+res);
        }
        // System.out.println("QP2 DONE");
        return res;
    }

    public static BigInteger modulárisGyorhatványozás(BigInteger x, BigInteger y, BigInteger m) {
        //System.out.println("FME ");
        //x = alap, y = kitevő, m = modulo
        //  return x.modPow(y, m);
        if (isKettőhatvány(y)[0] == 1) {

            //  System.out.println("FME DONE");
            return quickPow2hatvány(x, y, m);
        } else {
           // System.out.println("itt");
            BigInteger összeg = ONE;
            String bin = y.toString(2);
           // System.out.println(bin);
            bin = new StringBuilder(bin).reverse().toString();
            for (int i = 0; i < bin.length(); i++) {
                if (bin.charAt(i) == '1') {
                    BigInteger qp2 = quickPow2hatvány(x, BigInteger.valueOf((int) Math.pow(2, i)), m);
                    összeg = összeg.multiply(qp2);
                }
            }

           // System.out.println(összeg + " " + összeg.mod(m));
            return összeg.mod(m);
            // System.out.println("FME DONE");
        }
    }

    public static BigInteger[] eeaBigInteger(BigInteger a, BigInteger b) { //visszatérési értékek (lnko(a,b) [0]; x [1]; y [2
        // System.out.println("EEA start");
        BigInteger x = a, y = b;
        BigInteger[] result = new BigInteger[3];
        BigInteger x0 = BigInteger.ONE, x1 = BigInteger.ZERO;
        BigInteger y0 = BigInteger.ZERO, y1 = BigInteger.ONE;
        BigInteger counter = BigInteger.ZERO;
        while (true) {

            BigInteger maradék = x.mod(y);
            BigInteger egész_rész = x.divide(y);
            x0 = x1.multiply(egész_rész).add(x0);
            y0 = y1.multiply(egész_rész).add(y0);

            counter = counter.add(BigInteger.ONE);
            if (maradék.equals(BigInteger.ZERO)) {
                result[0] = y;
                result[1] = negONE.pow(counter.intValue()).multiply(x1);
                result[2] = negONE.pow(counter.intValue() + 1).multiply(y1);
                //      System.out.println("EEA DONE");
                return result;
            }
            x = maradék;
            maradék = y.mod(x);
            egész_rész = y.divide(x);
            x1 = x0.multiply(egész_rész).add(x1);
            y1 = y0.multiply(egész_rész).add(y1);
            counter = counter.add(BigInteger.ONE);
            if (maradék.equals(BigInteger.ZERO)) {
                result[0] = x;
                result[1] = negONE.pow(counter.intValue()).multiply(x0);
                result[2] = negONE.pow(counter.intValue() + 1).multiply(y0);

                //    System.out.println("EEA DONE");
                return result;
            }
            y = maradék;

        }
    }

    public static BigInteger quickPow(BigInteger x, BigInteger y, BigInteger m) {
        //x = alap, y = kitevő, m = modulo
        // kitevő bináris stringgé alakítása
        String bin = y.toString(2);
        //árirás visszafele
        bin = new StringBuilder(bin).reverse().toString();
        BigInteger result = ONE, temp = x;
        if (bin.charAt(0) == '1') {
            result = temp;
        }
        //kettővel szorozzuk a meglévő alapot mod m
        for (int i = 1; i < bin.length(); i++) {
            temp = (temp.pow(2)).mod(m);
            if (bin.charAt(i) == '1') {
                //ha a kitevő stringben éppen 1-es jön az i-edik helyen akkor az adott alapot az eredményhez szorozzuk
                result = result.multiply(temp).mod(m);
            }
        }
        return result.mod(m);
    }

    public static boolean Miller_RabinTeszt(BigInteger szam) {
        //  System.out.println("Miller Rabin Test start");
        BigInteger n = szam;

        int s = 0;
        BigInteger d = n.subtract(BigInteger.ONE);
        while (d.mod(TWO).equals(BigInteger.ZERO)) {
            d = d.divide(TWO);
            s++;
        }
        // generálunk 3 db "a" értéket és ha mindegyikre igaz lesz hogy a szám prím, akkor az n prim
        ArrayList<BigInteger> háromDb_a = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            //  System.out.println("call rB with 2 and " + n.subtract(ONE));
            BigInteger random_a = randBetween(TWO, n.subtract(ONE));
            //  System.out.println("call end");
            BigInteger[] eeaEredmény = eeaBigInteger(n, random_a);
            if (eeaEredmény[0].equals(ONE)) {
                if (!háromDb_a.contains(random_a)) {
                    háromDb_a.add(random_a);
                }
            } else {
                i--;
            }
        }
        boolean minden_prim = true;
        for (int i = 0; i < háromDb_a.size(); i++) {
            if (!minden_prim) {
                break;
            }
            minden_prim = false;
            BigInteger hatvany = quickPow(háromDb_a.get(i), d, n);
            if (hatvany.equals(ONE)) {
                minden_prim = true;

            } else {
                for (int r = 0; r < s; r++) {
                    BigInteger kitevo = d.multiply(TWO.pow(r));
                    hatvany = quickPow(háromDb_a.get(i), kitevo, n);
                    hatvany = hatvany.subtract(n);
                    if (hatvany.compareTo(new BigInteger("-1")) == 0) {
                        minden_prim = true;
                        break;
                    }
                }
            }
        }

        //  System.out.println("Miller Rabin Test DONE");
        if (minden_prim) {
            return true;
        } else {
            return false;
        }
    }

    private static BigInteger randBetween(BigInteger min, BigInteger max) {
        // System.out.println("Rand Between start");
        Random random = new Random();
        BigInteger x;
        do {
            //"top" bitszĂˇmĂş random lĂ©trehozĂˇsa
            x = new BigInteger(max.bitLength(), random);
        } while (x.compareTo(min) < 0 || x.compareTo(max) > 0);

        // System.out.println("Rand Between DONE");
        return x;
    }

    public static BigInteger encode(BigInteger m, BigInteger e, BigInteger n) {
        return quickPow(m, e, n);
    }

    public static BigInteger decode(BigInteger c, BigInteger d, BigInteger n) {
        return quickPow(c, d, n);
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        //kulcs létrehozása
        System.out.println("Begin");
        ArrayList<BigInteger> key = getKey(); //n, e, d, p, q

        System.out.print("Kérem a kódolandó számot: ");
        BigInteger string = new BigInteger(sc.nextLine());
        System.out.println("Mit kódolunk: " + string);

        BigInteger m = encode(string, key.get(1), key.get(0));
        System.out.println("KĂłdolva: " + m);

        m = decode(m, key.get(2), key.get(0));
        System.out.println("visszakódolva: " + m);

        
//   BigInteger c = quickPow(new BigInteger("165"), new BigInteger("3"), new BigInteger("253"));
//        System.out.println("encode: c="+c);
//        System.out.println(quickPow(c, new BigInteger("147"), new BigInteger("253")));
    }

}
