package NoiseGeneration;

import com.badlogic.gdx.math.Vector3;

public class NoiseGenerator 
{
	public static double[][] get2DNoise(int x, int y)
	{
		SimplexNoise noise = new SimplexNoise(4000, 0.85, 5000);
		
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
}
