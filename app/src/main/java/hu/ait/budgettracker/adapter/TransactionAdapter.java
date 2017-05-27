package hu.ait.budgettracker.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.ArrayList;

import hu.ait.budgettracker.ListActivity;
import hu.ait.budgettracker.R;
import hu.ait.budgettracker.data.Transaction;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by samberling on 5/21/17.
 */

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
    private RealmResults<Transaction> transactionRealmResults;
    private Realm transactionRealm;
    private ArrayList<Transaction> transactionList;
    private Context context;

    public TransactionAdapter(Context context, Realm transactionRealm) {
        this.transactionRealm = transactionRealm;
        this.context = context;
        transactionRealmResults = transactionRealm.where(Transaction.class).findAll();
        transactionList = new ArrayList<Transaction>();
        for (int i = 0; i < transactionRealmResults.size(); i++) {
            transactionList.add(transactionRealmResults.get(i));
        }
    }

    /**
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     * <p>
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     * <p>
     * The new ViewHolder will be used to display items of the adapter using
     * {#onBindViewHolder(ViewHolder, int, List)}. Since it will be re-used to display
     * different items in the data set, it is a good idea to cache references to sub views of
     * the View to avoid unnecessary {@link View#findViewById(int)} calls.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     * @see #getItemViewType(int)
     * @see #onBindViewHolder(ViewHolder, int)
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.transaction_card, parent, false);
        return new ViewHolder(inflatedView);
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link ViewHolder#itemView} to reflect the item at the given
     * position.
     * <p>
     * Note that unlike {@link ListView}, RecyclerView will not call this method
     * again if the position of the item changes in the data set unless the item itself is
     * invalidated or the new position cannot be determined. For this reason, you should only
     * use the <code>position</code> parameter while acquiring the related data item inside
     * this method and should not keep a copy of it. If you need the position of an item later
     * on (e.g. in a click listener), use {@link ViewHolder#getAdapterPosition()} which will
     * have the updated adapter position.
     * <p>
     * Override {#onBindViewHolder(ViewHolder, int, List)} instead if Adapter can
     * handle efficient partial bind.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(final TransactionAdapter.ViewHolder holder, int position) {
        setVariables(holder, position);
//        editButton(holder);
        deleteButton(holder);
        setBackground(holder, position);
        cardButton(holder);
    }

    private void cardButton(final ViewHolder holder) {
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ListActivity) context).editTransaction(transactionList.get(holder.getAdapterPosition()).getTransactionId(),
                        holder.getAdapterPosition());
            }
        });
    }

    private void setBackground(ViewHolder holder, int position) {
        if(transactionList.get(position).isIncome()){
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.green_semi_transparent));
        }
        else{
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.red_semi_transparent));
        }
    }

    public void deleteTransaction(int position){
        transactionRealm.beginTransaction();
        transactionList.get(position).deleteFromRealm();
        transactionRealm.commitTransaction();
        transactionList.remove(position);
        notifyItemRemoved(position);
    }

    private void deleteButton(final ViewHolder holder) {
        holder.ibDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ListActivity) context).showDeleteDialogBox(holder.getAdapterPosition());
            }
        });
    }

    private void setVariables(ViewHolder holder, int position) {
        String name = transactionList.get(position).getName();
        if(name == null){
            name = transactionList.get(position).getCategory();
        }
        holder.tvCardName.setText(name);
        holder.tvCardCategory.setText(transactionList.get(position).getCategory());
        holder.tvCardCost.setText(NumberFormat.getCurrencyInstance().format(transactionList.get(position).getCost()));
        holder.tvCardFrequency.setText(transactionList.get(position).getFrequency());
        String io;
        if(transactionList.get(position).isIncome()){
            io = context.getString(R.string.income);
        }
        else{
            io=context.getString(R.string.outcome);
        }
        holder.tvCardIO.setText(io);
    }

    private void editButton(final ViewHolder holder) {
        holder.ibEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ListActivity) context).editTransaction(transactionList.get(holder.getAdapterPosition()).getTransactionId(),
                        holder.getAdapterPosition());
            }
        });
    }

    public void updateTransaction(int position, Transaction transaction){
        transactionList.set(position, transaction);
        notifyDataSetChanged();
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public void addNew(Transaction transaction){
        transactionList.add(0, transaction);
        notifyItemInserted(0);
    }

    public void addNew(boolean io, String category, String frequency, double cost, String name) {
        transactionRealm.beginTransaction();
        Transaction newTransaction = transactionRealm.createObject(Transaction.class);
        newTransaction.setIncome(io);
        newTransaction.setCategory(category);
        newTransaction.setName(name);
        newTransaction.setCost(cost);
        newTransaction.setFrequency(frequency);


        transactionRealm.commitTransaction();

        transactionList.add(0, newTransaction);
        notifyItemInserted(0);
    }

    public void addNew(boolean io, String category, String frequency, double cost) {
        transactionRealm.beginTransaction();
        Transaction newTransaction = transactionRealm.createObject(Transaction.class);

        newTransaction.setIncome(io);
        newTransaction.setCategory(category);
        newTransaction.setName("");
        newTransaction.setCost(cost);
        newTransaction.setFrequency(frequency);


        transactionRealm.commitTransaction();

        transactionList.add(0, newTransaction);
        notifyItemInserted(0);
    }

    public void deleteAll(){
        transactionRealm.beginTransaction();
        transactionRealmResults = transactionRealm.where(Transaction.class).findAll();
        transactionRealmResults.deleteAllFromRealm();
        transactionRealm.commitTransaction();
        transactionList.clear();
        notifyDataSetChanged();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder{
        private CardView cardView;
        private TextView tvCardName;
        private TextView tvCardCategory;
        private TextView tvCardCost;
        private TextView tvCardFrequency;
        private TextView tvCardIO;
        private ImageButton ibEdit;
        private ImageButton ibDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.card_view);
            tvCardName = (TextView) itemView.findViewById(R.id.tvCardName);
            tvCardCategory = (TextView) itemView.findViewById(R.id.tvCardCategory);
            tvCardCost = (TextView) itemView.findViewById(R.id.tvCardCost);
            tvCardFrequency = (TextView) itemView.findViewById(R.id.tvCardFrequency);
            tvCardIO = (TextView) itemView.findViewById(R.id.tvCardIO);
//            ibEdit = (ImageButton) itemView.findViewById(R.id.ibEdit);
            ibDelete = (ImageButton) itemView.findViewById(R.id.ibDelete);
        }
    }

}
