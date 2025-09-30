package com.liquids;

import com.liquids.model.Move;
import com.liquids.model.TubeState;
import com.liquids.service.LiquidSorter;

import java.util.List;
import java.util.Optional;

// Главный пакет приложения
public class LiquidsSortingApplication {

    public static void main(String[] args) {
        int[][] initialTubesLiquid = new int[][] {
                { 2, 10, 4, 4 }, // пробирка 0 (верх -> низ)
                { 1, 8, 12, 8 }, // пробирка 1
                {10, 7, 5, 9 }, // пробирка 2
                { 5, 3, 2, 5 }, // пробирка 3
                { 6, 11, 8, 7 }, // пробирка 4
                {12, 12, 1, 2 }, // пробирка 5
                { 4, 7, 8, 11 }, // пробирка 6
                {10, 11, 3, 1 }, // пробирка 7
                {10, 7, 9, 9 }, // пробирка 8
                { 6, 2, 6, 11 }, // пробирка 9
                { 4, 6, 9, 3 }, // пробирка 10
                { 5, 3, 12, 1 }, // пробирка 11
                { 0, 0, 0, 0 }, // пробирка 12 - пустая
                { 0, 0, 0, 0 } // пробирка 13 - пустая
        };

//        int[][] initialTubesLiquid = new int[][] { // простой пример
//                {1, 1, 1, 1},
//                {2, 2, 2, 2},
//                {3, 3, 3, 3},
//                {0, 0, 0, 0}
//        };

        TubeState initialTubeState = TubeState.fromArray(initialTubesLiquid);

        System.out.println("---ИСХОДНОЕ СОСТОЯНИЕ---");
        System.out.println(initialTubeState);

        LiquidSorter liquidSorter = new LiquidSorter(200);
        Optional<List<Move>> sol = liquidSorter.solve(initialTubeState);

        if (sol.isPresent()) {
            System.out.println("\n---РЕШЕНИЕ НАЙДЕНО!---");
            System.out.printf("Количество ходов: %d%n%n", sol.get().size());

            int step = 1;
            for (Move m : sol.get()) {
                System.out.printf("%3d. %s%n", step++, m.toString());
            }

            System.out.println("\n---РЕШЕНИЕ ЗАВЕРШЕНО---");
        } else {
            System.out.println("\n---РЕШЕНИЕ НЕ НАЙДЕНО!---");
            System.out.println("(превышена максимальная глубина поиска)");
        }
    }
}