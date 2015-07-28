package NoiseGeneration;

public class NoiseGenerator 
{
	public static double[][] get2DNoise(int x, int y)
	{
		SimplexNoise noise = new SimplexNoise(200, 0.25, 5000);
		
		double[][] result = new double[129][129];
		
		for(int i = 0; i < 129; i++)
		{
			for(int j = 0; j < 129; j++)
			{
				result[i][j] = (1 + noise.getNoise(i + x, j + y));
			}
		}
		
		return result;
	}
	
	public static double[][][] get3DNoise(int x, int y, int z)
	{
		SimplexNoise noise = new SimplexNoise(4000, 0.7f, 5000);
		
		double[][][] result = new double[32][32][32];
		
		for(int i = 0; i < 32; i++)
		{
			for(int j = 0; j < 32; j++)
			{
				for(int k = 0; k < 32; k++)
				{
					result[i][j][k] = noise.getNoise(i + x, j + y, k + z);
				}
			}
		}
		
		return result;
	}
}
