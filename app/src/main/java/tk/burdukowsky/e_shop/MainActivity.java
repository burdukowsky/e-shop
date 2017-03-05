package tk.burdukowsky.e_shop;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // сслыка для скачки файла products.xml
    final static String LINK = "https://docs.google.com/uc?id=0BxK3aDRnWo6kTDBDbWhoMUVyOVU&export=download";

    // ссылки на измененные файлы
    //final static String LINK = "https://docs.google.com/uc?id=0BxK3aDRnWo6kcGlwVEFJaGdtZE0&export=download"; // products_2.xml
    //final static String LINK = "https://docs.google.com/uc?id=0BxK3aDRnWo6kcHJmc2x3WXdWM0E&export=download"; // products_3.xml

    final static int STATUS_SUCCESS = 1;
    final static int STATUS_ERROR = 2;
    final static int STATUS_DATABASE_ERROR = 3;

    // спиннер загрузки
    LinearLayout spinner;
    // listview с товарами
    ListView listViewProducts;
    // адаптер для listview
    ProductListAdapter productListAdapter;
    // textview с суммой
    TextView textViewSum;
    // кнопка купить
    Button buttonBuy;
    // здесь будут храниться товары
    static Map<Integer, Product> products;
    // "итого"
    static int totalSum;
    // здесь будут зраниться пары "id товара"->"количество"
    static Map<Integer, Integer> cart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // инициализация
        spinner = (LinearLayout) findViewById(R.id.linearLayoutLoadingSpinner);
        listViewProducts = (ListView) findViewById(R.id.listViewProducts);
        textViewSum = (TextView) findViewById(R.id.textViewSum);
        buttonBuy = (Button) findViewById(R.id.buttonBuy);
        cart = new HashMap<>();
        // начинаем работу
        new Run(this).execute();
    }

    // нажатие на кнопку "купить"
    public void onButtonBuyClick(View view) {
        // отправка данных приложению
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, getMessageByCart(cart, this));
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
        // обнуление переменных
        totalSum = 0;
        cart.clear();
        textViewSum.setText("");
    }

    // добавление предмета в корзину
    static int addToCart(int productId, int productCount) {
        if (cart.containsKey(productId)) {
            cart.put(productId, cart.get(productId) + productCount);
        } else {
            cart.put(productId, productCount);
        }
        totalSum += getSumByOneBuy(productId, productCount);
        return totalSum;
    }

    // получить сумму по id и количеству
    static int getSumByOneBuy(int productId, int productCount) {
        return products.get(productId).getCost() * productCount;
    }

    // составление сообщения для приложения
    static String getMessageByCart(Map<Integer, Integer> cart, Context context) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append( context.getString(R.string.purchases));
        stringBuilder.append("\n");
        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            int id = entry.getKey();
            int count = entry.getValue();
            stringBuilder.append(products.get(id).getName());
            stringBuilder.append(" x ");
            stringBuilder.append(String.valueOf(count));
            stringBuilder.append(" = ");
            stringBuilder.append(getSumByOneBuy(id, count));
            stringBuilder.append("\n");
        }
        stringBuilder.append(context.getString(R.string.total, totalSum));
        return stringBuilder.toString();
    }

    // работа
    private class Run extends AsyncTask<Void, Void, Integer> {

        private Context mContext;

        Run(Context context) {
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            spinner.setVisibility(View.VISIBLE);
        }

        protected Integer doInBackground(Void... args) {

            boolean needUpdate;
            DatabaseHelper databaseHelper = new DatabaseHelper(mContext);
            try {
                needUpdate = needUpdate();
                if (needUpdate) {
                    products = XMLHelper.getProductsByFile(new File(getFilesDir(), "products.xml"));
                } else {
                    products = databaseHelper.getProducts();
                }
            } catch (IOException | XmlPullParserException e) {
                //e.printStackTrace();
                return STATUS_ERROR;
            }

            productListAdapter = new ProductListAdapter(mContext, products, textViewSum);

            if (needUpdate) {
                if (!databaseHelper.insertIntoProducts(products)) {
                    return STATUS_DATABASE_ERROR;
                }
            }

            return STATUS_SUCCESS;
        }

        @Override
        protected void onPostExecute(Integer result) {
            spinner.setVisibility(View.GONE);
            switch (result) {
                case STATUS_SUCCESS:
                    listViewProducts.setAdapter(productListAdapter);
                    buttonBuy.setEnabled(true);
                    break;
                case STATUS_ERROR:
                    Toast.makeText(mContext, "Ошибка при получении данных", Toast.LENGTH_SHORT).show();
                    break;
                case STATUS_DATABASE_ERROR:
                    listViewProducts.setAdapter(productListAdapter);
                    buttonBuy.setEnabled(true);
                    Toast.makeText(mContext, "Ошибка при записи в базу данных", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

    }

    // сравнивает два потока
    private static boolean isEqual(InputStream i1, InputStream i2)
            throws IOException {

        ReadableByteChannel ch1 = Channels.newChannel(i1);
        ReadableByteChannel ch2 = Channels.newChannel(i2);

        ByteBuffer buf1 = ByteBuffer.allocateDirect(1024);
        ByteBuffer buf2 = ByteBuffer.allocateDirect(1024);

        try {
            while (true) {

                int n1 = ch1.read(buf1);
                int n2 = ch2.read(buf2);

                if (n1 == -1 || n2 == -1) return n1 == n2;

                buf1.flip();
                buf2.flip();

                for (int i = 0; i < Math.min(n1, n2); i++)
                    if (buf1.get() != buf2.get())
                        return false;

                buf1.compact();
                buf2.compact();
            }

        } finally {
            try {
                i1.close();
                i2.close();
            } catch (Exception ignored) {
            }
        }
    }

    // скачивает файл
    private static void downloadUsingStream(String urlStr, File file) throws IOException {
        URL url = new URL(urlStr);
        BufferedInputStream bis = new BufferedInputStream(url.openStream());
        FileOutputStream fis = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        int count;
        while ((count = bis.read(buffer, 0, 1024)) != -1) {
            fis.write(buffer, 0, count);
        }
        fis.close();
        bis.close();
    }

    // сравнивает локальный и удаленный файлы
    // если локального файла нет, скачивает и возвращает true
    // если одинаковые, возвращает false
    // если разные, то скачивает и возвращает true
    public boolean needUpdate() throws IOException {
        File localFile = new File(getFilesDir(), "products.xml");
        if (localFile.exists() && !localFile.isDirectory()) {
            URL url = new URL(LINK);
            URLConnection urlConnection = url.openConnection();
            InputStream remoteFileInputStream = urlConnection.getInputStream();
            InputStream localFileInputStream = new FileInputStream(localFile);
            if (!isEqual(remoteFileInputStream, localFileInputStream)) {
                //Log.d("MY_DEBUG", "Потоки разные, качаем файл");
                downloadUsingStream(LINK, localFile);
                return true;
            }
            //Log.d("MY_DEBUG", "Потоки одинаковые");
            return false;
        }
        //Log.d("MY_DEBUG", "Файл не был найден, качаем");
        downloadUsingStream(LINK, localFile);
        return true;
    }

}
