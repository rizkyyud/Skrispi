/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package skripsi;

/**
 *
 * @author ASUS
 */
import java.io.File;
import java.util.Random;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import java.util.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.text.DecimalFormat;

public class SkripsiFix {

    public static String[][] dataL; // penyimpanan data latih hasil seleksi pso
    public static String[][] dataT; // penyimpanan data test hasil seleksi pso
    public static double fitG; // penyimpanan nilai fitness gbest terbaik
    public static int[] kls; // penyimpanan kelas dari hasil program
    public static double[] fitPbest;
    public static int[][] Pbest;
    public static int[] Gbest;

    /**
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        File f = new File("C:\\Users\\ASUS\\OneDrive\\Desktop\\testData.xls");//file data "testData.xls" untuk yang 33 fitur dan file data "testData1.xls" untuk yang 5 fitur
        int k = 6;
        int e = 2;
        int partikel = 3;
        double persen = 0.7;
        String[][] data = data(f);
        String[][] dataset = convertNilai(data);
        String[][] normalisasi = normalisasi(dataset);
        String[][] dataLatih = dataLatih(normalisasi, persen); //data full
        String[][] dataTest = dataTest(normalisasi, persen); //data full 
//        print2D(dataset);
        
        int[] pso;
        System.out.println("Seleksi Fitur PSO");
        pso = pso(normalisasi, partikel, 3, 2, persen);
        System.out.println("///////////////////////////////////////////////////////////////");
//        double akurasi = Nwknn(dataLatih, dataTest, k, e);
//
//        System.out.println("Akurasi dari Program = " + akurasi);
    }

    public static String[][] data(File f) throws Exception {
        Workbook wb = Workbook.getWorkbook(f);
        Sheet s = wb.getSheet(0);
        int row = s.getRows();
        int col = s.getColumns();
        String[][] hsl = new String[row][col];

        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                Cell c = s.getCell(j, i);
                hsl[i][j] = c.getContents();
            }
        }
        return hsl;
    }

    public static String[][] convertNilai(String[][] x) {
        String[][] hsl = new String[x.length][x[0].length];
        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < x[i].length; j++) {
                if (x[i][j].equalsIgnoreCase("A")) {
                    hsl[i][j] = "4";
                } else if (x[i][j].equalsIgnoreCase("B")) {
                    hsl[i][j] = "3";
                } else if (x[i][j].equalsIgnoreCase("C")) {
                    hsl[i][j] = "2";
                } else if (x[i][j].equalsIgnoreCase("D")) {
                    hsl[i][j] = "1";
                } else {
                    hsl[i][j] = x[i][j];
                }
            }
        }
        return hsl;
    }

    public static String[][] normalisasi(String[][] x) {
        String[][] hsl = new String[x.length][x[0].length];
        double max = 0, min = 4;
        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < hsl[i].length - 2; j++) {
                if (Double.parseDouble(x[i][j]) > max) {
                    max = Double.parseDouble(x[i][j]);
                }
                if (Double.parseDouble(x[i][j]) < min) {
                    min = Double.parseDouble(x[i][j]);
                }
            }
        }

        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < hsl[i].length; j++) {
                if (j < hsl[i].length - 2) {
                    double a = (Double.parseDouble(x[i][j]) - min) / (max - min);
                    DecimalFormat df = new DecimalFormat("#.###");
                    hsl[i][j] = df.format(a);
                } else {
                    hsl[i][j] = x[i][j];
                }
            }
        }
        return hsl;
    }

    public static String[][] dataLatih(String[][] x, double persen) {
        double panjang = Math.round(persen * x.length);
        int a = (int) panjang;

        String[][] hsl = new String[a][x[0].length];
        for (int i = 0; i < hsl.length; i++) {
            for (int j = 0; j < hsl[i].length; j++) {
                hsl[i][j] = x[i][j];
            }
        }
        return hsl;
    }

    public static String[][] dataTest(String[][] x, double persen) {
        double panjang = Math.round(persen * x.length);
        int a = (int) panjang;
        String[][] hsl = new String[x.length - a][x[0].length];
        for (int i = 0; i < hsl.length; i++) {
            for (int j = 0; j < hsl[i].length; j++) {
                hsl[i][j] = x[a + i][j];
            }
        }
        return hsl;
    }

    public static double Nwknn(String[][] dataLatih, String[][] dataTest, int k, int e) {
        int[] kelas = new int[dataTest.length];
        double count = 0;
        int panjang = dataTest[0].length - 1;

        for (int i = 0; i < dataTest.length; i++) {
//            System.out.println("=============" + "Data Ke- " + i + "=============");
//            System.out.println("Nilai Jarak \t\tKelas");
            double jarak[][] = jarak(dataLatih, dataTest[i]);
//            print2D(jarak);
            sortbyColumn(jarak, 0);
//            System.out.println("Nilai Sorting \t\t");
//            print2D(jarak);

            double bobot[][] = bobot(jarak, k, e);
//            System.out.println("Nilai Bobot");
//            print2D(bobot);
            kelas[i] = skor(jarak, bobot, k);
        }

        for (int i = 0; i < dataTest.length; i++) {
            if (Integer.parseInt(dataTest[i][panjang]) == kelas[i]) {
                count += 1;
            }
        }
        double hsl = count / dataTest.length;

        return hsl;
    }

    public static double[][] jarak(String[][] x, String[] y) {
        double[][] jarak = new double[x.length][2];
        DecimalFormat df = new DecimalFormat("#.###");
        int kelas = x[0].length - 1;
        for (int i = 0; i < jarak.length; i++) {
            double htng = 0;
            for (int j = 0; j < x[i].length - 2; j++) {
                double a = Math.pow(Double.parseDouble(x[i][j]) - Double.parseDouble(y[j]), 2);
                htng += a;
            }

            String a = df.format(Math.sqrt(htng));
            jarak[i][0] = Double.parseDouble(a);
            jarak[i][1] = Double.parseDouble(x[i][kelas]);
        }
        return jarak;
    }

    public static double[][] bobot(double[][] jarak, int k, int e) {
        double[][] hsl = new double[3][2];
        double[] kumpulan = new double[3];
        double min = k;
        for (int j = 0; j < k; j++) {
            if (jarak[j][1] == 1.0) {
                kumpulan[0] += 1;
            } else if (jarak[j][1] == 2.0) {
                kumpulan[1] += 1;
            } else if (jarak[j][1] == 3.0) {
                kumpulan[2] += 1;
            }
        }

        for (int i = 0; i < kumpulan.length; i++) {
            if (kumpulan[i] != 0) {
                if (kumpulan[i] < min) {
                    min = kumpulan[i];
                }
            }
        }

        for (int i = 0; i < hsl.length; i++) {
            if (kumpulan[i] > 0) {
                double b = e;
                double a = Math.pow(kumpulan[i] / min, (1 / b));
                hsl[i][0] = (1 / a);
                hsl[i][1] = kumpulan[i];
            } else {
                hsl[i][0] = 0;
                hsl[i][1] = kumpulan[i];
            }
        }
        return hsl;
    }

    public static int skor(double[][] jarak, double[][] bobot, int k) {
        double[][] hslSkor = new double[bobot.length][2];
        int kelas;
        for (int i = 0; i < hslSkor.length; i++) {
            double count = 0;
            for (int j = 0; j < k; j++) {
                if (i + 1 == jarak[j][1]) {
                    if (bobot[i][1] > 0) {
                        double a = jarak[j][0];
                        count += a;
                    } else {
                        count = 0;
                    }
                }
            }
            hslSkor[i][0] = bobot[i][0] * count;
            hslSkor[i][1] = i + 1;
        }
//        System.out.println("Nilai Skor");
//        print2D(hslSkor);
        kelas = Max(hslSkor);
        return kelas;
    }

    public static int[] pso(String[][] data, int nPartikel, int k, int e, double persen) {
        int[][] partikel;
//        int[][] partikelPercobaan = {{0, 1, 1, 1, 0},
//        {1, 0, 0, 1, 0},
//        {1, 0, 0, 1, 1}};
        int generasi = 10;
        double w = 0.5;
        double c1 = 1;
        double c2 = 1;
        double[] fit = new double[nPartikel];
//        double[] fitPbest = new double[nPartikel];
//        partikel = partikelPercobaan;
        partikel = inisialAwal(data, nPartikel);
        double[][] v = new double[nPartikel][data[0].length - 2];
        int[][] pbest = new int[nPartikel][data.length - 2];
        int[] gbest = new int[data.length - 2];

        for (int i = 0; i < generasi; i++) {
            for (int j = 0; j < nPartikel; j++) {
                fit[j] = fitness(data, k, e, partikel[j], persen);
            }

            if (i == 0) {
                Pbest = partikel;
                pbest = Pbest;
                fitPbest = fit;
                gbest(partikel, fit);
                gbest = Gbest;
            } else {
                updatePbest(partikel, pbest, fit, fitPbest);
                pbest = Pbest;
                gbest(Pbest, fitPbest);
                gbest = Gbest;
                double[][] temp = kecepatan(w, c1, c2, partikel, Pbest, Gbest, v);
                v = temp;
                int[][] tempPosisi = updatePosisi(partikel, v);
                partikel = tempPosisi;
            }
            System.out.println("==========================");
            System.out.println("\nKecepatan Generasi Ke- " + i + "\n");
            print2D(v);
            System.out.println("\nPartikel Generasi Ke- " + i + "\n");
            print2D(partikel);
            System.out.println("\nPbest Generasi Ke- " + i + "\n");
            print2D(pbest);
            System.out.println("\nFitness Pbest\n");
            print1D(fitPbest);
            System.out.println("\nGbest generasi Ke- " + i + "\n");
            print1D(gbest);
            System.out.print("\t " + fitG);
            System.out.println("\n");
        }
        return gbest;
    }

    public static int[][] inisialAwal(String[][] data, int banyakPartikel) {
        int[][] hsl = new int[banyakPartikel][data[0].length - 2];
        Random random = new Random();
        for (int i = 0; i < hsl.length; i++) {
            for (int j = 0; j < hsl[i].length; j++) {
                double x = random.nextDouble();
                hsl[i][j] = (int) Math.round(x);
            }
        }
        return hsl;
    }

    public static double fitness(String[][] data, int k, int e, int[] partikel, double persen) {
        int count = 0;
        DecimalFormat df = new DecimalFormat("#.###");
        ArrayList test = new ArrayList();
        for (int i = 0; i < partikel.length; i++) {
            if (partikel[i] == 1) {
                test.add(i);
            }
        }
        int size = test.size();
        String[][] dataNew = new String[data.length][size + 2];
        for (int i = 0; i < dataNew.length; i++) {
            for (int j = 0; j < dataNew[i].length; j++) {
                if (j < size) {
                    int index = (int) test.get(j);
                    dataNew[i][j] = data[i][index];
                } else {
                    if (j == size) {
                        dataNew[i][j] = data[i][data[i].length - 2];
                    } else {
                        dataNew[i][j] = data[i][data[i].length - 1];
                    }
                }
            }
        }
        String[][] dataLatih = dataLatih(dataNew, persen);
        String[][] dataTest = dataTest(dataNew, persen);
        double hsl = Nwknn(dataLatih, dataTest, k, e);
        dataL = dataLatih;
        dataT = dataTest;
        return hsl;
    }

    public static double[][] kecepatan(double w, double c1, double c2, int[][] partikel, int[][] pbest, int[] gbest, double[][] v0) {
        double r1 = 0.1;
        double r2 = 0.4;
        DecimalFormat df = new DecimalFormat("#.###");
        double[][] v = new double[partikel.length][partikel[0].length];
        for (int i = 0; i < v.length; i++) {
            for (int j = 0; j < v[i].length; j++) {
                double a = (pbest[i][j] - partikel[i][j]);
                double b = (gbest[j] - partikel[i][j]);
                double c = (c2 * (r2 * b));
                String d = df.format(c);
                v[i][j] = Double.parseDouble(d);
            }
        }
        return v;
    }

    public static int[][] updatePosisi(int[][] partikel, double[][] v) {
        int[][] uPosisi = new int[partikel.length][partikel[0].length];
        double sigmoid = 0;
        DecimalFormat df = new DecimalFormat("#.#");
        double[][] sigmoidK = new double[partikel.length][partikel[0].length];
//        double[][] cekR = {{0.8, 0.6, 0.7, 0.2, 0.6},
//        {0.1, 0.4, 0.8, 0.9, 0.5},
//        {0.7, 0.8, 0.3, 0.8, 0.5}};

//        System.out.println("\nSigmoid");
        for (int i = 0; i < partikel.length; i++) {
            for (int j = 0; j < partikel[i].length; j++) {
                double a = (0 - v[i][j]);
                double e = 2;
                double r = Math.random();
                sigmoid = 1 / (1 + (Math.exp(a)));
                sigmoidK[i][j] = Double.parseDouble(df.format(sigmoid));
//                System.out.println(sigmoid);
                if (sigmoid > r) {
                    uPosisi[i][j] = 1;
                } else {
                    uPosisi[i][j] = 0;
                }
            }
        }
        System.out.println("\nSigmoid");
        print2D(sigmoidK);
        return uPosisi;
    }

    public static void updatePbest(int[][] partikel, int[][] pbest, double[] fit, double[] fitnesPbes) {        
        int [][] updatePbest = new int[partikel.length][partikel[0].length];
        double[] fitPbes = new double[fit.length];
        for (int i = 0; i < partikel.length; i++) {
            if (fitnesPbes[i] > fit[i]) {
                updatePbest[i] = pbest[i];
                fitPbes[i] = fitnesPbes[i];
            } else {
                updatePbest[i] = partikel[i];
                fitPbes[i] = fit[i];
            }
        }
        Pbest = updatePbest;
        fitPbest = fitPbes;
    }

    public static void gbest(int[][] partikel, double fitness[]) {
        double fit = 0;
        int index = 0;
        int [] updateGbest = new int[partikel[0].length];
        for (int i = 0; i < fitness.length; i++) {
            if (fitness[i] > fit) {
                fit = fitness[i];
                index = i;
            }
        }
        updateGbest = partikel[index];
        Gbest = updateGbest;
//        Gbest = gbest;
        fitG = fitness[index];
    }

    public static int Max(double[][] x) {
        int kelas = 0;
        double max = 0;
        for (int i = 0; i < x.length; i++) {
            if (x[i][0] > max) {
                double a = x[i][0];
                max = a;
                kelas = i + 1;
            }
        }
        return kelas;
    }

    public static void sortbyColumn(double arr[][], int col) {
        // Using built-in sort function Arrays.sort 
        Arrays.sort(arr, new Comparator<double[]>() {

            @Override
            // Compare values according to columns 
            public int compare(final double[] entry1,
                    final double[] entry2) {

                // To sort in descending order revert  
                // the '>' Operator 
                if (entry1[col] > entry2[col]) {
                    return 1;
                } else if (entry1[col] == entry2[col]) {
                    return 0;
                } else {
                    return -1;
                }

            }
        });  // End of function call sort(). 
    }

    public static void print2D(String[][] x) {
        for (int i = 0; i < x.length; i++) {
            System.out.println("");
            for (int j = 0; j < x[i].length; j++) {
                System.out.print(x[i][j] + "\t");
            }
        }
        System.out.println("\n");
    }

    public static void print2D(double[][] x) {
        for (int i = 0; i < x.length; i++) {
            System.out.println("");
            for (int j = 0; j < x[i].length; j++) {
                System.out.print(x[i][j] + "\t");

            }
        }
        System.out.println("\n");
    }

    public static void print2D(int[][] x) {
        for (int i = 0; i < x.length; i++) {
            System.out.println("");
            for (int j = 0; j < x[i].length; j++) {
                System.out.print(x[i][j] + "\t");

            }
        }
        System.out.println("\n");
    }

    public static void print1D(double[] x) {
        for (int i = 0; i < x.length; i++) {
            System.out.println(x[i]);
        }
    }

    public static void print1D(int[] x) {
        for (int i = 0; i < x.length; i++) {
            System.out.print(x[i]);
        }
    }
}
