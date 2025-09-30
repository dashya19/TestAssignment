package com.liquids.service;

import com.liquids.model.Move;
import com.liquids.model.TubeState;

import java.util.*;

// Класс для поиска решения (алгоритм)
public class LiquidSorter {
    private final int maxSearchDepth; // макс глубина поиска в глубину
    private final Set<String> visitedStates = new HashSet<>(); // множество посещенных состояний (для избежания циклов)
    private final List<Move> solutionMoves = new ArrayList<>(); // список ходов решения

    public LiquidSorter(int maxDepth) {
        this.maxSearchDepth = maxDepth;
    }

    public Optional<List<Move>> solve(TubeState start) {
        visitedStates.clear(); // очистка множества посещенных состояний
        solutionMoves.clear(); // очистка списка ходов решения

        System.out.println("---НАЧАЛО ПОИСКА РЕШЕНИЯ---");

        // Итеративное углубление - поиск с постепенным увеличением глубины
        for (int depthLimit = 1; depthLimit <= maxSearchDepth; depthLimit++) {
            visitedStates.clear();
            solutionMoves.clear();
            System.out.printf("┌─ Проверка глубины: %d%n", depthLimit);

            // Запуск поиска в глубину с текущим ограничением глубины
            boolean found = dfs(start, depthLimit);
            if (found) {
                System.out.println("└─ Решение найдено на данной глубине!");
                return Optional.of(new ArrayList<>(solutionMoves));
            }
            System.out.printf("└─ Решение на глубине %d не найдено%n", depthLimit);
        }
        return Optional.empty();
    }

    // Метод поиска в глубину
    private boolean dfs(TubeState state, int remainingDepth) {
        if (state.isSolved()) return true; // проверка, является ли состояние решенным
        if (remainingDepth == 0) return false; // проверка достижения максимальной глубины

        String key = state.canonical();
        if (!visitedStates.add(key)) return false; // проверка, не посещали ли уже это состояние

        // Генерация всех возможных валидных ходов
        List<Move> moves = generateValidMoves(state);
        // Перебор всех возможных ходов
        for (Move m : moves) {
            TubeState next = state.apply(m.sourceTube, m.targetTube, m.dropsCount); // применение хода к текущему состоянию
            solutionMoves.add(m); // добавление хода в решение
            boolean found = dfs(next, remainingDepth - 1); // вызов dfs
            if (found) return true; // решение найдено
            solutionMoves.remove(solutionMoves.size() - 1); // откат
        }

        return false;
    }

    // Метод генерации всех допустимых ходов
    private List<Move> generateValidMoves(TubeState state) {
        List<Move> validMoves = new ArrayList<>();
        int tubeCount = state.getTotalTubes();

        // Перебор всех исх пробирок
        for (int sourceTube = 0; sourceTube < tubeCount; sourceTube++) {
            if (state.getFillHeight(sourceTube) == 0) continue;

            int colorRun = state.getTopColorRun(sourceTube);
            int liquidColor = state.getTopColor(sourceTube);

            // Перебор всех целевых пробирок
            for (int targetTube = 0; targetTube < tubeCount; targetTube++) {
                if (sourceTube == targetTube) continue; // нельзя переливать в ту же пробирку
                if (state.getFreeSpace(targetTube) == 0) continue; // пропуск заполненных пробирок

                int targetTopColor = state.getTopColor(targetTube); // верхняя капля цел пробирки
                if (targetTopColor != 0 && targetTopColor != liquidColor) continue; // либо пустая либо тот же цвет

                int maxTransfer = Math.min(colorRun, state.getFreeSpace(targetTube)); // макс колво перелит капель
                validMoves.add(new Move(sourceTube, targetTube, maxTransfer));
            }
        }
        return validMoves;
    }
}