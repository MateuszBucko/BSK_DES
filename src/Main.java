import java.util.*;

/**
 * Created by X on 02.04.2017.
 */
public class Main {


    public static void main(String[] args) {
        // Scanner sc = new Scanner(System.in);
        // System.out.println("Podaj 16 znaków w systemie szesnastkowym: ");
        String input = "0123456789ABCDEF";

        // System.out.println("Podaj 16 znaków KLUCZA w systemie szesnastkowym: ");
        String key = "133457799BBCDFF1";

        //64 bity wejściowe
        int[] inputArray = hexTextToBitArray(input);
        //64 bity klucza
        int[] keyArray = hexTextToBitArray(key);


        System.out.println("Klucz: ");
        for (int i = 0; i < keyArray.length; i++) {
            System.out.print(keyArray[i]);
            if (i % 8 == 7 && i != 0) {
                System.out.print(" ");
            }
        }
        System.out.println();
        System.out.println("Klucz po permutacji PC1: ");
        keyArray = permute(keyArray, Tables.PC1);
        for (int i = 0; i < keyArray.length; i++) {
            System.out.print(keyArray[i]);
            if (i % 7 == 6 && i != 0) {
                System.out.print(" ");
            }
        }

        System.out.println();
        System.out.println("Rozdzielenie kluczy na C0 i D0 - po 28 bitów ");

        System.out.print("C0: ");
        int[] c0 = getKeyC0(keyArray);
        for (int i = 0; i < c0.length; i++) {
            System.out.print(c0[i]);
            if (i % 7 == 6 && i != 0) {
                System.out.print(" ");
            }
        }
        System.out.println();
        System.out.print("D0: ");
        int[] d0 = getKeyD0(keyArray);
        for (int i = 0; i < d0.length; i++) {
            System.out.print(d0[i]);
            if (i % 7 == 6 && i != 0) {
                System.out.print(" ");
            }
        }

        System.out.println();
        System.out.print("Tworzenie 16 subkeys:");
        Map<String, int[]> subKeysMap = createSubKeys(c0, d0);
        subKeysMap.forEach((k, v) -> {
                    System.out.println();
                    System.out.print(k + "  ");
                    for (int i = 0; i < v.length; i++) {
                        System.out.print(v[i]);
                    }
                }
        );


        Map<String, int[]> mergedKeysMap = mergeKeys(subKeysMap);
        mergedKeysMap.forEach((k, v) -> {
            int[] permuted = permute(v, Tables.PC2);
            System.out.println();
            System.out.print(k + "  ");
            for (int i = 0; i < permuted.length; i++) {
                System.out.print(permuted[i]);
            }
        });

        System.out.println("Dane wejściowe: ");
        for (int i = 0; i < inputArray.length; i++) {
            System.out.print(inputArray[i]);
            if (i % 8 == 7 && i != 0) {
                System.out.print(" ");
            }
        }

        System.out.println();
        System.out.print("Dane wejściowe po permutacji IP: ");
        int[] inputPermutedPC = permute(inputArray, Tables.IP);
        for (int i = 0; i < inputPermutedPC.length; i++) {
            System.out.print(inputPermutedPC[i]);
            if (i % 8 == 7 && i != 0) {
                System.out.print(" ");
            }
        }

        System.out.println();
        System.out.print("L0: ");
        int[] L0 = getL0(inputPermutedPC);
        for (int i = 0; i < L0.length; i++) {
            System.out.print(L0[i]);
            if (i % 8 == 7 && i != 0) {
                System.out.print(" ");
            }
        }

        System.out.println();
        System.out.print("R0: ");
        int[] R0 = getR0(inputPermutedPC);
        for (int i = 0; i < R0.length; i++) {
            System.out.print(R0[i]);
            if (i % 8 == 7 && i != 0) {
                System.out.print(" ");
            }
        }

        


    }

    public static int[] hexTextToBitArray(String hexText) {
        int[] array = new int[64];
        for (int i = 0; i < hexText.length(); i++) {
            //zmiana zapisu szesnastkowego na binarny ( 0 - 1 )
            String bin = Integer.toBinaryString(Integer.parseInt(hexText.charAt(i) + "", 16));
            // uzupelnienie zer -> tzn gdy chcemy zamienić np. 1 powinniśmy otrzymać 0001 , a otrzymamy 1 z funkcji powyżej

            while (bin.length() < 4) {
                bin = "0" + bin;
            }
            //wstawienie wartości do tablicy
            for (int w = 0; w < 4; w++) {
                array[(i * 4) + w] = Integer.parseInt(bin.charAt(w) + "");
            }
        }
        return array;
    }

    public static int[] permute(int[] array, byte[] permuteTable) {
        int[] tempTable = new int[permuteTable.length];
        int wsk = 0;
        for (int i = 0; i < permuteTable.length; i++) {
            tempTable[wsk] = array[permuteTable[wsk] - 1];
            wsk++;
        }
        return tempTable;
    }

//    public static int[] permute(int[] array, byte[] permuteTable) {
//        int[] tempTable = new int[permuteTable.length];
//        for (int i = 0; i < array.length; i++) {
//            tempTable[i] = array[permuteTable[i] - 1];
//        }
//        return tempTable;
//    }

    public static int[] split(int[] array, int beginIndex, int endIndex) {
        int[] result = new int[Math.abs(endIndex - beginIndex)];
        int wsk = 0;
        for (int i = beginIndex; i < endIndex; i++) {
            result[wsk] = array[i];
            wsk++;
        }
        return result;
    }

    public static int[] getKeyC0(int[] array) {
        return split(array, 0, 28);
    }

    public static int[] getKeyD0(int[] array) {
        return split(array, 28, 56);
    }

    public static int[] getL0(int[] array) {
        return split(array, 0, 32);
    }

    public static int[] getR0(int[] array) {
        return split(array, 32, 64);
    }

    public static Map<String, int[]> createSubKeys(int[] c0, int[] d0) {
        Map<String, int[]> subkeysMap = new LinkedHashMap<>();
        int[] c = c0;
        int[] d = d0;
        for (int i = 0; i < 16; i++) {
            c = leftShift(c, Tables.rotations[i]);
            subkeysMap.put("c" + (i + 1), c);
            d = leftShift(d, Tables.rotations[i]);
            subkeysMap.put("d" + (i + 1), d);

        }

        return subkeysMap;
    }

    public static int[] leftShift(int[] array, int n) {

        // tablica z wynikiem
        int result[] = new int[array.length];

        //arraycopy(Object src, int srcPos, Object dest, int destPos, int length)
        //Copies an array from the specified source array, beginning at the specified position, to the specified position of the destination array.
        System.arraycopy(array, 0, result, 0, array.length);

        for (int i = 0; i < n; i++) {
            //bierzemy pierwszy bit
            int temp = result[0];
            for (int j = 0; j < array.length - 1; j++) {
                //przesuwamy wszystkie w lewo
                result[j] = result[j + 1];
            }
            //wstawiamy na koniec
            result[array.length - 1] = temp;
        }
        return result;
    }

    public static Map<String, int[]> mergeKeys(Map<String, int[]> map) {
        Map<String, int[]> mergedKeysMap = new LinkedHashMap<>();
        for (int i = 0; i < 16; i++) {
            int[] temp = new int[56];
            int[] c = map.get("c" + (i + 1));
            int[] d = map.get("d" + (i + 1));
            int wsk = 0;
            for (int j = 0; j < 28; j++) {
                temp[wsk] = c[j];
                wsk++;
            }
            for (int k = 0; k < 28; k++) {
                temp[wsk] = d[k];
                wsk++;
            }
            mergedKeysMap.put("K" + (i + 1), temp);
        }

        return mergedKeysMap;
    }

}
