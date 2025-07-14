package com.eranbe.setgame.model;

public class SetCard {
    public enum Shape { DIAMOND, SQUIGGLE, OVAL }
    public enum Color { RED, GREEN, PURPLE }
    public enum Shading { SOLID, STRIPED, OPEN }
    public enum Number { ONE, TWO, THREE }

    private final Shape shape;
    private final Color color;
    private final Shading shading;
    private final Number number;

    private boolean isSelected;

    public SetCard(Shape shape, Color color, Shading shading, Number number) {
        this.shape = shape;
        this.color = color;
        this.shading = shading;
        this.number = number;
        this.isSelected = false;
    }

    // Getters
    public Shape getShape() {
        return shape;
    }

    public Color getColor() {
        return color;
    }

    public Shading getShading() {
        return shading;
    }

    public Number getNumber() {
        return number;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void toggleSelection() {
        isSelected = !isSelected;
    }

    @Override
    public String toString() {
        return "SetCard{" +
                "shape=" + shape +
                ", color=" + color +
                ", shading=" + shading +
                ", number=" + number +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SetCard setCard = (SetCard) o;
        return shape == setCard.shape &&
                color == setCard.color &&
                shading == setCard.shading &&
                number == setCard.number;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(shape, color, shading, number);
    }
}
