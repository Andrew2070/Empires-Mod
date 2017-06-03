package com.EmpireMod.Empires.entities.Empire;


public class Alliance implements Comparable<Alliance> {
    private String name;

    public final Empire.Container empiresContainer = new Empire.Container();

    public Alliance(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format("Alliance: {Name: %s}", name);
    }

    /* ----- Comparable ----- */

    @Override
    public int compareTo(Alliance n) { // TODO Flesh this out some more?
        int thisNumberOfEmpires = empiresContainer.size(),
                thatNumberOfEmpires = n.empiresContainer.size();
        if (thisNumberOfEmpires > thatNumberOfEmpires)
            return -1;
        else if (thisNumberOfEmpires == thatNumberOfEmpires)
            return 0;
        else if (thisNumberOfEmpires < thatNumberOfEmpires)
            return 1;

        return -1;
    }
}