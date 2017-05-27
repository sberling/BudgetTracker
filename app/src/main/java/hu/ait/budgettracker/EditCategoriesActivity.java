package hu.ait.budgettracker;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.getbase.floatingactionbutton.AddFloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import hu.ait.budgettracker.data.Transaction;
import io.realm.Realm;
import io.realm.RealmResults;

public class EditCategoriesActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayList<String> outcomeCategories;
    private ArrayList<String> incomeCategories;
    private ArrayList<String> useCategories;
    private ToggleButton tbIO;
    private boolean io;
    private final ArrayList<Transaction> affectedTransactions;

    public EditCategoriesActivity() {
        affectedTransactions = new ArrayList<Transaction>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_categories);
        getCategories();

        listView = (ListView) findViewById(R.id.lvCategories);

        tbIO = (ToggleButton) findViewById(R.id.tbIO);

        setupTb();
        checkTb();
        setupFab();

    }

    private void setupFab() {
        AddFloatingActionButton fabNewCategory = (AddFloatingActionButton) findViewById(R.id.fabNewCategory);
        fabNewCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddDialog();
            }
        });
    }

    private void showAddDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.add_category);
        final EditText etNewCategory = getEditText();
        builder.setView(etNewCategory);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addCategory(etNewCategory);
                checkTb();
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
                input = NewEditTransaction.removeSpaces(input);
                if (input.isEmpty()) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }

            }
        });
    }

    private void addCategory(EditText etNewCategory) {
        String newCategory = NewEditTransaction.removeSpaces(etNewCategory.getText().toString());
        boolean goodTogo = true;
        for (String category : useCategories) {
            if (category.equalsIgnoreCase(newCategory)) {
                goodTogo = false;
                break;
            }
        }
        if (goodTogo) {
            useCategories.add(0, newCategory);
            ((MainApplication) getApplication()).writeCategories();
        }
    }


    private void setupTb() {
        tbIO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkTb();
            }
        });
    }

    private void checkTb() {
        if (tbIO.isChecked()) {
            tbIO.setBackgroundColor(getResources().getColor(R.color.green_semi_transparent));
            useCategories = incomeCategories;
            io = true;
        } else {
            tbIO.setBackgroundColor(getResources().getColor(R.color.red_semi_transparent));
            useCategories = outcomeCategories;
            io = false;
        }

        CategoryAdapter adapter = new CategoryAdapter(this, useCategories);
        listView.setAdapter(adapter);
    }

    private void getCategories() {
        outcomeCategories = ((MainApplication) getApplication()).getOutcomeCategories();
        incomeCategories = ((MainApplication) getApplication()).getIncomeCategories();
    }


    public class CategoryAdapter extends BaseAdapter {
        private Context context;
        private LayoutInflater inflater;
        private ArrayList<String> categories;

        public CategoryAdapter(Context context, ArrayList<String> categories) {
            this.context = context;
            this.categories = categories;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return categories.size();
        }

        @Override
        public Object getItem(int position) {
            return categories.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View categoryRow = inflater.inflate(R.layout.category_row, parent, false);
            TextView tvCategoryName = (TextView) categoryRow.findViewById(R.id.tvCategoryName);
//            ImageButton ibEdit = (ImageButton) categoryRow.findViewById(R.id.ibEdit);
            ImageButton ibDelete = (ImageButton) categoryRow.findViewById(R.id.ibDelete);
            tvCategoryName.setText(categories.get(position));
            categoryRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showEditDialog(position);
                }
            });

            ibDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeleteDialog(position);
                }
            });

            return categoryRow;
        }

    }

    private void showDeleteDialog(final int position) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.delete_category);
        List<Transaction> useTransactionList;
        if (io) {
            useTransactionList = getIncomeList();

        } else {
            useTransactionList = getOutcomeList();
        }
        populateAffected(position, useTransactionList);
        setDeleteMessage(builder);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteCategory(position);
                checkTb();
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
    }

    private void showEditDialog(final int position) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText etNewCategory = getEditText();
        etNewCategory.setText(useCategories.get(position));
        builder.setView(etNewCategory);
        builder.setTitle(R.string.change_category_name);
        List<Transaction> useTransactionList;
        if (io) {
            useTransactionList = getIncomeList();

        } else {
            useTransactionList = getOutcomeList();
        }
        populateAffected(position, useTransactionList);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                editCategory(etNewCategory, position);
                checkTb();
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
                input = NewEditTransaction.removeSpaces(input);
                if (input.isEmpty()) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }

            }
        });
    }

    private void populateAffected(int position, List<Transaction> useTransactionList) {
        affectedTransactions.clear();
        for (Transaction transaction : useTransactionList) {
            if (transaction.getCategory().equals(useCategories.get(position))) {
                affectedTransactions.add(transaction);
            }
        }
    }

    private void setDeleteMessage(AlertDialog.Builder builder) {
        if (!affectedTransactions.isEmpty()) {
            boolean andOthers = false;
            boolean hasOneName = false;

            String message = "Deleting this category will also delete the following items: \n";

            for (Transaction transaction : affectedTransactions) {
                String name = transaction.getName();
                if (name == null) {
                    andOthers = true;
                } else {
                    hasOneName = true;
                    message += "\t" + name + "\n";
                }
            }
            if (!hasOneName) {
                message = "Deleting this category will also delete the transactions in this category";
            } else if (andOthers) {
                message += " and other items";
            }
            builder.setMessage(message);

        }
    }

    private void editCategory(EditText etNewCategory, int position) {
        String newCategory = etNewCategory.getText().toString();
        boolean goodTogo = true;
        for (String category : useCategories) {
            if (category.equalsIgnoreCase(newCategory)) {
                goodTogo = false;
                break;
            }
        }
        if (goodTogo) {
            useCategories.set(position, NewEditTransaction.removeSpaces(etNewCategory.getText().toString()));
            ((MainApplication) getApplication()).writeCategories();
            for (Transaction affected : affectedTransactions) {
                getRealm().beginTransaction();
                getRealm().where(Transaction.class)
                        .equalTo("name", affected.getName())
                        .equalTo("category", affected.getCategory())
                        .equalTo("cost", affected.getCost())
                        .equalTo("frequency", affected.getFrequency())
                        .equalTo("income", affected.isIncome())
                        .findFirst().setCategory(etNewCategory.getText().toString());
                getRealm().commitTransaction();
            }
        }
    }

    private void deleteCategory(int position) {
        useCategories.remove(position);
        ((MainApplication) getApplication()).writeCategories();
        for (Transaction affected : affectedTransactions) {
            getRealm().beginTransaction();
            getRealm().where(Transaction.class)
                    .equalTo("name", affected.getName())
                    .equalTo("category", affected.getCategory())
                    .equalTo("cost", affected.getCost())
                    .equalTo("frequency", affected.getFrequency())
                    .equalTo("income", affected.isIncome())
                    .findFirst().deleteFromRealm();
            getRealm().commitTransaction();
        }
    }


    private Realm getRealm() {
        return ((MainApplication) getApplication()).getTransactionRealm();
    }

    public List<Transaction> getIncomeList() {
        RealmResults<Transaction> realmResults = getRealm().where(Transaction.class)
                .equalTo(MainActivity.incomeString, ListActivity.INCOME).findAll();
        ArrayList<Transaction> list = new ArrayList<Transaction>();
        for (int i = 0; i < realmResults.size(); i++) {
            list.add(realmResults.get(i));
        }
        return list;
    }

    public ArrayList<Transaction> getOutcomeList() {
        RealmResults<Transaction> realmResults = getRealm().where(Transaction.class)
                .equalTo(MainActivity.incomeString, ListActivity.OUTCOME).findAll();
        ArrayList<Transaction> list = new ArrayList<Transaction>();
        for (int i = 0; i < realmResults.size(); i++) {
            list.add(realmResults.get(i));
        }
        return list;
    }

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

}
