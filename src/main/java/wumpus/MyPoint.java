package wumpus;

public class MyPoint implements Comparable<MyPoint>{


    public Integer getValue() {
        return value;
    }

    private int x,y;
    private Integer value;

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public MyPoint(int x, int y, Integer value){
        this.x = x;
        this.y = y;
        this.value = value;
    }

    public MyPoint(Integer value){
        this.value = value;
    }

    @Override
    public int compareTo(MyPoint o) {
        return this.getValue().compareTo(o.getValue());
    }

    public String toString() {
        return "point: " + getX() + ", " + getY() + ", value: " + getValue();
    }

}