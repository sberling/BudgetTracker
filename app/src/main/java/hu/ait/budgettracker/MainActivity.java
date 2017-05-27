package hu.ait.budgettracker;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import hu.ait.budgettracker.data.Frequency;
import hu.ait.budgettracker.data.Transaction;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private List<Transaction> incomeList;
    public static final String incomeString = "income";
    private List<Transaction> outcomeList;
    private double totalRelevantOutcome;
    private double totalRelevantIncome;

    //chart stuff
    private PieChart transactionChart;
    private List<PieEntry> transactionEntries;


    //other
    private Frequency frequency = Frequency.DAILY;
    private TextView tvNetBudget;
    private TextView tvBudgetFrequency;
    public static final boolean INCOME = true;
    public static final boolean OUTCOME = false;
    private boolean incomeOrOutcome;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        incomeOrOutcome = OUTCOME;
        tvNetBudget = (TextView) findViewById(R.id.tvNetBudget);
        tvBudgetFrequency = (TextView) findViewById(R.id.tvBudgetFrequency);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        ((MainApplication) getApplication()).openRealm();
        setupNavView(toolbar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupChart();
    }

    private void setupChart() {
        transactionChart = (PieChart) findViewById(R.id.transactionChart);

        setChartLook();

        retrieveRealmData();

        validateChart();
    }

    private void retrieveRealmData() {
        RealmResults<Transaction> incomeRealmResults = ((MainApplication) getApplication()).getTransactionRealm().where(Transaction.class)
                .equalTo(incomeString, ListActivity.INCOME).findAll();
        incomeList = new ArrayList<Transaction>();
        for (int i = 0; i < incomeRealmResults.size(); i++) {
            incomeList.add(incomeRealmResults.get(i));
        }

        RealmResults<Transaction> outcomeRealmResults = ((MainApplication) getApplication()).getTransactionRealm().where(Transaction.class)
                .equalTo(incomeString, ListActivity.OUTCOME).findAll();
        outcomeList = new ArrayList<Transaction>();
        for (int i = 0; i < outcomeRealmResults.size(); i++) {
            outcomeList.add(outcomeRealmResults.get(i));
        }
    }

    private void setChartLook() {
        Description d = new Description();
        d.setText("");

        transactionChart.setDescription(d);
        transactionChart.setCenterTextSize(20);
        transactionChart.setEntryLabelTextSize(18);
        transactionChart.setEntryLabelColor(getResources().getColor(android.R.color.black));
        transactionChart.setEntryLabelTypeface(Typeface.defaultFromStyle(Typeface.BOLD_ITALIC));
        transactionChart.setHoleColor(getResources().getColor(R.color.background));
        setupLegend();
    }

    private void setupLegend() {
        Legend legend = transactionChart.getLegend();
        legend.setTextSize(15);
        legend.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
        legend.setWordWrapEnabled(true);
        legend.setXEntrySpace(10);
    }

    private void validateChart() {
        totalRelevantIncome = 0;
        totalRelevantOutcome = 0;
        transactionEntries = new ArrayList<PieEntry>();
        String centerText = "";
        double totalRelevantMoney;

        if (incomeMode()) {
            populateWithIncome();
            centerText = frequency.getString() + getString(R.string._income);
            totalRelevantMoney = totalRelevantIncome;
        } else {
            populateWithOutcome();
            centerText = frequency.getString() + getString(R.string._expenses);
            totalRelevantMoney = totalRelevantOutcome;
        }

        PieData transactionData = setupDataSets();

        transactionChart.setCenterText(centerText + "\n" + NumberFormat.getCurrencyInstance().format(totalRelevantMoney));
        transactionChart.setData(transactionData);
        transactionChart.invalidate();

        tvNetBudget.setText(NumberFormat.getCurrencyInstance().format(totalRelevantIncome - totalRelevantOutcome));
        String budgetFrequency = frequency.getString() + getString(R.string._budget);
        tvBudgetFrequency.setText(budgetFrequency);
    }

    @NonNull
    private PieData setupDataSets() {
        PieDataSet transactionDataSet = new PieDataSet(transactionEntries, "");
        transactionDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        transactionDataSet.setValueTextSize(18);
        PieData transactionData = new PieData(transactionDataSet);
        transactionData.setValueFormatter(new ValueFormatter());
        return transactionData;
    }

    private void populateWithOutcome() {
        for (Transaction income : incomeList) {
            double cost = getRelevantCost(income);
            totalRelevantIncome += cost;
        }
        condenseCategories(OUTCOME);
    }

    private void populateWithIncome() {
        condenseCategories(INCOME);
        for (Transaction outcome : outcomeList) {
            double cost = getRelevantCost(outcome);
            totalRelevantOutcome += cost;
        }
    }

    private void condenseCategories(boolean IO) {
        if (IO == INCOME) {
            for (String category : getIncomeCategories()) {
                double categoryTotal = 0;
                for (Transaction income : incomeList) {
                    if (income.getCategory().equals(category)) {
                        double cost = getRelevantCost(income);
                        categoryTotal += cost;
                        totalRelevantIncome += cost;
                    }
                }
                if (categoryTotal != 0) {
                    transactionEntries.add(new PieEntry((float) categoryTotal, category));
                }
            }
        } else {
            for (String category : getOutcomeCategories()) {
                double categoryTotal = 0;
                for (Transaction outcome : outcomeList) {
                    if (outcome.getCategory().equals(category)) {
                        double cost = getRelevantCost(outcome);
                        categoryTotal += cost;
                        totalRelevantOutcome += cost;
                    }
                }
                if (categoryTotal != 0) {
                    transactionEntries.add(new PieEntry((float) categoryTotal, category));
                }
            }

        }
    }


    private void changeFrequency(Frequency freq) {
        if (!frequency.equals(freq)) {
            frequency = freq;
        }
    }


    private double getRelevantCost(Transaction t) {
        double relevantCost;

        Frequency f = getFrequency(t.getFrequency());
        double c = t.getCost();
        relevantCost = c * f.getRatios()[frequency.getIndex()];

        return relevantCost;
    }

    private void setupNavView(Toolbar toolbar) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    public Frequency getFrequency(String f) {
        if (f.equalsIgnoreCase(getString(R.string.daily))) {
            return Frequency.DAILY;
        } else if (f.equalsIgnoreCase(getString(R.string.weekly))) {
            return Frequency.WEEKLY;
        } else if (f.equalsIgnoreCase(getString(R.string.monthly))) {
            return Frequency.MONTHLY;
        } else if (f.equalsIgnoreCase(getString(R.string.annual))) {
            return Frequency.ANNUAL;
        } else {
            return Frequency.NONE;
        }

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.itemDaily:
                changeFrequency(Frequency.DAILY);
                break;
            case R.id.itemWeekly:
                changeFrequency(Frequency.WEEKLY);
                break;
            case R.id.itemMonthly:
                changeFrequency(Frequency.MONTHLY);
                break;
            case R.id.itemAnnual:
                changeFrequency(Frequency.ANNUAL);
                break;
            case R.id.itemIncome:
                incomeOrOutcome = INCOME;
                break;
            case R.id.itemOutcome:
                incomeOrOutcome = OUTCOME;
            default:
                break;
        }
        validateChart();
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.itemTransactions:
                Intent showTransactionsIntent = new Intent(this, ListActivity.class);
                this.startActivity(showTransactionsIntent);
                break;
            case R.id.itemAbout:
                startActivity(new Intent(this, SplashActivity.class));
                break;
            case R.id.itemEditCategories:
                this.startActivity(new Intent(this, EditCategoriesActivity.class));
                break;
            default:
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean incomeMode() {
        return (incomeOrOutcome == INCOME);
    }

    private boolean outcomeMode() {
        return (incomeOrOutcome == OUTCOME);
    }

    private ArrayList<String> getOutcomeCategories() {
        return ((MainApplication) getApplication()).getOutcomeCategories();
    }

    private ArrayList<String> getIncomeCategories() {
        return ((MainApplication) getApplication()).getIncomeCategories();
    }

    public class ValueFormatter implements IValueFormatter {

        private DecimalFormat mFormat;

        public ValueFormatter() {
            mFormat = new DecimalFormat("###,###,##0.00");
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            return "$" + mFormat.format(value); // e.g. append a dollar-sign
        }
    }

}
