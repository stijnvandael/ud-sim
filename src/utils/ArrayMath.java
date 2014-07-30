package utils;

import java.lang.reflect.Array;

public class ArrayMath {

    public static long[] sumArray(long[] array1, long[] array2) {
        long[] newArray = new long[Math.max(array1.length, array2.length)];
        for(int i=0; i < array1.length; i++) {
            newArray[i] = newArray[i] + array1[i];
        }
        for(int i=0; i < array2.length; i++) {
            newArray[i] = newArray[i] + array2[i];
        }
        return newArray;
    }

	public static long[] sumAccArray(long[] array1, long[] array2) {
		if(array1.length == 0) {
			return array2;
		}
		if(array2.length == 0) {
			return array1;
		}
		int length = Math.max(array1.length, array2.length);
		long[] newArray = new long[length];
		for(int i=0; i < length; i++) {
			if(i < array1.length) {
				newArray[i] = newArray[i] + array1[i];	
			}else{
				newArray[i] = newArray[i] + array1[array1.length-1];
			}
			if(i < array2.length) {
				newArray[i] = newArray[i] + array2[i];	
			}else{
				newArray[i] = newArray[i] + array2[array2.length-1];
			}
		}
		return newArray;
	}
	
	public static boolean isEqual(long[] array1, long[] array2) {
		if(array1.length == array2.length) {
			for(int i = 0; i < array1.length; i++) {
				if(array1[i] != array2[i]) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	public static long[] minusArray(long[] array1, long[] array2) {
		long[] newArray = new long[Math.max(array1.length, array2.length)];
		for(int i=0; i < array1.length; i++) {
			newArray[i] = newArray[i] + array1[i];
		}
		for(int i=0; i < array2.length; i++) {
			newArray[i] = newArray[i] - array2[i];
		}
		return newArray;
	}
	
	public static long[] diff(long[] array) {
		long[] newArray = new long[array.length-1];
		for(int i=0; i < array.length-1; i++) {
			newArray[i] = array[i] - array[i+1];
		}
		return newArray;
	}
	
	public static long[] multiply(long[] array, long n) {
		long[] newArray = new long[array.length];
		for(int i=0; i < array.length; i++) {
			newArray[i] = array[i]*n;
		}
		return newArray;
	}
	
	public static long totalSum(long[] array) {
		long totalSum= 0;
		for(int i=0; i < array.length; i++) {
			totalSum = totalSum + array[i];
		}
		return totalSum;
	}

	public static void show(long[] currentFlexPath) {
		for(int i = 0; i < currentFlexPath.length; i++) {
			System.out.print(currentFlexPath[i] + "|");
		}
		System.out.println();
	}
	
	public static String[] toStringArray(long[] input, String delimiter) {
		String[] output = new String[input.length];
		for(int i = 0; i < input.length; i++){
			output[i] = String.valueOf(input[i] + delimiter);
		}
		return output;
	}
	
	public static String toString(long[] input, String delimiter) {
		StringBuffer output = new StringBuffer("[");
		for(int i = 0; i < input.length; i++){
			output.append(String.valueOf(input[i] + delimiter));
		}
		output.append("]");
		return output.toString();
	}
	
	public static <T> T[] concat(T[]... arrays) {
	    int totalLen = 0;
	    for (T[] arr: arrays) {
	        totalLen += arr.length;
	    }
	    T[] all = (T[])Array.newInstance(
	        arrays.getClass().getComponentType().getComponentType(), totalLen);
	    int copied = 0;
	    for (T[] arr: arrays) {
	        System.arraycopy(arr, 0, all, copied, arr.length);
	        copied += arr.length;
	    }
	    return all;
	}
	
	public static boolean compare(long[] array1, long[] array2) {
        boolean b = true;
        if (array1 != null && array2 != null){
          if (array1.length != array2.length)
              b = false;
          else
              for (int i = 0; i < array2.length; i++) {
                  if (array2[i] != array1[i]) {
                      b = false;    
                  }                 
            }
        }else{
          b = false;
        }
        return b;
    }
	
	
	public static void printArray(double[] array) {
		System.out.print("[");
		for(int i = 0; i < array.length; i++) {
			System.out.print(array[i] + ",");
		}
		System.out.println("]");
	}
	
	public static void printArray(int[] array) {
		System.out.print("[");
		for(int i = 0; i < array.length; i++) {
			System.out.print(array[i] + ",");
		}
		System.out.println("]");
	}
	
	public static double[] convertToDoubles(long[] input)
	{
	    if (input == null)
	    {
	        return null; // Or throw an exception - your choice
	    }
	    double[] output = new double[input.length];
	    for (int i = 0; i < input.length; i++)
	    {
	        output[i] = input[i];
	    }
	    return output;
	}
	
	public static long[] convertToLongs(double[] input)
	{
	    if (input == null)
	    {
	        return null; // Or throw an exception - your choice
	    }
	    long[] output = new long[input.length];
	    for (int i = 0; i < input.length; i++)
	    {
	        output[i] = (long) input[i];
	    }
	    return output;
	}
	
	public static String toText(double[] array) {
		StringBuffer s = new StringBuffer();
		s.append("[");
		for(int i = 0; i < array.length; i++) {
			s.append(array[i] + ",");
		}
		s.append("]");
		return s.toString();
	}

	public static String toText(int[] array) {
		StringBuffer s = new StringBuffer();
		s.append("[");
		for(int i = 0; i < array.length; i++) {
			s.append(array[i] + ",");
		}
		s.append("]");
		return s.toString();
	}

    
}