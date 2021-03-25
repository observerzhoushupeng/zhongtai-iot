import java.util.Random;

public class Test {


    public static void main(String[] args) {
        int[] a1 = new int[280];
        for(int i=1;i<=80;i++){
            a1[i-1] = i;
        }
        for(int i=81;i<=180;i++){
            a1[i-1] = i;
        }
        for(int i=181;i<=280;i++){
            a1[i-1] = i;
        }
        shuffle(a1);
        System.out.println("你的：");
        for(int i=0;i<138;i++){
            System.out.print(a1[i]+"\t");
        }
        System.out.println();
        System.out.println("我的：");
        for(int i=138;i<216;i++){
            System.out.print(a1[i]+"\t");
        }
    }

    //打乱数组
    public static void shuffle(int[] arr) {
        Random mRandom = new Random();
        for (int i = arr.length; i > 0; i--) {
            int rand = mRandom.nextInt(i);
            swap(arr, rand, i - 1);
        }
    }

    //交换两个值
    private static void swap(int[] a, int i, int j) {
        int temp = a[i];
        a[i] = a[j];
        a[j] = temp;
    }
}
