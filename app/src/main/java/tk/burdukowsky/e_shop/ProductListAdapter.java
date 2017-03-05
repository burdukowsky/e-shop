package tk.burdukowsky.e_shop;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Map;

/**
 * Created by Android Studio
 * User: STANISLAV
 * Date: 02 Март 2017 20:12
 */

class ProductListAdapter extends BaseAdapter {

    // поля
    private LayoutInflater layoutInflater;
    private Map<Integer, Product> mData;
    private Integer[] mKeys;
    private Context mContext;
    private TextView textViewSum;

    // конструткор
    ProductListAdapter(Context context, Map<Integer, Product> data, TextView textViewSum) {
        this.mData = data;
        this.mKeys = mData.keySet().toArray(new Integer[data.size()]);
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.textViewSum = textViewSum;
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(mKeys[position]);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;

        if (view == null) {
            view = layoutInflater.inflate(R.layout.product, parent, false);
        }

        final Product product = getProduct(position);

        ((TextView) view.findViewById(R.id.textViewProductName)).setText(product.getName());
        ((TextView) view.findViewById(R.id.textViewProductCost)).setText(mContext.getString(R.string.unit_price, product.getCost()));

        final TextView textViewProductTotal = (TextView) view.findViewById(R.id.textViewProductTotal);
        textViewProductTotal.setText(mContext.getString(R.string.product_total, product.getCost()));

        final SeekBar seekBarProductCount = (SeekBar) view.findViewById(R.id.seekBarProductCount);

        // слушатель для seekBar
        seekBarProductCount.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewProductTotal.setText(mContext.getString(R.string.product_total, progress * product.getCost()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // нажатие на кнопку "В корзину"
        Button buttonAddToCart = (Button) view.findViewById(R.id.buttonAddToCart);
        buttonAddToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int sum = MainActivity.addToCart(product.getId(), seekBarProductCount.getProgress());
                textViewSum.setText(mContext.getString(R.string.sum, sum));
                seekBarProductCount.setProgress(1);
            }
        });

        return view;
    }

    // получает товар по позиции
    private Product getProduct(int position) {
        return ((Product) getItem(position));
    }
}
