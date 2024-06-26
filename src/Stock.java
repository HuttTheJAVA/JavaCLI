public class Stock extends Product{

    private String CEO;

    private int per;

    private int revenue; // 매출액 (기본 단위 = 억) ex) revenue = 10 => 10억

    private String unit = "억"; // revenue의 기본 단위 (ex. 억, 조)

    public Stock(String name,Long price,Long quantity,String CEO,RISK risk){
        super(name,price,quantity,risk);
        this.CEO = CEO;
        this.per = random.nextInt(1,300); // per은 1 ~ 300 사이의 값을 가짐.
        this.revenue = random.nextInt(10,90_000_000);
        if(this.revenue >= 10_000){
            unit = "조";
            this.revenue /= 10_000; // 소수점 단위는 생략
        }
    }

    @Override
    public String toString(){
        String info = "======================== 주식 정보 ========================\n"+
                "                   주식명: "+getName()+"\n"+
                "                   매출액:"+getRevenue()+getUnit()+"\n"+
                "                   주가: "+getPrice()+"\n"+
                "                   발행주식 수: "+getQuantity()+"\n"+
                "                   CEO: "+getCEO()+"\n"+
                "                   PER: "+getPer()+"\n"+
                "=========================================================\n";

        return info;
    }

    public String getCEO() {
        return CEO;
    }

    public int getPer() {
        return per;
    }

    public int getRevenue() {
        return revenue;
    }

    public String getUnit() {
        return unit;
    }
}
