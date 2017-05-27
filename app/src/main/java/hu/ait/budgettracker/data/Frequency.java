package hu.ait.budgettracker.data;

/**
 * Created by samberling on 5/21/17.
 */

public enum Frequency {
    DAILY  ("Daily",   0, new double[]{(1.0),        (7.0),       (30.42),    (365.25)}),
    WEEKLY ("Weekly",  1, new double[]{(1.0/7.0),    (1.0),       (4.35),     (52.14) }),
    MONTHLY("Monthly", 2, new double[]{(1.0/30.42),  (1.0/4.35),  (1.0),      (12.0)  }),
    ANNUAL ("Annual",  3, new double[]{(1.0/365.25), (1.0/52.14), (1.0/12.0), (1.0)   }),
    NONE   ("Once",    4, new double[]{0,             0,           0,          0      });

    public String getString() {
        return string;
    }

    public int getIndex() {
        return index;
    }

    public double[] getRatios() {
        return ratios;
    }

    private String string;
    private int index; //index in the ratio array
    private double[] ratios; //ratios to the other frequencies

    Frequency(String string, int index, double[] ratios){
        this.string = string;
        this.index = index;
        this.ratios = ratios;
    }

    public static String[] getAllStrings(){
        String[] strings = new String[5];
        for (int i = 0; i < values().length; i++) {
            strings[i] = values()[i].getString();
        }
        return strings;
    }
}
