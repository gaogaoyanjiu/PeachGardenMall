package me.zoro.peachgardenmall.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.Transformer;
import com.youth.banner.loader.ImageLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zoro.peachgardenmall.R;
import me.zoro.peachgardenmall.adapter.CommentRecyclerViewAdapter;
import me.zoro.peachgardenmall.datasource.GoodsDatasource;
import me.zoro.peachgardenmall.datasource.GoodsRepository;
import me.zoro.peachgardenmall.datasource.UserDatasource;
import me.zoro.peachgardenmall.datasource.UserRepository;
import me.zoro.peachgardenmall.datasource.domain.Comment;
import me.zoro.peachgardenmall.datasource.domain.Goods;
import me.zoro.peachgardenmall.datasource.domain.UserInfo;
import me.zoro.peachgardenmall.datasource.remote.GoodsRemoteDatasource;
import me.zoro.peachgardenmall.datasource.remote.UserRemoteDatasource;
import me.zoro.peachgardenmall.fragment.HomeFragment;
import me.zoro.peachgardenmall.utils.DensityUtil;
import me.zoro.peachgardenmall.utils.PreferencesUtil;
import me.zoro.peachgardenmall.view.FlexRadioGroup;

/**
 * Created by dengfengdecao on 17/4/10.
 */

public class GoodsDetailActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener, View.OnClickListener, FlexRadioGroup.OnCheckedChangeListener, PopupWindow.OnDismissListener {
    private static final String TAG = "GoodsDetailActivity";
    public static final String ADDRESS_ID_EXTRA = "address_id";
    public static final String GOODS_SPEC_KEY_EXTRA = "goods_speck_key";
    public static final String GOODS_EXTRA = "goods";
    public static final String GOODS_COUNT_EXTRA = "goods_count";
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.banner)
    Banner mBanner;
    @BindView(R.id.tv_goods_name)
    TextView mTvGoodsName;
    @BindView(R.id.tv_goods_remark)
    TextView mTvGoodsRemark;
    @BindView(R.id.tv_price)
    TextView mTvPrice;
    @BindView(R.id.iv_category)
    ImageView mIvCategory;
    @BindView(R.id.tv_select_spec)
    TextView mTvSelectSpec;
    @BindView(R.id.edit_select_spec)
    RelativeLayout mEditSelectSpec;
    @BindView(R.id.tv_promotion_count)
    TextView mTvPromotionCount;
    @BindView(R.id.tv_promotion)
    TextView mTvPromotion;
    @BindView(R.id.edit_promotion)
    RelativeLayout mEditPromotion;
    @BindView(R.id.tv_service)
    TextView mTvService;
    @BindView(R.id.edit_service)
    RelativeLayout mEditService;
    @BindView(R.id.tv_comment_number)
    TextView mTvCommentNumber;
    @BindView(R.id.edit_comment)
    RelativeLayout mEditComment;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.web_view)
    WebView mWebView;
    @BindView(R.id.iv_service)
    ImageView mIvService;
    @BindView(R.id.iv_shopping_cart)
    ImageView mIvShoppingCart;
    @BindView(R.id.iv_collection)
    ImageView mIvCollection;
    @BindView(R.id.tv_purchase)
    TextView mTvPurchase;
    @BindView(R.id.tv_add_to_shopping_cart)
    TextView mTvAddToShoppingCart;

    private PopupWindow mPopupWindow;

    private TextView mTvSpecPrice;
    private TextView mTvCount;
    /**
     * 规格内容
     */
    private TextView mTvGoodSpec;

    /**
     * 库存
     */
    private TextView mTvStock;

    private GoodsRepository mGoodsRepository;


    /**
     * 规格的种类数
     */
    private int mSpecNum;
    /**
     * 保存PopupWindow中的数量
     */
    private String mGoodsCount = "1";
    /**
     * 保存PopupWindow中的价格
     */
    private String mSpecPrice;

    private String mKey = "", mDelimiter = "_";

    // mk1，mk2，升序并已'_'连接成mKey
    private int mk1, mk2;

    /**
     * 该商品对应的评论列表
     */
    private List<Comment> mComments;

    private CommentRecyclerViewAdapter mCommentRecyclerViewAdapter;
    private int mGoodsId;
    private Goods mGoods;
    private UserRepository mUserRepository;
    private UserInfo mUserInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goods_detail);
        ButterKnife.bind(this);

        // 当系统版本为4.4或者4.4以上时可以使用沉浸式状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // 透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            final int statusBarHeight = DensityUtil.getStatusBarHeight(this);
            mToolbar.post(new Runnable() {
                @Override
                public void run() {
                    mToolbar.getLayoutParams().height += statusBarHeight;
                    mToolbar.setPadding(mToolbar.getPaddingLeft(),
                            statusBarHeight + mToolbar.getPaddingTop(),
                            mToolbar.getPaddingRight(),
                            mToolbar.getPaddingBottom());

                }
            });
        }


        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        mToolbar.setOnMenuItemClickListener(this);

        mEditSelectSpec.setOnClickListener(this);
        mEditPromotion.setOnClickListener(this);
        mEditComment.setOnClickListener(this);

        mIvService.setOnClickListener(this);
        mIvShoppingCart.setOnClickListener(this);
        mIvCollection.setOnClickListener(this);
        mTvPurchase.setOnClickListener(this);
        mTvAddToShoppingCart.setOnClickListener(this);

        mUserRepository = UserRepository.getInstance(UserRemoteDatasource.getInstance(
                getApplicationContext()
        ));

        mGoodsRepository = GoodsRepository.getInstance(GoodsRemoteDatasource.getInstance(
                getApplicationContext()
        ));

        mGoodsId = getIntent().getIntExtra(HomeFragment.GOODS_ID_EXTRA, -1);

        mComments = new ArrayList<>();
        mCommentRecyclerViewAdapter = new CommentRecyclerViewAdapter(this, mComments);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mCommentRecyclerViewAdapter);

        fetchUserInfo();
    }


    private void setupBanner(List<String> imagesUrl) {
        // 设置banner样式
        mBanner.setBannerStyle(BannerConfig.NUM_INDICATOR);
        // 设置指示器位置（当banner模式中有指示器时）
        mBanner.setIndicatorGravity(BannerConfig.RIGHT);
        // 设置图片加载器
        mBanner.setImageLoader(new PicassoImageLoader());
        // 设置banner动画效果
        mBanner.setBannerAnimation(Transformer.DepthPage);
        // 设置标题集合（当banner样式有显示title时）
        // mBanner.setBannerTitles(Arrays.asList(mTitles));
        // 设置轮播图片(所有设置参数方法都放在此方法之前执行)
        mBanner.setImages(imagesUrl);
        // banner设置方法全部调用完毕时最后调用
        mBanner.start();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mGoodsId != -1) {
            new FetchGoodsDetailTask().execute();
        }
    }

    /**
     * 获取最新用户信息
     */
    private void fetchUserInfo() {
        if (mUserInfo == null) {
            mUserInfo = PreferencesUtil.getUserInfoFromPref(this);
        }
        if (mUserInfo != null) {
            mUserRepository.setDirty(true);
            new FetchUserInfoTask().execute(mUserInfo.getUserId());
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.goods_detail_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_home:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
                break;
            /*case R.id.action_share:
                msg += "Click share";
                break;*/
        }

        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.edit_select_spec:
                showSpecPopupWindow();
                break;
            // TODO: 17/4/25 选择促销
            case R.id.edit_promotion:
                break;
            // TODO: 17/4/25 查看评论
            case R.id.edit_comment:
                break;
            case R.id.iv_service:
                showServiceInfo();
                break;
            // TODO: 17/4/25 查看购物车
            case R.id.iv_shopping_cart:
                break;
            // TODO: 17/4/25 判断是否已收藏
            case R.id.iv_collection:
                if (mUserInfo != null) {
                    new CollectionGoodsTask().execute();
                } else {
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }

                break;
            // TODO: 17/4/25 购买，即创建订单
            case R.id.tv_purchase:
                if (mUserInfo != null) {
                    // 如果用户未选择规格，则显示选择规格窗口,否则添加到购物车
                    String selectSpec = mTvSelectSpec.getText().toString();
                    if (TextUtils.isEmpty(selectSpec)) {
                        showSpecPopupWindow();
                    } else {
                        Intent intent = new Intent(this, CreateOrderActivity.class);
                        intent.putExtra(GOODS_EXTRA, mGoods);
                        intent.putExtra(GOODS_SPEC_KEY_EXTRA, mKey);
                        intent.putExtra(GOODS_COUNT_EXTRA, mGoodsCount);
                        intent.putExtra(ADDRESS_ID_EXTRA, mUserInfo.getAddressId());
                        startActivity(intent);
                    }

                } else {
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }

                break;
            // TODO: 17/4/25 加入购物车
            case R.id.tv_add_to_shopping_cart:
                if (mUserInfo != null) {
                    // 如果用户未选择规格，则显示选择规格窗口,否则添加到购物车
                    String selectSpec = mTvSelectSpec.getText().toString();
                    if (TextUtils.isEmpty(selectSpec)) {
                        showSpecPopupWindow();
                    } else {
                        //new AddGoodsToShoppingCartTask().execute();
                    }
                } else {
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
                break;
            // TODO: 17/4/27 数量改变时，规格价格UI待同步
            case R.id.iv_subtract:
                int count = Integer.parseInt(mTvCount.getText().toString());
                count--;
                if (count < 1) {
                    count = 1;
                }
                mTvCount.setText(String.valueOf(count));
                if (mTvGoodSpec.getText().toString().contains("已选")) {
                    String spec = mTvGoodSpec.getText().toString().replaceFirst("x\\d$", "x" + String.valueOf(count));
                    mTvGoodSpec.setText(spec);
                }
                break;
            case R.id.iv_add:
                count = Integer.parseInt(mTvCount.getText().toString());
                count++;
                mTvCount.setText(String.valueOf(count));
                if (mTvGoodSpec.getText().toString().contains("已选")) {
                    String spec = mTvGoodSpec.getText().toString().replaceFirst("x\\d$", "x" + String.valueOf(count));
                    mTvGoodSpec.setText(spec);
                }
                break;
            case R.id.iv_close_window:

                dismissPpwAfter();
                break;
        }
    }

    /**
     * 关闭规格选择窗口后更新详情页的规格UI
     */
    private void dismissPpwAfter() {
        mPopupWindow.dismiss();
        String ppwSpec = mTvGoodSpec.getText().toString().replaceFirst("已选：", "");
        if (!ppwSpec.contains("请选择")) {
            mTvSelectSpec.setText(ppwSpec);
        }
        // 保存选择的数量
        mGoodsCount = mTvCount.getText().toString();
        // 保存价格
        mSpecPrice = mTvSpecPrice.getText().toString();
    }

    @Override
    public void onDismiss() {
        dismissPpwAfter();
    }

    /**
     * 显示规格选择窗口
     */
    private void showSpecPopupWindow() {
        View contentView = LayoutInflater.from(this).inflate(R.layout.popup_goods_spec, null);
        ImageView ivGoodsImg = (ImageView) contentView.findViewById(R.id.iv_goods_img);
        mTvSpecPrice = (TextView) contentView.findViewById(R.id.tv_goods_money);
        mTvStock = (TextView) contentView.findViewById(R.id.tv_stock);
        mTvGoodSpec = (TextView) contentView.findViewById(R.id.tv_goods_spec);
        mTvCount = (TextView) contentView.findViewById(R.id.tv_count);
        ImageView ivSubstract = (ImageView) contentView.findViewById(R.id.iv_subtract);
        ImageView ivAdd = (ImageView) contentView.findViewById(R.id.iv_add);
        ImageView ivCloseWindow = (ImageView) contentView.findViewById(R.id.iv_close_window);

        LinearLayout llSpec1 = (LinearLayout) contentView.findViewById(R.id.ll_spec1);
        // 默认隐藏llSpec2
        LinearLayout llSpec2 = (LinearLayout) contentView.findViewById(R.id.ll_spec2);
        FlexRadioGroup lrSpecContent1 = (FlexRadioGroup) contentView.findViewById(R.id.rg_goods_spec1);
        FlexRadioGroup lrSpecContent2 = (FlexRadioGroup) contentView.findViewById(R.id.rg_goods_spec2);
        TextView tvSpecTitle1 = (TextView) contentView.findViewById(R.id.tv_spec_title1);
        TextView tvSpecTitle2 = (TextView) contentView.findViewById(R.id.tv_spec_title2);

        ImageView ivService = (ImageView) contentView.findViewById(R.id.iv_service);
        ImageView ivShoppingCart = (ImageView) contentView.findViewById(R.id.iv_shopping_cart);
        ImageView ivCollection = (ImageView) contentView.findViewById(R.id.iv_collection);
        TextView tvPurchase = (TextView) contentView.findViewById(R.id.tv_purchase);
        TextView tvAddToShoppingCart = (TextView) contentView.findViewById(R.id.tv_add_to_shopping_cart);

        ivSubstract.setOnClickListener(this);
        ivAdd.setOnClickListener(this);
        ivCloseWindow.setOnClickListener(this);

        ivService.setOnClickListener(this);
        ivShoppingCart.setOnClickListener(this);
        ivCollection.setOnClickListener(this);
        tvPurchase.setOnClickListener(this);
        tvAddToShoppingCart.setOnClickListener(this);

        lrSpecContent1.setOnCheckedChangeListener(this);
        lrSpecContent2.setOnCheckedChangeListener(this);

        List<Goods.FilterSpecEntity> filterSpecList = mGoods.getFilterSpec();
        if (filterSpecList != null) {
            mSpecNum = filterSpecList.size();
        }


        Picasso.with(this)
                .load(mGoods.getOriginalImg())
                .fit()
                .into(ivGoodsImg);
        if (TextUtils.isEmpty(mSpecPrice)) {
            mTvSpecPrice.setText(mGoods.getPrice());
        } else {
            mTvSpecPrice.setText(mSpecPrice);
        }
        mTvStock.setText(String.valueOf(mGoods.getStoreCount()));
        if (TextUtils.isEmpty(mGoodsCount)) {
            mTvCount.setText(String.valueOf(1));
        } else {
            mTvCount.setText(mGoodsCount);
        }

        if (mSpecNum != 0) {
            // 规格有两种,显示第二种布局
            if (mSpecNum == 2) {
                llSpec2.setVisibility(View.VISIBLE);
                Goods.FilterSpecEntity filterSpecEntity1 = filterSpecList.get(0);
                tvSpecTitle1.setText(filterSpecEntity1.getTitle());
                Goods.FilterSpecEntity filterSpecEntity2 = filterSpecList.get(1);
                tvSpecTitle2.setText(filterSpecEntity2.getTitle());
                List<Goods.FilterSpecEntity.SpecItemsEntity> specItemsEntityList1 = filterSpecEntity1.getSpecItems();
                List<Goods.FilterSpecEntity.SpecItemsEntity> specItemsEntityList2 = filterSpecEntity2.getSpecItems();


                for (int i = 0; i < specItemsEntityList1.size(); i++) {
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setButtonDrawable(null);
                    radioButton.setTextColor(getResources().getColorStateList(R.color.rb_popup_spec_text_color_selector));
                    radioButton.setBackgroundResource(R.drawable.rb_ppw_spec_selector);
                    radioButton.setPadding(16, 16, 16, 16);
                    FlexRadioGroup.LayoutParams layoutParams = new FlexRadioGroup.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(0, 4, 4, 0);
                    radioButton.setLayoutParams(layoutParams);

                    Goods.FilterSpecEntity.SpecItemsEntity specItem = specItemsEntityList1.get(i);
                    String spec = specItem.getItem();
                    radioButton.setText(spec);
                    radioButton.setTag(specItem);

                    lrSpecContent1.addView(radioButton);
                }

                for (int i = 0; i < specItemsEntityList1.size(); i++) {
                    Goods.FilterSpecEntity.SpecItemsEntity specItem = specItemsEntityList1.get(i);
                    RadioButton radioButton = (RadioButton) lrSpecContent1.getChildAt(i);
                    /**
                     * 如果{@link mKey} 不为空，则表示该用户已选择了规格，那么将对应key的RadioButton设置选中状态
                     */
                    if (!TextUtils.isEmpty(mKey)) {
                        String[] key = mKey.split(mDelimiter);
                        int k1 = Integer.parseInt(key[0]);
                        if (specItem.getItemId() == k1) {
                            radioButton.setChecked(true);
                        }
                    }
                }

                for (int i = 0; i < specItemsEntityList2.size(); i++) {
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setButtonDrawable(null);
                    radioButton.setTextColor(getResources().getColorStateList(R.color.rb_popup_spec_text_color_selector));
                    radioButton.setBackgroundResource(R.drawable.rb_ppw_spec_selector);
                    radioButton.setPadding(16, 16, 16, 16);
                    FlexRadioGroup.LayoutParams layoutParams = new FlexRadioGroup.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(0, 4, 4, 0);
                    radioButton.setLayoutParams(layoutParams);

                    Goods.FilterSpecEntity.SpecItemsEntity specItem = specItemsEntityList2.get(i);
                    String spec = specItem.getItem();
                    radioButton.setText(spec);
                    radioButton.setTag(specItem);

                    lrSpecContent2.addView(radioButton);
                }

                for (int i = 0; i < specItemsEntityList2.size(); i++) {
                    Goods.FilterSpecEntity.SpecItemsEntity specItem = specItemsEntityList2.get(i);
                    RadioButton radioButton = (RadioButton) lrSpecContent2.getChildAt(i);
                    /**
                     * 如果{@link mKey} 不为空，则表示该用户已选择了规格，那么将对应key的RadioButton设置选中状态
                     */
                    if (!TextUtils.isEmpty(mKey)) {
                        String[] key = mKey.split(mDelimiter);
                        int k2 = Integer.parseInt(key[1]);
                        if (specItem.getItemId() == k2) {
                            radioButton.setChecked(true);
                        }
                    }
                }

            } else {
                // todo 规格只有一种，待测试
                Goods.FilterSpecEntity filterSpecEntity1 = filterSpecList.get(0);
                tvSpecTitle1.setText(filterSpecEntity1.getTitle());
                List<Goods.FilterSpecEntity.SpecItemsEntity> specItemsEntityList = filterSpecEntity1.getSpecItems();
                for (int i = 0; i < specItemsEntityList.size(); i++) {
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setButtonDrawable(null);
                    radioButton.setTextColor(getResources().getColorStateList(R.color.rb_popup_spec_text_color_selector));
                    radioButton.setBackgroundResource(R.drawable.rb_ppw_spec_selector);
                    radioButton.setPadding(16, 16, 16, 16);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                    layoutParams.setMargins(0, 4, 4, 0);
                    radioButton.setLayoutParams(layoutParams);
                    Goods.FilterSpecEntity.SpecItemsEntity specItem = specItemsEntityList.get(i);
                    String spec = specItem.getItem();
                    radioButton.setText(spec);
                    radioButton.setTag(specItem);
                    lrSpecContent1.addView(radioButton);
                }

                for (int i = 0; i < specItemsEntityList.size(); i++) {
                    Goods.FilterSpecEntity.SpecItemsEntity specItem = specItemsEntityList.get(i);
                    RadioButton radioButton = (RadioButton) lrSpecContent1.getChildAt(i);
                    /**
                     * 如果{@link mKey} 不为空，则表示该用户已选择了规格，那么将对应key的RadioButton设置选中状态
                     */
                    if (!TextUtils.isEmpty(mKey)) {
                        String[] key = mKey.split(mDelimiter);
                        int k1 = Integer.parseInt(key[0]);
                        if (specItem.getItemId() == k1) {
                            radioButton.setChecked(true);
                        }
                    }
                }
            }

            mPopupWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, true);
            mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            mPopupWindow.showAtLocation(this.getCurrentFocus(), Gravity.BOTTOM, 0, 0);
            mPopupWindow.setOnDismissListener(this);
/*            contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int popupHeight = contentView.getMeasuredHeight();
            int[] outLocation = new int[2];
            llGoodsBottomMenu.getLocationOnScreen(outLocation);
            Point point = new Point();
            getWindowManager().getDefaultDisplay().getSize(point);
            Log.d(TAG, "initPopupWindow: point==>" + point +  "\t" + "popupHeight ==> " + popupHeight + "\t" + Arrays.toString(outLocation));
            mPopupWindow.showAtLocation(llGoodsBottomMenu, Gravity.NO_GRAVITY, outLocation[0], outLocation[1] - popupHeight);*/
        }
    }

    private void showServiceInfo() {
        if (!isFinishing()) {
            TextView view = new TextView(this);
            view.setPadding(16, 16, 16, 16);
            view.setText(Html.fromHtml(getString(R.string.service_contact_information)));
            view.setMovementMethod(LinkMovementMethod.getInstance());
            new AlertDialog.Builder(this)
                    .setTitle("客服信息")
                    .setView(view)
                    .setCancelable(true)
                    .show();
        }
    }

    private void showMessage(String msg) {
        if (!isFinishing()) {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        }
    }

    private void invalidateUI(Goods goods) {
        mGoods = goods;
        List<String> imagesUrl = new ArrayList<>();
        List<Goods.ImageDataEntity> imageDataEntityList = goods.getImageData();
        for (int i = 0; i < imageDataEntityList.size(); i++) {
            imagesUrl.add(imageDataEntityList.get(i).getImageUrl());
        }
        setupBanner(imagesUrl);

        mTvGoodsName.setText(goods.getGoodsName());
        mTvGoodsRemark.setText(goods.getGoodsRemark());
        mTvPrice.setText(goods.getPrice());
        int num = goods.getComment().getNumber();
        mTvCommentNumber.setText("（" + num + "）");
        mWebView.loadUrl(goods.getDetailInfoUrl());
    }

    @Override
    public void onCheckedChanged(FlexRadioGroup group, @IdRes int checkedId) {
        // 规则关系列表
        List<Goods.SpecRelationEntity> specRelationList = mGoods.getSpecRelation();

        switch (group.getId()) {
            // 规则窗口
            case R.id.rg_goods_spec1:
                for (int i = 0; i < group.getChildCount(); i++) {
                    RadioButton radioButton = (RadioButton) group.getChildAt(i);
                    if (radioButton.getId() == checkedId) {
                        Goods.FilterSpecEntity.SpecItemsEntity specItem = (Goods.FilterSpecEntity.SpecItemsEntity) radioButton.getTag();

                        mk1 = specItem.getItemId();

                        if (mk2 != 0) {
                            mKey = mk1 + mDelimiter + mk2;
                        }

                        Log.d(TAG, "onCheckedChanged: mKey ==> " + mKey);

                        /**
                         * 处理规则关系
                         */
                        for (int j = 0; j < specRelationList.size(); j++) {
                            String key = specRelationList.get(j).getKey();
                            if (key.equals(mKey)) {
                                double money = Double.parseDouble(specRelationList.get(j).getPrice());
                                int count = Integer.parseInt(mTvCount.getText().toString());
                                double price = money * count;
                                mTvSpecPrice.setText(String.valueOf(price));
                                mTvStock.setText(String.valueOf(specRelationList.get(j).getStock()));
                                mTvGoodSpec.setText("已选：" + specRelationList.get(j).getValue() + " x" + mTvCount.getText());
                            }
                        }
                    }
                }
                break;
            case R.id.rg_goods_spec2:
                for (int i = 0; i < group.getChildCount(); i++) {
                    RadioButton radioButton = (RadioButton) group.getChildAt(i);
                    if (radioButton.getId() == checkedId) {
                        Goods.FilterSpecEntity.SpecItemsEntity specItem = (Goods.FilterSpecEntity.SpecItemsEntity) radioButton.getTag();

                        mk2 = specItem.getItemId();

                        if (mk1 != 0) {
                            mKey = mk1 + mDelimiter + mk2;
                        }

                        Log.d(TAG, "onCheckedChanged: mKey ==> " + mKey);

                        /**
                         * 处理规则关系
                         */
                        for (int j = 0; j < specRelationList.size(); j++) {
                            String key = specRelationList.get(j).getKey();
                            if (key.equals(mKey)) {
                                double money = Double.parseDouble(specRelationList.get(j).getPrice());
                                int count = Integer.parseInt(mTvCount.getText().toString());
                                double price = money * count;
                                mTvSpecPrice.setText(String.valueOf(price));
                                mTvStock.setText(String.valueOf(specRelationList.get(j).getStock()));
                                mTvGoodSpec.setText("已选：" + specRelationList.get(j).getValue() + " x" + mTvCount.getText());
                            }
                        }
                    }
                }
                break;

        }

    }

    private class PicassoImageLoader implements ImageLoader {

        @Override
        public void displayImage(final Context context, Object path, ImageView imageView) {
            // 加载发生错误会重复三次请求，三次都失败才会显示error Place holder
            Picasso.with(context).load((String) path)
                    .fit()  // This avoided the OutOfMemoryError
                    .into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "onSuccess: 加载轮播图成功！");
                        }

                        @Override
                        public void onError() {
                            Toast.makeText(context, "服务器异常，加载轮播图失败！", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }


    private class FetchGoodsDetailTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            mGoodsRepository.getGoodsDetail(mGoodsId, new GoodsDatasource.GetGoodsDetailCallback() {
                @Override
                public void onGoodsLoaded(final Goods goods) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            invalidateUI(goods);
                        }
                    });
                }

                @Override
                public void onDataNotAvailable(String errorMsg) {
                    showMessage(errorMsg);
                }
            });
            return null;
        }
    }


    private class CollectionGoodsTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", mUserInfo.getUserId());
            map.put("goodsId", mGoods.getGoodsId());
            map.put("isStar", true);
            mGoodsRepository.starGoods(map, new GoodsDatasource.StarGoodsCallback() {
                @Override
                public void onStarSuccess() {
                    showMessage(getString(R.string.star_success_msg));
                }

                @Override
                public void onStarFailure(String errorMsg) {
                    showMessage(errorMsg);
                }
            });
            return null;
        }
    }

    private class AddGoodsToShoppingCartTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", mUserInfo.getUserId());
            map.put("goodsIds", mGoods.getGoodsId());
            map.put("isAdd", true);
            return null;
        }
    }

    /**
     * 获取用户信息
     */
    private class FetchUserInfoTask extends AsyncTask<Integer, Void, Void> {
        @Override
        protected Void doInBackground(Integer... params) {
            int userId = params[0];
            mUserRepository.fetchUserInfo(userId, new UserDatasource.GetUserInfoCallback() {
                @Override
                public void onUserInfoLoaded(final UserInfo userInfo) {
                    mUserInfo = userInfo;
                    PreferencesUtil.persistentUserInfo(GoodsDetailActivity.this, userInfo);
                }

                @Override
                public void onDataNotAvailable(String errorMsg) {
                    Log.w(TAG, "onDataNotAvailable: " + errorMsg);
                    showMessage(errorMsg);
                }
            });

            return null;
        }
    }
}
