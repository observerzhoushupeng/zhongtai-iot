import java.io.InputStream;
import java.util.Random;

public class RandomSort {

    public static void main(String[] args) {
        try{
            InputStream is = RandomSort.class.getClassLoader().getResourceAsStream("mqtt.conf");
            int count = 0;
            while (count == 0) {
                count = is.available();
            }
            byte[] b = new byte[count];
            is.read(b);
            String[] lines = new String(b).split("\n");
            System.out.println(lines.length);
            for(int i=0;i<lines.length;i++){
                System.out.println(lines[i]);
            }
        }catch (Exception e){
            e.printStackTrace();
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

