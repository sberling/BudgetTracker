package hu.ait.budgettracker;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import hu.ait.budgettracker.adapter.TransactionAdapter;
import hu.ait.budgettracker.data.Transaction;
import io.realm.Realm;

public class ListActivity extends AppCompatActivity {


    public static final int REQUEST_CODE_CREATE = 101;
    public static final int REQUEST_CODE_EDIT = 102;
    public static final boolean INCOME = true;
    public static final String INCOME_OR_OUTCOME = "IO";
    public static final boolean OUTCOME = false;
    public static final String KEY_EDIT = "KEY EDIT";

    private TransactionAdapter adapter;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private int positionToEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);


        setupRecycler();

        setupFabs();
    }

    private void setupFabs() {
        final FloatingActionsMenu fabMenu = (FloatingActionsMenu) findViewById(R.id.fabMenu);
        FloatingActionButton newIncome = (FloatingActionButton) findViewById(R.id.action_income);
        FloatingActionButton newOutcome = (FloatingActionButton) findViewById(R.id.action_outcome);
        newIncome.setImageResource(R.mipmap.money_bag);
        newIncome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newTransaction(INCOME);
                fabMenu.collapse();
            }
        });
        newOutcome.setImageResource(R.mipmap.money_fire);
        newOutcome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newTransaction(OUTCOME);
                fabMenu.collapse();
            }
        });
    }

    public void editTransaction(String transactionID, int position) {
        Intent editTransactionIntent = new Intent(this, NewEditTransaction.class);
        editTransactionIntent.putExtra(KEY_EDIT, transactionID);
        startActivityForResult(editTransactionIntent, REQUEST_CODE_EDIT);
        positionToEdit = position;
    }

    public void showDeleteDialogBox(final int position) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.delete_transaction);
        builder.setMessage(R.string.sure_delete);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                adapter.deleteTransaction(position);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();

    }

    private void newTransaction(boolean income) {
        Intent newIncomeIntent = new Intent(ListActivity.this, NewEditTransaction.class);
        newIncomeIntent.putExtra(INCOME_OR_OUTCOME, income);
        startActivityForResult(newIncomeIntent, REQUEST_CODE_CREATE);
    }


    private void setupRecycler() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new TransactionAdapter(this, getRealm());
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case RESULT_OK:
                String transactionID = data.getStringExtra(NewEditTransaction.KEY_TRANSACTION);
                Transaction transaction = getRealm().where(Transaction.class)
                        .equalTo("transactionID", transactionID)
                        .findFirst();

                if (requestCode == REQUEST_CODE_CREATE) {
                    adapter.addNew(transaction);
                } else if (requestCode == REQUEST_CODE_EDIT) {
                    adapter.updateTransaction(positionToEdit, transaction);
                }

                break;
            case RESULT_CANCELED:
                Toast.makeText(ListActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                break;
        }
        recyclerView.smoothScrollToPosition(0);
    }

    public Realm getRealm() {
        return ((MainApplication) getApplication()).getTransactionRealm();
    }
}
