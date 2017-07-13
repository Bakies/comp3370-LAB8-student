package edu.wit.cs.comp3370;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/* Provides a solution to the 0-1 knapsack problem 
 * 
 * Wentworth Institute of Technology
 * COMP 3370
 * Lab Assignment 8
 * 
 */

public class LAB8 {

	/**
 	* Uses dynamic programming to determine the best value to hold in the 0-1 knapsack problem
 	* @return list of best items to put in the knapsack
 	* also sets static best_value to the value of the knapsack items
 	*/
	public static Item[] FindDynamic(Item[] table, final int weight) {
		// vals will be the calculated best value for the knapsack
		int[][] vals = new int[table.length + 1][weight + 1];
		// ptrs will hold the index of the item that got added to the table from or -1 if it did not add the item
		int[][] ptrs = new int[table.length + 1][weight + 1];

		for (int y = 1; y < table.length + 1; y++) {
			for (int x = 1; x < weight + 1; x++) {
				if (table[y - 1].weight > x) {
					vals[y][x] = vals[y - 1][x];
					ptrs[y][x] = -1;
				} else {
					int a = vals[y - 1][x];
					int b = vals[y - 1][x - table[y - 1].weight] + table[y - 1].value;

					if (a >= b) {
						vals[y][x] = a;
						ptrs[y][x] = -1;
					} else {
						vals[y][x] = b;
						ptrs[y][x] = y - 1;
					}
				}
			}
		}

		// Reconstruct
		ArrayList<Item> retItems = new ArrayList<>();
		int x = table.length, y = weight;
		while (x > 0 && y > 0) {
			if (ptrs[x][y] >= 0) {
				retItems.add(table[ptrs[x][y]]);
				y -= table[ptrs[x][y]].weight;
			}
			x--;
		}

		Item[] ret = new Item[retItems.size()];

		for (x = 0; x < retItems.size(); x++)
			ret[x] = retItems.get(retItems.size() - x - 1);

		best_value = vals[table.length][weight];
		return ret;
	}

	/********************************************
	 * 
	 * You shouldn't modify anything past here
	 * 
	 ********************************************/

	// set by calls to Find* methods
	private static int best_value = 0;

	public static class Item {
		public int weight;
		public int value;
		public int index;

		public Item(int w, int v, int i) {
			weight = w;
			value = v;
			index = i;
		}

		@Override
		public String toString() {
			return "(" + weight + "#, $" + value + ")"; 
		}
	}

	// enumerates all subsets of items to find maximum value that fits in knapsack
	public static Item[] FindEnumerate(Item[] table, int weight) {

		if (table.length > 31) {	// bitshift fails for larger sizes
			System.err.println("Problem size too large. Exiting");
			System.exit(0);
		}

		int nCr = 1 << table.length; // bitmask for included items
		int bestSum = -1;
		boolean[] bestUsed = {}; 
		boolean[] used = new boolean[table.length];

		for (int i = 0; i < nCr; i++) {	// test all combinations
			int temp = i;

			for (int j = 0; j < table.length; j++) {
				used[j] = (temp % 2 == 1);
				temp = temp >> 1;
			}

			if (TotalWeight(table, used) <= weight) {
				if (TotalValue(table, used) > bestSum) {
					bestUsed = Arrays.copyOf(used, used.length);
					bestSum = TotalValue(table, used);
				}
			}
		}

		int itemCount = 0;	// count number of items in best result
		for (int i = 0; i < bestUsed.length; i++)
			if (bestUsed[i])
				itemCount++;

		Item[] ret = new Item[itemCount];
		int retIndex = 0;

		for (int i = 0; i < bestUsed.length; i++) {	// construct item list
			if (bestUsed[i]) {
				ret[retIndex] = table[i];
				retIndex++;
			}
		}
		best_value = bestSum;
		return ret;

	}

	// returns total value of all items that are marked true in used array
	private static int TotalValue(Item[] table, boolean[] used) {
		int ret = 0;
		for (int i = 0; i < table.length; i++)
			if (used[i])
				ret += table[i].value;

		return ret;
	}

	// returns total weight of all items that are marked true in used array
	private static int TotalWeight(Item[] table, boolean[] used) {
		int ret = 0;
		for (int i = 0; i < table.length; i++) {
			if (used[i])
				ret += table[i].weight;
		}

		return ret;
	}

	// adds items to the knapsack by picking the next item with the highest
	// value:weight ratio. This could use a max-heap of ratios to run faster, but
	// it runs in n^2 time wrt items because it has to scan every item each time
	// an item is added
	public static Item[] FindGreedy(Item[] table, int weight) {
		boolean[] used = new boolean[table.length];
		int itemCount = 0;

		while (weight > 0) {	// while the knapsack has space
			int bestIndex = GetGreedyBest(table, used, weight);
			if (bestIndex < 0)
				break;
			weight -= table[bestIndex].weight;
			best_value += table[bestIndex].value;
			used[bestIndex] = true;
			itemCount++;
		}

		Item[] ret = new Item[itemCount];
		int retIndex = 0;

		for (int i = 0; i < used.length; i++) { // construct item list
			if (used[i]) {
				ret[retIndex] = table[i];
				retIndex++;
			}
		}

		return ret;
	}

	// finds the available item with the best value:weight ratio that fits in
	// the knapsack
	private static int GetGreedyBest(Item[] table, boolean[] used, int weight) {

		double bestVal = -1;
		int bestIndex = -1;
		for (int i = 0; i < table.length; i++) {
			double ratio = (table[i].value*1.0)/table[i].weight;
			if (!used[i] && (ratio > bestVal) && (weight >= table[i].weight)) {
				bestVal = ratio;
				bestIndex = i;
			}
		}

		return bestIndex;
	}

	public static int getBest() {
		return best_value;
	}

	public static void main(String[] args) {

		ArrayList<Item> tableList = new ArrayList<Item>();
		try (Scanner f = new Scanner(new File("objects/small1"))) {
			int i = 0;
			while (f.hasNextInt())
				tableList.add(new Item(f.nextInt(), f.nextInt(), i++));
		} catch (IOException e) {
			System.err.println("Cannot open file");
			System.exit(0);
		}
		Item[] table = new Item[tableList.size()];
		for (int i = 0; i < tableList.size(); i++)
			table[i] = tableList.get(i);
		final int knapsackSize = 20;
		Item[] ret = null;

		best_value = 0;
		ret = FindDynamic(table, knapsackSize);
		System.out.println("\nDynamic got " + best_value);
		int totWeight = 0;
		if (ret != null)
			for (Item i : ret) {
				System.out.printf("%s ", i);
				totWeight += i.weight;
			}
		System.out.println("\nTotal Weight: " + totWeight);

		best_value = 0;
		ret = FindEnumerate(table, knapsackSize);
		System.out.println("\nEnumerate got " + best_value);
		totWeight = 0;
		for (Item i : ret) {
			System.out.printf("%s ", i);
			totWeight += i.weight;
		}
		System.out.println("\nTotal Weight: " + totWeight);

		best_value = 0;
		FindGreedy(table, knapsackSize);
		System.out.println("\nGreedy got " + best_value);
		totWeight = 0;
		for (Item i : ret) {
			System.out.printf("%s ", i);
			totWeight += i.weight;
		}
		System.out.println("\nTotal Weight: " + totWeight);

		System.exit(0);

		Scanner s = new Scanner(System.in);
		String file1;
		int weight = 0;
		System.out.printf("Enter <objects file> <knapsack weight> <algorithm>, ([d]ynamic programming, [e]numerate, [g]reedy).\n");
		System.out.printf("(e.g: objects/small 10 g)\n");
		file1 = s.next();
		weight = s.nextInt();

		// ArrayList<Item> tableList = new ArrayList<Item>();

		try (Scanner f = new Scanner(new File(file1))) {
			int i = 0;
			while(f.hasNextInt())
				tableList.add(new Item(f.nextInt(), f.nextInt(), i++));
		} catch (IOException e) {
			System.err.println("Cannot open file " + file1 + ". Exiting.");
			System.exit(0);
		}

		String algo = s.next();
		Item[] result = {};

		switch (algo.charAt(0)) {
		case 'd':
			result = FindDynamic(table, weight);
			break;
		case 'e':
			result = FindEnumerate(table, weight);
			break;
		case 'g':
			result = FindGreedy(table, weight);
			break;
		default:
			System.out.println("Invalid algorithm");
			System.exit(0);
			break;
		}

		s.close();

		System.out.printf("Index of included items: ");
		for (int i = 0; i < result.length; i++)
			System.out.printf("%d ", result[i].index);
		System.out.printf("\nBest value: %d\n", best_value);	
	}

}
