package tk.burdukowsky.e_shop;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Android Studio
 * User: STANISLAV
 * Date: 04 Март 2017 15:17
 */

class XMLHelper {

    // пространства имен не используются
    private final static String ns = null;

    // названия узлов
    private final static String ROOT_NODE = "products";
    private final static String ITEM_NODE = "product";
    private final static String ID_NODE = "id";
    private final static String NAME_NODE = "name";
    private final static String COST_NODE = "cost";

    // получает товары из файла
    static Map<Integer, Product> getProductsByFile(File file) throws IOException, XmlPullParserException {
        return parse(new FileInputStream(file));
    }

    // получает товары из InputStream
    private static Map<Integer, Product> parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return getProducts(parser);
        } finally {
            try {
                in.close();
            } catch (Exception ignored) {
            }
        }
    }

    // получает товары с помощью парсера
    private static Map<Integer, Product> getProducts(XmlPullParser parser) throws XmlPullParserException, IOException {
        Map<Integer, Product> products = new HashMap<>();

        parser.require(XmlPullParser.START_TAG, ns, ROOT_NODE);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            if (name.equals(ITEM_NODE)) {
                Product p = getProduct(parser);
                products.put(p.getId(), p);
            } else {
                skip(parser);
            }
        }
        return products;
    }

    // получает товар из конкретного узла
    private static Product getProduct(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, ITEM_NODE);
        Integer productId = null;
        String productName = null;
        Integer productCost = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            switch (name) {
                case ID_NODE:
                    productId = Integer.valueOf(getNodeValue(parser, ID_NODE));
                    break;
                case NAME_NODE:
                    productName = getNodeValue(parser, NAME_NODE);
                    break;
                case COST_NODE:
                    productCost = Integer.valueOf(getNodeValue(parser, COST_NODE));
                    break;
                default:
                    skip(parser);
                    break;
            }
        }
        return new Product(productId, productName, productCost);
    }

    // получает значение конкретного узла
    private static String getNodeValue(XmlPullParser parser, String nodeName) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, nodeName);
        String value = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, nodeName);
        return value;
    }

    // извлечение значения конкретного узла
    private static String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    // пропускает "не товары" и не интересующие поля товаров, если они есть
    private static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
