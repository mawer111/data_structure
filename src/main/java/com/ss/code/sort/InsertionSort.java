package com.ss.code.sort;

import com.ss.code.PrintUtils;

public class InsertionSort {

    public static void main(String[] args) {
        InsertionSort insertionSort = new InsertionSort();
        PrintUtils.printArray(insertionSort.sort(new int[]{1, 2, 10, 6, 3}));
    }

    public int[] sort(int[] origin) {
        for (int i = 1; i < origin.length; i++) {
            int c = origin[i];
            int j = i - 1;
            while (j > 0 && origin[j] > c) {
                //exchange
                origin[j + 1] = origin[j];
                j--;
            }
            origin[j + 1] = c;
        }
        return origin;
    }



}
