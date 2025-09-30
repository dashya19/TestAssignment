package com.liquids.model;

// Класс для представления хода
public class Move {
    public final int sourceTube; // пробирка A
    public final int targetTube; // пробирка B
    public final int dropsCount; // капли

    public Move(int sourceTube, int targetTube, int dropsCount) {
        this.sourceTube = sourceTube;
        this.targetTube = targetTube;
        this.dropsCount = dropsCount;
    }

    @Override
    public String toString() {
        return String.format("Из пробирки %2d в пробирку %2d перелито %d капель", sourceTube, targetTube, dropsCount);
    }
}
