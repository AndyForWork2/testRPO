package ru.iu3.fclient;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;


import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.iu3.fclient.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements TransactionEvents{

    // Used to load the 'fclient' library on application startup.
    static {
        System.loadLibrary("fclient");
        System.loadLibrary("mbedcrypto");
    }

    private ActivityMainBinding binding;
    ActivityResultLauncher activityResultLauncher;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        int res = initRng();
//        byte[] v = randomBytes(10);

        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        // обработка результата
//                        String pin = data.getStringExtra("pin");
//                        Toast.makeText(MainActivity.this, pin, Toast.LENGTH_SHORT).show();
                        pin = data.getStringExtra("pin");
                        synchronized (MainActivity.this) {
                            MainActivity.this.notifyAll();
                        }
                    }
                });



        // Example of a call to a native method
//        TextView tv = binding.sampleText;
//        tv.setText(stringFromJNI());
    }

    //тестовый код
//    public void onButtonClick(View v)
//    {
//        Toast.makeText(this, "Hello", Toast.LENGTH_SHORT).show();
//    }


    //задаем ключ, кодируем hex, декодируем hex и выводим на экран
//    public void onButtonClick(View v)
//    {
//        byte[] key = stringToHex("0123456789ABCDEF0123456789ABCDE0");
//        byte[] enc = encrypt(key, stringToHex("000000000000000102"));
//        byte[] dec = decrypt(key, enc);
//        String s = new String(Hex.encodeHex(dec)).toUpperCase();
//        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
//    }



//    //просто вызов другого активити (возвращенное значение не созранится
//    public void onButtonClick(View v)
//    {
//        Intent it = new Intent(this, PinpadActivity.class);
//        startActivity(it);
//    }

//    public void onButtonClick(View v)
//    {
//        Intent it = new Intent(this, PinpadActivity.class);
////        startActivity(it);
//        activityResultLauncher.launch(it);
//    }

    //обработчик транзакции с созданием потока в Java
//      public void onButtonClick(View v)
//    {
//
//        new Thread(()-> {
//            try {
//                byte[] trd = stringToHex("9F0206000000000100");
//                boolean ok = transaction(trd);
//                runOnUiThread(()-> {
//                    Toast.makeText(MainActivity.this, ok ? "ok" : "failed", Toast.LENGTH_SHORT).show();
//                });
//
//            } catch (Exception ex) {
//                // todo: log error
//            }
//        }).start();
//
//    }

//обработчик транзакции без необходимости выполнять ее в отедльном потоке, созданном в JAVA
//      public void onButtonClick(View v)
//    {
//        byte[] trd = stringToHex("9F0206000000000100");
//        transaction(trd);
//    }


    //при получении инфы о том, что транзакция отработала выводим текст
    @Override
    public void transactionResult(boolean result) {
        runOnUiThread(()-> {
            Toast.makeText(MainActivity.this, result ? "ok" : "failed", Toast.LENGTH_SHORT).show();
        });
    }

    private String pin;

    @Override
    public String enterPin(int ptc, String amount) {
        pin = new String();
        Intent it = new Intent(MainActivity.this, PinpadActivity.class);
        it.putExtra("ptc", ptc);
        it.putExtra("amount", amount);
        synchronized (MainActivity.this) {
            activityResultLauncher.launch(it);
            try {
                MainActivity.this.wait();
            } catch (Exception ex) {
                //todo: log error
            }
        }
        return pin;
    }





    public void onButtonClick(View v)
    {
        testHttpClient();
    }


    //функия для получения title страницы
    protected void testHttpClient()
    {
        new Thread(() -> {
            try {
                HttpURLConnection uc = (HttpURLConnection)
                        (new URL("https://www.wikipedia.org").openConnection());
                InputStream inputStream = uc.getInputStream();
                String html = IOUtils.toString(inputStream);
                String title = getPageTitle(html);
                runOnUiThread(() ->
                {
                    Toast.makeText(this, title, Toast.LENGTH_LONG).show();
                });

            } catch (Exception ex) {
                Log.e("fapptag", "Http client fails", ex);
            }
        }).start();
    }

    //вытягиваем title из html кода (по тупому, поиском подстрок)
//    protected String getPageTitle(String html)
//    {
//        int pos = html.indexOf("<title");
//        String p="not found";
//        if (pos >= 0)
//        {
//            int pos2 = html.indexOf("<", pos + 1);
//            if (pos >= 0)
//                p = html.substring(pos + 7, pos2);
//        }
//        return p;
//    }



    //вытягиваем title из html кода (с использованием регулярок)
    protected String getPageTitle(String html)
    {
        Pattern pattern = Pattern.compile("<title>(.+?)</title>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(html);
        String p;
        if (matcher.find())
            p = matcher.group(1);
        else
            p = "Not found";
        return p;
    }









    //функция перевода из строки в hex
    public static byte[] stringToHex(String s)
    {
        byte[] hex;
        try
        {
            hex = Hex.decodeHex(s.toCharArray());
        }
        catch (DecoderException ex)
        {
            hex = null;
        }
        return hex;
    }



    /**
     * A native method that is implemented by the 'fclient' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
    public static native int initRng();
    public static native byte[] randomBytes(int no);
    public static native byte[] encrypt(byte[] key, byte[] data);

    public static native byte[] decrypt(byte[] key, byte[] data);

    public native boolean transaction(byte[] trd);



}