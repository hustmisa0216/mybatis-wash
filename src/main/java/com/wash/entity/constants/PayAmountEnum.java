package com.wash.entity.constants;

public enum PayAmountEnum {

    MOUNTH(3800,1),
    CARD(5000,2),
    THREE_MOUNTH_CARD(9900,4),
    HALF_YEAR(17800,7),
    YEAR(29800,13),
    TWO_YEAR(49800,25),
    VIRTUAL(80000,26);

    private int amount;
    private int mounth;

     PayAmountEnum(int amount,int mounth){
        this.amount=amount;
        this.mounth=mounth;
    }

    public  static int fromAmount(int payAmount){
        PayAmountEnum[] payAmountEnums=PayAmountEnum.values();
        int length=payAmountEnums.length;

        for(int i=0;i<length-1;i++){
            int cur=payAmountEnums[i].amount;
            int next=payAmountEnums[i+1].amount;
            if(payAmount==cur&&payAmount<next){
                return payAmountEnums[i].mounth;
            }else if(payAmount>cur&&payAmount<next){
                return payAmountEnums[i+1].mounth;
            }
        }

        return 0;//不限制月数
    }

    public static void main(String[] args) {
        System.out.println(fromAmount(4100));
    }

}
