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
}
