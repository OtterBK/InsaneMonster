package insanemonster.insanemonster;

public class Utility {

    //min ~ max 중 값 1개 반환
    public static int getRandom(int min, int max) {
        return (int)(Math.random() * (max - min + 1) + min);
    }

}
