package com.liquids.model;

import java.util.Arrays;

// Класс для описания состояния всех пробирок в данный момент
public class TubeState {
    private final int[][] tubeContents; // содержимое пробирок
    private final int[] fillHeights; // высота заполнения каждой пробирки
    private final int tubeCapacity; // V - вместимость каждой пробирки
    private final int totalTubes; // N - общее количество пробирок

    public TubeState(int[][] tubeContents, int[] fillHeights) {
        this.totalTubes = tubeContents.length;
        this.tubeCapacity = tubeContents[0].length;
        this.tubeContents = new int[totalTubes][tubeCapacity];
        for (int i = 0; i < totalTubes; i++) {
            System.arraycopy(tubeContents[i], 0, this.tubeContents[i], 0, tubeCapacity);
        }
        this.fillHeights = Arrays.copyOf(fillHeights, fillHeights.length);
    }

    public static TubeState fromArray(int[][] input) {
        int n = input.length;
        int v = input[0].length;
        int[][] tubeContents = new int[n][v];
        int[] fillHeights = new int[n];

        // Обработка каждой пробирки
        for (int i = 0; i < n; i++) {
            int h = 0; // высота заполнения
            boolean seenZero = false; // пустое место
            //Обработка каждого уровня в пробирке
            for (int d = 0; d < v; d++) {
                int color = input[i][d]; // цвет жидкости на текущей позиции
                if (color == 0) {
                    seenZero = true;
                } else {
                    // если после нуля ненулевое значение - ошибка
                    if (seenZero) {
                        throw new IllegalArgumentException(
                                "Неверный ввод: ненулевое значение после нуля в строке " + i + "."
                        );
                    }
                    h++;
                }
            }
            fillHeights[i] = h;
            // заполнение содержимого пробирки - переворот порядка
            for (int d = 0; d < v; d++) {
                tubeContents[i][d] = input[i][v - 1 - d];
            }
        }
        return new TubeState(tubeContents, fillHeights);
    }

    public int getTotalTubes() { return totalTubes; }
    public int getTubeCapacity() { return tubeCapacity; }

    // Получение верхней капли в пробирке
    public int getTopColor(int tubeIndex) {
        if (fillHeights[tubeIndex] == 0) return 0;
        return tubeContents[tubeIndex][fillHeights[tubeIndex]-1];
    }

    public int getFillHeight(int tubeIndex) { return fillHeights[tubeIndex]; }
    public int getFreeSpace(int tubeIndex) { return tubeCapacity - fillHeights[tubeIndex]; }

    // Метод для получения длины последовательности цвета сверху (сколько капель можно перелить за один ход)
    public int getTopColorRun(int i) {
        int h = fillHeights[i]; // высота заполнения пробирки
        if (h == 0) return 0; // пустая пробирка
        int color = tubeContents[i][h-1]; // цвет верхней капли
        int run = 0;
        // Подсчет кодичества подряд идущих капель одного цвета сверху
        for (int k = h-1; k >=0; k--) {
            if (tubeContents[i][k] == color) run++;
            else break;
        }
        return run;
    }

    // Метод применения хода к текущ состоянию
    public TubeState apply(int fromIdx, int toIdx, int amount) {
        int[][] nc = new int[totalTubes][tubeCapacity];
        int[] nh = new int[totalTubes];
        // Копирование текущ состояния
        for (int i = 0; i < totalTubes; i++) {
            System.arraycopy(this.tubeContents[i], 0, nc[i], 0, tubeCapacity);
            nh[i] = this.fillHeights[i];
        }

        int fromHeight = nh[fromIdx]; // высота исходной пробирки
        int color = nc[fromIdx][fromHeight - 1]; // цвет переливаемой жидкости
        // Удаление жидкости из исх пробирки
        for (int k = 0; k < amount; k++) {
            nc[fromIdx][fromHeight - 1 - k] = 0;
        }
        nh[fromIdx] = fromHeight - amount; // обновление высоты исх пробирки

        int toHeight = nh[toIdx]; // высота целевой пробирки
        // Добавление жидкости в цел пробирку
        for (int k = 0; k < amount; k++) {
            nc[toIdx][toHeight + k] = color;
        }
        // Обновление высоты цел пробирки
        nh[toIdx] = toHeight + amount;
        return new TubeState(nc, nh); // новое состояние
    }

    // Проверка, является ли состояние решенным
    public boolean isSolved() {
        // Проверка каждой пробирки
        for (int i = 0; i < totalTubes; i++) {
            int h = fillHeights[i];
            if (h == 0) continue; // пропуск пустой пробирки
            if (h != tubeCapacity) return false; // если пробирка не заполнена полностью - не решено
            int color = tubeContents[i][0]; // цвет первой капли
            // Проверка, если в пробирке все капли одного цвета
            for (int d = 0; d < tubeCapacity; d++) {
                if (tubeContents[i][d] != color) return false;
            }
        }
        return true;
    }
}
