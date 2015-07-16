package NoiseGeneration;

import java.util.Random;

public class SimplexNoise 
{
    SimplexNoiseOctave[] octaves;
    double[] frequencys;
    double[] amplitudes;

    int largestFeature;
    double persistence;
    int seed;

    public SimplexNoise(int largestFeature, double persistence, int seed)
    {
        this.largestFeature=largestFeature;
        this.persistence=persistence;
        this.seed=seed;

        //recieves a number (eg 128) and calculates what power of 2 it is (eg 2^7)
        int numberOfOctaves=(int)Math.ceil(Math.log10(largestFeature)/Math.log10(2));

        octaves=new SimplexNoiseOctave[numberOfOctaves];
        frequencys=new double[numberOfOctaves];
        amplitudes=new double[numberOfOctaves];

        Random rnd=new Random(seed);

        for(int i=0;i<numberOfOctaves;i++){
            octaves[i]=new SimplexNoiseOctave(rnd.nextInt());

            frequencys[i] = Math.pow(2,i);
            amplitudes[i] = Math.pow(persistence,octaves.length-i);
        }
    }

    public double getNoise(int x, int y){

        double result=0;

        for(int i=0;i<octaves.length;i++){
          //double frequency = Math.pow(2,i);
          //double amplitude = Math.pow(persistence,octaves.length-i);

          result=result+octaves[i].noise(x/frequencys[i], y/frequencys[i])* amplitudes[i];
        }

        return result;
    }

    public double getNoise(int x,int y, int z){

        double result=0;

        for(int i=0;i<octaves.length;i++){
          double frequency = Math.pow(2,i);
          double amplitude = Math.pow(persistence,octaves.length-i);

          result=result+octaves[i].noise(x/frequency, y/frequency,z/frequency)* amplitude;
        }

        return result;
    }
    
    public double[][][] getSimplexNoise()
    {
       SimplexNoise simplexNoise=new SimplexNoise(100,0.1,5000);

       double xStart=0;
       double XEnd=500;
       double yStart=0;
       double yEnd=500;
       double zStart = 0;
       double zEnd = 500;

       int xResolution=16;
       int yResolution=16;
       int zResolution=16;

       double[][][] result = new double[xResolution][yResolution][zResolution];

       // Store the 3D simplex noise in result
       for(int i = 0; i < xResolution; i++)
       {
          for(int j = 0; j < yResolution; j++)
          {
         	 for(int k = 0; k < zResolution; k++)
         	 {
         		 int x = (int)(xStart + i * ((XEnd - xStart) / xResolution));
         		 int y = (int)(yStart + j * ((yEnd - yStart) / yResolution));
         		 int z = (int)(zStart + k * ((zEnd - zStart) / zResolution));
         		 result[i][j][k] = 0.5 * (1 + simplexNoise.getNoise(x, y, z));
         	 }
          }
       }
       
       return result;
   }
} 
