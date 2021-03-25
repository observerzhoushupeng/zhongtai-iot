import com.zhongtai.mqtt.MqttMain;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class Test2 {

    public static void main(String[] args) {
        String s = "2021-02-09 17:53:56.0";
        System.out.println(s.split("\\.")[0]);
    }
}
