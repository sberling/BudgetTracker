package hu.ait.budgettracker.data;


import io.realm.RealmModel;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by samberling on 5/21/17.
 */

public class Transaction extends RealmObject implements RealmModel {


    @PrimaryKey
    private String transactionID;

    private String name;
    private String category;
    private String frequency;
    private double cost;
    private boolean income;

    public boolean isIncome() {
        return income;
    }

    public void setIncome(boolean income) {
        this.income = income;
    }

    public String getName(){
        return name;
    }

    public String getCategory(){
        return category;
    }

    public String getFrequency(){
        return frequency;
    }

    public double getCost(){
        return cost;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setCategory(String category){
        this.category = category;
    }

    public void setFrequency(String frequency){
        this.frequency = frequency;
    }

    public void setCost(double cost){
        this.cost = cost;
    }

    public String getTransactionId() {
        return transactionID;
    }
}
