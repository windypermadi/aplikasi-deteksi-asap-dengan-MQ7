package aplikasiispu.rajacoding.com;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.bumptech.glide.Glide;
import com.leavjenn.smoothdaterangepicker.date.SmoothDateRangePickerFragment;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import aplikasiispu.rajacoding.com.helper.Config;
import aplikasiispu.rajacoding.com.helper.SessionManager;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.view.PieChartView;

public class LaporanActivity extends AppCompatActivity {
    private String tanggal_awal = "";
    private String tanggal_akhir = "";
    public String waktu = "N";
    private TextView text_more;
    int limit = 0;
    int offset = 10;
    private CardView cv_filter1, cv_filter;
    private TextView text_awal, text_akhir;
    public String ip;
    public String idadmin;
    public SessionManager SessionManager;
    public ProgressDialog pDialog;
    String asap;
    int jumlah_aman, jumlah_bahaya;
    private PieChartView pieChartView;
    private ImageView filter;
    private RecyclerView rv_laporan;
    private ArrayList<LaporanModel> LaporanModel;
    private TextView text_asap1, text_asap3;
    private SwipeRefreshLayout swipe_refresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laporan);
        SessionManager = new SessionManager(getApplicationContext());
        SessionManager.checkLogin();
        HashMap<String, String> user = SessionManager.getUserDetails();
        idadmin = user.get(SessionManager.KEY_ID);
        ip = user.get(SessionManager.KEY_IP);

        text_awal = findViewById(R.id.text_awal);
        text_akhir = findViewById(R.id.text_akhir);
        cv_filter1 = findViewById(R.id.cv_filter1);
        pieChartView = findViewById(R.id.chart);
        filter = findViewById(R.id.filter);
        cv_filter = findViewById(R.id.cv_filter);
        rv_laporan = findViewById(R.id.rv_laporan);
        text_asap1 = findViewById(R.id.text_asap1);
        text_asap3 = findViewById(R.id.text_asap3);
        text_more = findViewById(R.id.text_more);
        swipe_refresh = findViewById(R.id.swipe_refresh);

        LaporanModel = new ArrayList<>();
        LinearLayoutManager i = new GridLayoutManager(getApplicationContext(), 1, GridLayoutManager.VERTICAL, false);
        rv_laporan.setHasFixedSize(true);
        rv_laporan.setLayoutManager(i);
        rv_laporan.setNestedScrollingEnabled(true);

        AksiTombol();
    }

    @Override
    protected void onResume() {
        super.onResume();
        pDialog = new ProgressDialog(LaporanActivity.this);
        pDialog.setCancelable(false);
        pDialog.setMessage("Memuat Tampilan . .");
        showDialog();
        limit = 0;
        LaporanModel.clear();
        LoadData();
    }

    private void AksiTombol() {
        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pDialog = new ProgressDialog(LaporanActivity.this);
                pDialog.setCancelable(false);
                pDialog.setMessage("Memuat Tampilan . .");
                showDialog();
                LaporanModel.clear();
                waktu = "Y";
                limit = 0;
                LoadData();
                getCart();
            }
        });
        cv_filter1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cv_filter1.setVisibility(View.GONE);
                cv_filter.setVisibility(View.VISIBLE);
                filter.setVisibility(View.VISIBLE);
                SmoothDateRangePickerFragment smoothDateRangePickerFragment =
                        SmoothDateRangePickerFragment
                                .newInstance(new SmoothDateRangePickerFragment.OnDateRangeSetListener() {
                                    @SuppressLint("SetTextI18n")
                                    @Override
                                    public void onDateRangeSet(SmoothDateRangePickerFragment view,
                                                               int yearStart, int monthStart,
                                                               int dayStart, int yearEnd,
                                                               int monthEnd, int dayEnd) {
                                        String date = "You picked the following date range: \n"
                                                + "From " + dayStart + "/" + (++monthStart)
                                                + "/" + yearStart + " To " + dayEnd + "/"
                                                + (++monthEnd) + "/" + yearEnd;

                                        tanggal_awal = yearStart + "-" + monthStart + "-" + dayStart;
                                        tanggal_akhir = yearEnd + "-" + monthEnd + "-" + dayEnd;
                                        text_awal.setText(yearStart + "-" + monthStart + "-" + dayStart);
                                        text_akhir.setText(yearEnd + "-" + monthEnd + "-" + dayEnd);
                                    }
                                });
                smoothDateRangePickerFragment.show(getFragmentManager(), "Datepickerdialog");
            }
        });
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        text_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pDialog = new ProgressDialog(LaporanActivity.this);
                pDialog.setCancelable(false);
                pDialog.setMessage("Memuat Tampilan . .");
                showDialog();
                waktu = "Y";
                limit = limit + offset;
                LoadData();
                getCart();
            }
        });
        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pDialog = new ProgressDialog(LaporanActivity.this);
                pDialog.setCancelable(false);
                pDialog.setMessage("Memuat Tampilan . .");
                showDialog();
                limit = 0;
                LaporanModel.clear();
                LoadData();
            }
        });
    }

    private void getCart() {
        List<SliceValue> pieData = new ArrayList<>();
        pieData.add(new SliceValue(jumlah_bahaya, getResources().getColor(R.color.colorRed)));
        pieData.add(new SliceValue(jumlah_aman, getResources().getColor(R.color.colorGreen)));

        PieChartData pieChartData = new PieChartData(pieData);
        pieChartData.setHasLabels(true).setValueLabelTextSize(14);
        pieChartData.setHasCenterCircle(true).setCenterText1("Data Asap").setCenterText1FontSize(20).setCenterText1Color(Color.parseColor("#0097A7"));
        pieChartView.setPieChartData(pieChartData);
    }

    private void LoadData() {
        AndroidNetworking.post("http://" + ip + Config.HOST  + "list_data.php")
                .addBodyParameter("waktu", waktu)
                .addBodyParameter("menu", "laporan")
                .addBodyParameter("limit", String.valueOf(limit))
                .addBodyParameter("offset", String.valueOf(offset))
                .addBodyParameter("waktu_awal", tanggal_awal)
                .addBodyParameter("waktu_akhir", tanggal_akhir)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.optJSONArray("result");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject responses = jsonArray.getJSONObject(i);
                                LaporanModel bk = new LaporanModel(
                                        responses.getString("idasap"),
                                        responses.getString("asap"),
                                        responses.getString("waktu"));
                                LaporanModel.add(bk);
                            }

                            LaporanAdapter adapter = new LaporanAdapter(getApplicationContext(), LaporanModel);
                            rv_laporan.setAdapter(adapter);

                            if (adapter.getItemCount() < offset) {
                                text_more.setVisibility(View.GONE);
                            } else {
                                text_more.setVisibility(View.VISIBLE);
                            }

                            hideDialog();
                            swipe_refresh.setRefreshing(false);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            hideDialog();
                        }

                        try {
                            JSONArray jsonArray = response.optJSONArray("result");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject responses = jsonArray.getJSONObject(i);
                                asap = responses.optString("asap");
                                jumlah_aman = responses.optInt("jumlah_aman");
                                jumlah_bahaya = responses.optInt("jumlah_bahaya");
                            }

                            text_asap1.setText("(" + jumlah_bahaya + ")");
                            text_asap3.setText("(" + jumlah_aman + ")");

                            getCart();
                            hideDialog();
                            swipe_refresh.setRefreshing(false);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            hideDialog();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        hideDialog();
                        new AlertDialog.Builder(LaporanActivity.this)
                                .setTitle("Konfirmasi !!!")
                                .setMessage("Ini merupakan data asap terakhir!!!")
                                .setCancelable(false)
                                .setNegativeButton("Tutup pesan", null)
                                .show();
                        hideDialog();
                        text_more.setVisibility(View.GONE);
                    }
                });
    }

    public class LaporanAdapter extends RecyclerView.Adapter<LaporanAdapter.ProductViewHolder> {
        private Context mCtx;
        private List<LaporanModel> LaporanModel;

        public LaporanAdapter(Context mCtx, List<LaporanModel> LaporanModel) {
            this.mCtx = mCtx;
            this.LaporanModel = LaporanModel;
        }

        @Override
        public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater inflater = LayoutInflater.from(mCtx);
            @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.row_model_histori, null);
            return new ProductViewHolder(view);
        }

        @SuppressLint({"SetTextI18n", "ResourceType", "UseCompatLoadingForDrawables"})
        @Override
        public void onBindViewHolder(ProductViewHolder holder, int i) {
            final LaporanModel item = LaporanModel.get(i);
            holder.text_tanggal.setText(item.getTanggal());
            holder.text_asap.setText("asap " + item.getAsap() + " ppm");
            holder.cv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    popupgantidefault(item.getAsap(), "Terdapat perubahan intensitas nilai default Sensor MQ-7 senilai " + item.getAsap() + " ppm. Apakah mau melakukan perubahan?");
                }
            });
        }

        @Override
        public int getItemCount() {
            return LaporanModel.size();
        }

        class ProductViewHolder extends RecyclerView.ViewHolder {
            TextView text_tanggal, text_asap;
            CardView cv;

            ProductViewHolder(View itemView) {
                super(itemView);
                text_tanggal = itemView.findViewById(R.id.text_tanggal);
                text_asap = itemView.findViewById(R.id.text_asap);
                cv = itemView.findViewById(R.id.cv);
            }
        }
    }

    private void UpdateIntensitas(String asap) {
        AndroidNetworking.get("http://" + ip + Config.HOST + "update_intensitas.php")
                .addQueryParameter("nilai_intensitas", asap)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        new AlertDialog.Builder(LaporanActivity.this)
                                .setTitle("Konfirmasi !!!")
                                .setMessage(response.optString("pesan"))
                                .setCancelable(false)
                                .setNegativeButton("Baiklah", null)
                                .show();
                    }

                    @Override
                    public void onError(ANError error) {
                        hideDialog();
                    }
                });
    }

    private void popupgantidefault(final String asap, String isi) {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(LaporanActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        @SuppressLint("InflateParams") final View dialogView = inflater.inflate(R.layout.model_pergantian_default, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        final AlertDialog alertDialog = dialogBuilder.create();
        TextView keterangan = dialogView.findViewById(R.id.keterangan);
        keterangan.setText(isi);
        dialogView.findViewById(R.id.tidak).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        dialogView.findViewById(R.id.ya).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateIntensitas(asap);
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private void showDialog() {
        if (!pDialog.isShowing()) {
            pDialog.show();
        }
    }

    private void hideDialog() {
        if (pDialog.isShowing()) {
            pDialog.dismiss();
        }
    }
}