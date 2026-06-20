package me.dedushka.monkeyJailGradle.Classes;

import java.util.Objects;

public class BlockPosClass {
    public int x;
    public int y;
    public int z;
    public BlockPosClass(){}
    public BlockPosClass(int x, int y, int z){
        this.x=x;
        this.y=y;
        this.z=z;
    }
    @Override
    public String toString() {
        return "BlockPos: x = " + x + ", y = " + y + ", z = "+z;
    }


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BlockPosClass)) return false;
        BlockPosClass other = (BlockPosClass) o;
        return x == other.x && y == other.y && z == other.z;
    }
    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }
}
