package hu.ait.budgettracker;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.UUID;

import hu.ait.budgettracker.data.Frequency;
import hu.ait.budgettracker.data.Transaction;
import io.realm.Realm;

public class NewEditTransaction extends AppCompatActivity {

    //Spinner stuff
    private ArrayList<String> categories;
    private static final String[] frequencies = Frequency.getAllStrings();

    //Views
    private EditText etName;
    private EditText etAmount;
    private Spinner spCategory;
    private Spinner spFrequency;
    private Button btnNewCategory;
    private Button btnCancel;
    private Button btnSave;
    private ToggleButton tbIO;

    //Constants
    public static final String KEY_TRANSACTION = "KEY_TRANSACTION";
    public static final String TRANSACTION_ID = "transactionID";
    public static final boolean CREATE = true;
    public static final boolean EDIT = false;

    //Other stuff
    private Transaction transaction;
    private boolean transactionType;
    private boolean createOrEdit;
    private Intent intent;
    private String transactionID = TRANSACTION_ID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_edit_transaction);

        intent = getIntent();

        createOrEdit = (intent.getSerializableExtra(ListActivity.KEY_EDIT) == null);

        if (editMode()) {
            String transactionID = intent.getStringExtra(ListActivity.KEY_EDIT);
            transaction = getRealm().where(Transaction.class)
                    .equalTo(TRANSACTION_ID, transactionID).findFirst();
        }

        incomeOrOutcome();

        setViews();

        if (editMode()) {
            initEdit();
        }

        setupButtons();
    }

    private void initEdit() {
        etName.setText(transaction.getName());
        etAmount.setText(NumberFormat.getCurrencyInstance().format(transaction.getCost()).substring(1)); //substring(1) gets rid of the $
        int spinnerPos = getSpinnerPosCategory();
        spCategory.setSelection(spinnerPos);
        spinnerPos = getSpinnerPosFrequency();
        spFrequency.setSelection(spinnerPos);
    }

    private int getSpinnerPosCategory() {
        String category = transaction.getCategory();

        int spinnerPos = -1;
        do {
            spinnerPos++;

        }
        while (!spCategory.getItemAtPosition(spinnerPos).toString().equals(category) && spinnerPos < categories.size());

        if (spinnerPos >= categories.size()) {
            spinnerPos = 0;
        }
        return spinnerPos;
    }

    private int getSpinnerPosFrequency() {
        String frequency = transaction.getFrequency();

        int spinnerPos = -1;
        do {
            spinnerPos++;
        }
        while (!spFrequency.getItemAtPosition(spinnerPos).toString().equals(frequency) && spinnerPos < frequencies.length);
        return spinnerPos;
    }

    private void incomeOrOutcome() {
        if (createMode()) {
            transactionType = intent.getBooleanExtra(ListActivity.INCOME_OR_OUTCOME, true);
        } else {
            transactionType = transaction.isIncome();
        }
        checkCategories();
    }

    private void checkCategories() {
        if (transactionType == ListActivity.INCOME) {
            categories = ((MainApplication) getApplication()).getIncomeCategories();
        } else {
            categories = ((MainApplication) getApplication()).getOutcomeCategories();
        }
    }

    private void setupButtons() {
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (createMode()) {
                    initCreate();
                }

                if (etAmount.getText().toString().equalsIgnoreCase("") || categories.size() == 0 || spFrequency.getSelectedItem().equals(null)) {
                    setErrors();
                } else {
                    if(checkForDuplicates()){
                        Toast.makeText(NewEditTransaction.this, R.string.duplicate_error, Toast.LENGTH_LONG).show();
                    }
                    else {
                        setData();

                        goBack(RESULT_OK);
                    }
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBack(RESULT_CANCELED);
            }
        });


        if (editMode()) {
            tbIO.setVisibility(View.VISIBLE);
            tbIO.setChecked(transactionType);
            checkTb();
            tbIO.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    applyTbChange();
                }
            });
        }
    }

    private void checkTb() {
        if (tbIO.isChecked()) {
            transactionType = ListActivity.INCOME;
            tbIO.setBackgroundColor(getResources().getColor(R.color.green_semi_transparent));
        } else {
            transactionType = ListActivity.OUTCOME;
            tbIO.setBackgroundColor(getResources().getColor(R.color.red_semi_transparent));
        }
    }

    private void applyTbChange() {
        checkTb();
        checkCategories();
        spCategory.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories));
    }

    private void goBack(int resultCode) {
        Intent back = new Intent(NewEditTransaction.this, ListActivity.class);
        if (resultCode == RESULT_OK) {
            back.putExtra(KEY_TRANSACTION, transaction.getTransactionId());
        }
        setResult(resultCode, back);

        NewEditTransaction.this.finish();
    }

    private boolean checkForDuplicates() {
        boolean foundDuplicate = false;

        if (getRealm().where(Transaction.class)
                .equalTo("name", etName.getText().toString())
                .equalTo("income", transactionType)
                .equalTo("cost", Double.parseDouble(etAmount.getText().toString()))
                .equalTo("category", categories.get(spCategory.getSelectedItemPosition()))
                .equalTo("frequency", frequencies[spFrequency.getSelectedItemPosition()])
                .findFirst() != null) {
            foundDuplicate = true;
        }
        return foundDuplicate;
    }

    private void setData() {
        getRealm().beginTransaction();
        transaction.setIncome(transactionType);
        if (!etName.getText().toString().equals("")) {
            transaction.setName(etName.getText().toString());
        }
        transaction.setCost(Double.parseDouble(etAmount.getText().toString()));
        transaction.setCategory(categories.get(spCategory.getSelectedItemPosition()));
        transaction.setFrequency(frequencies[spFrequency.getSelectedItemPosition()]);
        getRealm().commitTransaction();
    }

    private void initCreate() {
        getRealm().beginTransaction();
        transaction = getRealm().createObject(Transaction.class, UUID.randomUUID().toString());
        getRealm().commitTransaction();
    }

    private void setErrors() {
        if (etAmount.getText().toString().equalsIgnoreCase("")) {
            etAmount.setError("Please enter an amount");
        }
        if (categories.size() == 0) {
            btnNewCategory.setError("Please create a category");
        }
        if (spFrequency.getSelectedItem().equals(null)) {
            ((TextView) findViewById(R.id.tvFrequency)).setError("Please select a frequency");
        }
    }

    private void setViews() {
        etName = (EditText) findViewById(R.id.etName);
        etName.addTextChangedListener(duplicateWatcher);
        etAmount = (EditText) findViewById(R.id.etAmount);
        etAmount.addTextChangedListener(duplicateWatcher);
        spCategory = (Spinner) findViewById(R.id.spCategory);
        spFrequency = (Spinner) findViewById(R.id.spFrequency);
        btnNewCategory = (Button) findViewById(R.id.btnNewCategory);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnSave = (Button) findViewById(R.id.btnSave);
        tbIO = (ToggleButton) findViewById(R.id.tbIO);
        spCategory.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories));


        btnNewCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNewCategoryDialog();
            }
        });

        spFrequency.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, frequencies));
    }

    private void showNewCategoryDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText etNewCategory = getEditText();
        builder.setView(etNewCategory);
        builder.setTitle(getString(R.string.new_category));
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addCategory(etNewCategory);

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        etNewCategory.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString();
                input = removeSpaces(input);
                if (input.isEmpty()) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }
        });
    }

    private void addCategory(EditText etNewCategory) {
        String newCategory = removeSpaces(etNewCategory.getText().toString());
        boolean goodTogo = true;
        for (String category : categories) {
            if (category.equalsIgnoreCase(newCategory)) {
                goodTogo = false;
                break;
            }
        }
        if (goodTogo) {
            categories.add(0, newCategory);
            spCategory.setAdapter(new ArrayAdapter<String>(NewEditTransaction.this, android.R.layout.simple_spinner_item, categories));
            ((MainApplication) getApplication()).writeCategories();
        }
    }

    /**
     * This method just makes sure that a new Category can't contain anything but letters
     *
     * @return
     */
    @NonNull
    public EditText getEditText() {
        final EditText etNewCategory = new EditText(this);
        etNewCategory.setFilters(new InputFilter[]{
                new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence src, int start,
                                               int end, Spanned dst, int dstart, int dend) {
                        if (src.equals("")) { // for backspace
                            return src;
                        }
                        if (src.toString().matches("[a-zA-Z ]+")) {
                            return src;
                        }
                        return "";
                    }
                }
        });
        return etNewCategory;
    }


    private Realm getRealm() {
        return ((MainApplication) getApplication()).getTransactionRealm();
    }

    private boolean editMode() {
        return (createOrEdit == EDIT);
    }

    private boolean createMode() {
        return (createOrEdit == CREATE);
    }

    private static String removeStartingSpaces(String s) {
        while (s.startsWith(" ")) {
            s = s.substring(1);
        }
        return s;
    }

    private static String removeEndingSpaces(String s) {
        String r = new StringBuilder(s).reverse().toString();
        r = removeStartingSpaces(r);
        s = new StringBuilder(r).reverse().toString();
        return s;
    }

    public static String removeSpaces(String s) {
        s = removeStartingSpaces(s);
        s = removeEndingSpaces(s);
        return s;
    }

    TextWatcher duplicateWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            String input = s.toString();
            input = removeSpaces(input);
            if (input.isEmpty()) {
                btnSave.setEnabled(false);
            } else {
                btnSave.setEnabled(true);
            }
        }
    };
}
