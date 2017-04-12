import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by X on 02.04.2017.
 */
public class Main {

    private static int[] L, R;
    private static boolean cipher = false;
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        int choose = -1;

        while (choose != 0) {
            createMenu();

            System.out.print(ANSI_GREEN + "Choose option: " + ANSI_RESET);
            try {
                choose = sc.nextInt();
                sc.nextLine();
            } catch (Exception e) {
                System.out.println("Choose number !");
                sc.nextLine();
                choose = -1;

            }

            switch (choose) {
                case 1:
                    System.out.println("Podaj 16 znaków w systemie szesnastkowym: ");
                    //String input = "0123456789ABCDEF";
                    String input = sc.nextLine();
                    System.out.println("Podaj 16 znaków KLUCZA w systemie szesnastkowym: ");
                    //String key = "133457799BBCDFF1";
                    String key = sc.nextLine();
                    cipher = true;

                    DES(input, key);
                    break;

                case 2:
                    System.out.println("Podaj 16 znaków w systemie szesnastkowym: ");
                    //String input = "0123456789ABCDEF";
                    String inputDecipher = sc.nextLine();
                    System.out.println("Podaj 16 znaków KLUCZA w systemie szesnastkowym: ");
                    //String key = "133457799BBCDFF1";
                    String keyDecipher = sc.nextLine();
                    cipher = false;
                    DES(inputDecipher, keyDecipher);
                    break;
                case 3:
                    cipher=true;
                    criyptingBinaryFile();

                    break;
                case 4:
                    cipher=false;
                    criyptingBinaryFile();
                    break;
            }


        }


    }


    private static void criyptingBinaryFile() throws IOException {
        Scanner sc = new Scanner(System.in);
        System.out.print("File name to encode: ");
        String fileName = sc.nextLine();

        System.out.print("Result file name: ");
        String resultFileName = sc.nextLine();

//                    System.out.println("Podaj 16 znaków (szesnastkowych) klucza: ");
        String keyFronEcryptBinaryFile = "133457799BBCDFF1";

        int[] keyArray1 = new int[keyFronEcryptBinaryFile.length()];


        int[] keyArray;
        keyArray = hexTextToBitArray(keyFronEcryptBinaryFile);
        for(int i=0;i<keyArray.length;i++){
            System.out.print(keyArray[i]);
        }

        File file = new File(fileName);
        byte[] fileData = new byte[(int) file.length()];
        DataInputStream dis = new DataInputStream(new FileInputStream(file));
        dis.readFully(fileData);

        int loops = fileData.length / 8;



        byte[] result = new byte[(int) file.length()];


        int wsk = 0;
        int resultIdx = 0;
        for (int i = 0; i < loops; i++) {
            StringBuilder bits = new StringBuilder(64);
            for (int j = 0; j < 8; j++) {
                bits.append(String.format("%8s", Integer.toBinaryString(fileData[wsk] & 0xFF)).replace(' ', '0'));
                wsk++;
            }

            int[] bitsArray = new int[bits.length()];
            for(int k=0;k<bits.length();k++){
                bitsArray[k] = Character.getNumericValue(bits.charAt(k));
            }

            int[] desArray = DESForBinary(bitsArray, keyArray);
            String desArrayString = Arrays.toString(desArray).replaceAll("\\[|\\]|,|\\s", "");
            for(int l=0;l<8;l++){
                Byte resByte = (byte) (int) Integer.valueOf(desArrayString.substring(l*8,l*8+7), 2);
                result[resultIdx++] = resByte;

            }

        }

        Files.write(Paths.get(resultFileName), result);
        dis.close();
    }

    private static void createMenu() {
        System.out.println();
        System.out.println(ANSI_RED + "1. Encrypt DES ");
        System.out.println("2. Decrypt Des");
        System.out.println("3. Encrypt file with DES ");
        System.out.println("4. Decrypt file with DES " + ANSI_RESET);
    }

    public static void DES(String input, String key) {


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
            mergedKeysMap.put(k, permuted);
        });
        System.out.println();
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


        L = new int[32];
        R = new int[32];
        //wstawienie w tablice L i R wszystkie bity z tablic L0, R0 wygenerowanych na podstawie danych wejściowych (po 32 bity każda)
        for (int i = 0; i < L0.length; i++) {
            L[i] = L0[i];
            R[i] = R0[i];
        }


        System.out.println();
        System.out.print("Przejście przez permutację E i funkcję F");

        if (cipher) {
            for (int i = 1; i <= 16; i++) {
                Sloop(mergedKeysMap, i);
            }
        } else {
            for (int i = 16; i > 0; i--) {
                Sloop(mergedKeysMap, i);
            }
        }


        int[] finalLandRArray = new int[64];
        for (int i = 0; i < 32; i++) {
            finalLandRArray[i] = R[i];
        }
        int wsk = 0;
        for (int i = 32; i < 64; i++) {
            finalLandRArray[i] = L[wsk];
            wsk++;
        }

        System.out.println();
        System.out.print("R16L16:  ");
        System.out.println();
        for (int l = 0; l < finalLandRArray.length; l++) {
            System.out.print(finalLandRArray[l]);
            if (l % 8 == 7 && l != 0) {
                System.out.print(" ");
            }
        }

        int[] finalArrayPermuted = permute(finalLandRArray, Tables.FP);
        System.out.println();
        System.out.print("Finalna tablica:  ");
        System.out.println();
        for (int l = 0; l < finalArrayPermuted.length; l++) {
            System.out.print(finalArrayPermuted[l]);
            if (l % 8 == 7 && l != 0) {
                System.out.print(" ");
            }
        }
        System.out.println();
        if (cipher) {
            System.out.print("Wynik szyfrowania: ");
        } else {
            System.out.print("Wynik deszyfrowania: ");
        }
        System.out.println();
        System.out.print(bitArrayToHexText(finalArrayPermuted));
    }


    public static int[] DESForBinary(int[] inputArray, int[] keyArray) {



        keyArray = permute(keyArray, Tables.PC1);


        int[] c0 = getKeyC0(keyArray);
        int[] d0 = getKeyD0(keyArray);


        Map<String, int[]> subKeysMap = createSubKeys(c0, d0);


        Map<String, int[]> mergedKeysMap = mergeKeys(subKeysMap);
        mergedKeysMap.forEach((k, v) -> {
            int[] permuted = permute(v, Tables.PC2);
            mergedKeysMap.put(k, permuted);
        });

        int[] inputPermutedPC = permute(inputArray, Tables.IP);


        int[] L0 = getL0(inputPermutedPC);
        int[] R0 = getR0(inputPermutedPC);


        L = new int[32];
        R = new int[32];
        //wstawienie w tablice L i R wszystkie bity z tablic L0, R0 wygenerowanych na podstawie danych wejściowych (po 32 bity każda)
        for (int i = 0; i < L0.length; i++) {
            L[i] = L0[i];
            R[i] = R0[i];
        }


        if (cipher) {
            for (int i = 1; i <= 16; i++) {
                SloopForBinary(mergedKeysMap, i);
            }
        } else {
            for (int i = 16; i > 0; i--) {
                SloopForBinary(mergedKeysMap, i);
            }
        }

        int[] finalLandRArray = new int[64];
        for (int i = 0; i < 32; i++) {
            finalLandRArray[i] = R[i];
        }
        int wsk = 0;
        for (int i = 32; i < 64; i++) {
            finalLandRArray[i] = L[wsk];
            wsk++;
        }

        int[] finalArrayPermuted = permute(finalLandRArray, Tables.FP);


        return finalArrayPermuted;

    }

    public static void SloopForBinary(Map<String, int[]> mergedKeysMap, int i) {

        //permutacja E tablic
        //L = permute(L, Tables.E);

        int[] permutedR = permute(R, Tables.E);



        int[] xored_R = new int[permutedR.length];
        int[] fKey = mergedKeysMap.get("K" + (i));


        //xorowanie tablicy R z kluczem
        for (int j = 0; j < permutedR.length; j++) {
            xored_R[j] = (permutedR[j] + fKey[j]) % 2;
        }


        //przetworzenie tablicy R przez funkcję f
        int[] afterF = functionF(xored_R);

        afterF = permute(afterF, Tables.P);


        int[] resultTab = new int[32];
        for (int j = 0; j < afterF.length; j++) {

            resultTab[j] = (afterF[j] + L[j]) % 2;
        }

        System.arraycopy(R, 0, L, 0, L.length);
        R = resultTab;


    }


    public static void Sloop(Map<String, int[]> mergedKeysMap, int i) {

        //permutacja E tablic
        //L = permute(L, Tables.E);

        int[] permutedR = permute(R, Tables.E);

        System.out.println();
        System.out.print("Tablica R po permutacji E");
        System.out.println();
        for (int k = 0; k < permutedR.length; k++) {
            System.out.print(permutedR[k]);
            if (k % 6 == 5 && k != 0) {
                System.out.print(" ");
            }
        }

        int[] xored_R = new int[permutedR.length];
        int[] fKey = mergedKeysMap.get("K" + (i));


        System.out.println();
        System.out.print("Klucz");
        System.out.println();
        for (int k = 0; k < fKey.length; k++) {
            System.out.print(fKey[k]);
            if (k % 6 == 5 && k != 0) {
                System.out.print(" ");
            }
        }


        //xorowanie tablicy R z kluczem
        for (int j = 0; j < permutedR.length; j++) {
            xored_R[j] = (permutedR[j] + fKey[j]) % 2;
        }
        System.out.println();
        System.out.print("Tablica zXORowana z kluczem");
        System.out.println();
        for (int l = 0; l < xored_R.length; l++) {
            System.out.print(xored_R[l]);
            if (l % 6 == 5 && l != 0) {
                System.out.print(" ");
            }
        }

        //przetworzenie tablicy R przez funkcję f
        int[] afterF = functionF(xored_R);
        System.out.println();
        System.out.print("Wynik F: ");
        System.out.println();
        for (int l = 0; l < afterF.length; l++) {
            System.out.print(afterF[l]);
            if (l % 4 == 3 && l != 0) {
                System.out.print(" ");
            }
        }
        System.out.println();
        System.out.print("Wynik F po permutacji P: ");
        System.out.println();
        afterF = permute(afterF, Tables.P);
        for (int l = 0; l < afterF.length; l++) {
            System.out.print(afterF[l]);
            if (l % 4 == 3 && l != 0) {
                System.out.print(" ");
            }
        }


        int[] resultTab = new int[32];
        for (int j = 0; j < afterF.length; j++) {

            resultTab[j] = (afterF[j] + L[j]) % 2;
        }

        System.arraycopy(R, 0, L, 0, L.length);
        R = resultTab;


        System.out.println();
        System.out.print("L:" + i);
        System.out.println();
        for (int l = 0; l < L.length; l++) {
            System.out.print(L[l]);
            if (l % 4 == 3 && l != 0) {
                System.out.print(" ");
            }
        }
        System.out.println();
        System.out.print("R:" + i);
        System.out.println();
        for (int l = 0; l < R.length; l++) {
            System.out.print(R[l]);
            if (l % 4 == 3 && l != 0) {
                System.out.print(" ");
            }
        }

    }

    public static int[] functionF(int[] array) {
        int[] result = new int[32];
        List<int[]> splitedList = splitIntoSixBitsArray(array);
        //8 iteracji po tablicach od S1 do S8 (S0 - S8)
        int wsk = 0;
        for (int i = 0; i < 8; i++) {
            int[] tab = splitedList.get(i);

            String row = String.valueOf(tab[0]) + String.valueOf(tab[5]);
            String col = String.valueOf(tab[1]) + String.valueOf(tab[2]) + String.valueOf(tab[3]) + String.valueOf(tab[4]);
            int rowPos = Integer.parseInt(row, 2) * 16;
            int colPos = Integer.parseInt(col, 2);
            int pos = rowPos + colPos;
            pos = Tables.S[i][pos];

            String bin = Integer.toString(pos, 2);

            while (bin.length() < 4) {
                bin = "0" + bin;
            }

            for (int k = 0; k < bin.length(); k++) {
                result[wsk] = Integer.valueOf(bin.substring(k, k + 1));
                wsk++;
            }


        }
        return result;
    }

    public static List<int[]> splitIntoSixBitsArray(int[] array) {
        List<int[]> list = new ArrayList<>();
        int wsk = 0;

        for (int i = 1; i <= 8; i++) {
            int temp[] = new int[6];
            System.arraycopy(array, (i - 1) * 6, temp, 0, 6);
            list.add(temp);
        }
        return list;
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

    public static String bitArrayToHexText(int[] array) {
        String hexText = "";
        List<int[]> listOfEightsBits = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            listOfEightsBits.add(split(array, i * 8, i * 8 + 8));
        }

        for (int[] list : listOfEightsBits) {
            StringBuilder stringBuilder = new StringBuilder(list.length);
            for (int i = 0; i < list.length; i++) {
                stringBuilder.append(list[i]);
            }
            int res = Integer.valueOf(stringBuilder.toString(), 2);
            hexText += String.format("%02X", res & 0xFFFFF);
        }


        return hexText;
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
