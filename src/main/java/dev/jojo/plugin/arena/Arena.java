package dev.jojo.plugin.arena;

public class Arena {
    private String name;
    private float[] spawn1;
    private float[] orient1;
    private float[] spawn2;
    private float[] orient2;
    private boolean isOccupied;

    public Arena(String name, float[] spawn1, float[] orient1, float[] spawn2, float[] orient2) {
        this.name = name;
        this.spawn1 = spawn1;
        this.orient1 = orient1;
        this.spawn2 = spawn2;
        this.orient2 = orient2;
        this.isOccupied = false;
    }

    public Arena() {
        this.isOccupied = false;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOccupied(boolean occupied) {
        isOccupied = occupied;
    }

    public boolean isOccupied() {
        return isOccupied;
    }

    public void setSpawn1(float[] spawn1) {
        this.spawn1 = spawn1;
    }

    public void setOrient1(float[] orient1) {
        this.orient1 = orient1;
    }

    public void setSpawn2(float[] spawn2) {
        this.spawn2 = spawn2;
    }

    public void setOrient2(float[] orient2) {
        this.orient2 = orient2;
    }

    public float[] getSpawn1() {
        return spawn1;
    }

    public float[] getOrient1() {
        return orient1;
    }

    public float[] getSpawn2() {
        return spawn2;
    }

    public float[] getOrient2() {
        return orient2;
    }
}
