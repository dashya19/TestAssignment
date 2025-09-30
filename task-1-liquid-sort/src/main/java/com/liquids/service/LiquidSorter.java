package com.liquids.service;

import com.liquids.model.Move;
import com.liquids.model.TubeState;

import java.util.*;

// Класс для поиска решения (алгоритм)
public class LiquidSorter {
    private final int maxSearchDepth; // макс глубина поиска в глубину
    private final Set<String> visitedStates = new HashSet<>(); // множество посещенных состояний (для избежания циклов)
    private final List<Move> solutionMoves = new ArrayList<>(); // список ходов решения
    private int statesEvaluated = 0; // счетчик оцененных состояний
    private final int MAX_STATES_TO_EVALUATE = 1000000; // макс колво состояний для оценки (ограничение)

    public LiquidSorter(int maxDepth) {
        this.maxSearchDepth = maxDepth;
    }

    public Optional<List<Move>> solve(TubeState start) {
        visitedStates.clear(); // очистка множества посещенных состояний
        solutionMoves.clear(); // очистка списка ходов решения
        statesEvaluated = 0; // сброс счетчика состояний

        System.out.println("---НАЧАЛО ПОИСКА РЕШЕНИЯ---");

        // Итеративное углубление - поиск с постепенным увеличением глубины
        for (int depthLimit = 1; depthLimit <= maxSearchDepth; depthLimit++) {
            visitedStates.clear();
            solutionMoves.clear();
            statesEvaluated = 0;
            System.out.printf("┌─ Проверка глубины: %d%n", depthLimit);

            // Запуск поиска в глубину с текущим ограничением глубины
            boolean found = dfs(start, depthLimit);
            if (found) {
                System.out.println("└─ Решение найдено на данной глубине!");
                return Optional.of(new ArrayList<>(solutionMoves));
            }

            // Проверка достижения лимита состояний
            if (statesEvaluated >= MAX_STATES_TO_EVALUATE) {
                System.out.printf("└─ Достигнут лимит состояний на глубине %d%n", depthLimit);
                break;
            }
            System.out.printf("└─ Решение на глубине %d не найдено%n", depthLimit);
        }
        return Optional.empty();
    }

    // Метод поиска в глубину
    private boolean dfs(TubeState state, int remainingDepth) {
        statesEvaluated++;
        // Периодический вывод прогресса
        if (statesEvaluated % 10000 == 0) {
            System.out.printf("  ├─ Проверено состояний: %d, глубина: %d%n", statesEvaluated, remainingDepth);
        }

        if (statesEvaluated > MAX_STATES_TO_EVALUATE) return false;
        if (state.isSolved()) return true; // проверка, является ли состояние решенным
        if (remainingDepth == 0) return false; // проверка достижения максимальной глубины

        String key = state.canonical();
        if (!visitedStates.add(key)) return false; // проверка, не посещали ли уже это состояние

        // Генерация всех возможных валидных ходов
        List<Move> moves = generateValidMoves(state);
        moves.sort((a, b) -> Integer.compare(heuristic(b, state), heuristic(a, state)));

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

    // Эвристическая функция для оценки полезности хода
    private int heuristic(Move move, TubeState state) {
        int score = 0;

        // Бонус за заполнение целевой пробирки до конца
        if (state.getFillHeight(move.targetTube) + move.dropsCount == state.getTubeCapacity()) {
            score += 100;
        }

        // Бонус за опустошение исходной пробирки
        if (state.getFillHeight(move.sourceTube) == move.dropsCount) {
            score += 50;
        }

        // Бонус за переливание большего количества капель
        score += move.dropsCount * 5;

        // Бонус за переливание в пустую пробирку
        if (state.getFillHeight(move.targetTube) == 0) {
            score += 10;
        }

        return score;
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

                // Условия для добавления хода:
                // 1. Можно перелить всю
                if (maxTransfer == colorRun) {
                    validMoves .add(new Move(sourceTube, targetTube, maxTransfer));
                }
                // 2. Можно перелить хотя бы 2 капли
                else if (maxTransfer >= 2) {
                    validMoves .add(new Move(sourceTube, targetTube, maxTransfer));
                }
                // 3. Целевая пробирка полностью пуста
                else if (state.getFreeSpace(targetTube) == state.getTubeCapacity()) {
                    validMoves .add(new Move(sourceTube, targetTube, 1));
                }
            }
        }
        return validMoves;
    }
}