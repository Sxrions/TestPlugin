package dev.jojo.plugin.arena;

public class Arena {
    private String worldName;
    private float[] spawn1;
    private float[] spawn2;
    private boolean isOccupied;

    public Arena(String name, float[] spawn1, float[] spawn2) {
        this.worldName = name;
        this.spawn1 = spawn1;
        this.spawn2 = spawn2;
        this.isOccupied = false;
    }

    public Arena() {
        this.isOccupied = false;
    }

    public String getWorldName() {
        return worldName;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
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

    public void setSpawn2(float[] spawn2) {
        this.spawn2 = spawn2;
    }

    public float[] getSpawn1() {
        return spawn1;
    }

    public float[] getSpawn2() {
        return spawn2;
    }
}
