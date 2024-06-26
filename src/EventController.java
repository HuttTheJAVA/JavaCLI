import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class EventController {

    private ProductController productController = ProductController.getProductController();
    private EventController() {}

    private static final EventController eventController = new EventController();

    public static EventController getEventController(){
        return eventController;
    }

    private static final Random random = new Random();

    private ArrayList<String> upMessage = new ArrayList<>();

    private ArrayList<String> downMessage = new ArrayList<>();

    // 공통 속성들
    private double rise_Probability; // 주식 상승 확률 / 1 - stockRiseProbability = 하락 확률;
    private double upDown_Rate; // 주가 등락률 //난이도에 따라 정해짐. // 주가 상,하락 = 주가*upDownRate*stockRiseProbability => 상승 또는 하락
    private double ceil_Probability; // 상한가 확률
    private double floor_Probability; // 하한가 확률

    private double limit_ratio; // 투자 원금의 마지노선 비율 ex limit_ratio = 0.3 ==> (현재 재산 < 0.3 * 투자원금) == GAME OVER
    public void setMode(int mode){

        if(mode == 0){
            init(0.7,8,0.3,0.05,0.3);
        }
        else if(mode == 1){
            init(0.6,5,0.15,0.15,0.4);
        }else{
            init(0.3,10,0.05,0.3,0.5);
        }
    }

    public void refresh() throws InterruptedException {
        synchronized (productController.getLock()) {
            upMessage = new ArrayList<>();
            downMessage = new ArrayList<>();

            HashMap<String,Product> products = productController.getProduct_lst();

            for (String name : products.keySet()) {
                Product product = products.get(name);
                String message = event(product);
                if (message.contains("▲") || message.contains("\uD83E\uDC45")) {
                    upMessage.add(message);
                } else {
                    downMessage.add(message);
                }
            }

            System.out.println("♨♨♨♨♨♨ ♨♨♨♨ ♨♨♨♨♨♨ ♨♨♨♨♨♨ ♨ ♨♨ ♨ refresh 시작! ♨♨♨♨♨♨ ♨♨♨♨ ♨♨♨♨♨♨ ♨♨♨♨♨♨ ♨ ♨♨ ♨");
            System.out.println("▤▤▤▤ ▤ ▤ ▤▤ ▤▤ ▤▤▤ ▤ ▤▤▤ 8초 동안 얼음! ▤▤▤▤ ▤ ▤ ▤▤ ▤▤ ▤▤▤ ▤ ▤▤▤ ");
            Thread.sleep(8000);

//            printMessage();
        }
    }

    public String event(Product product){ // 주식, 펀드, 채권에 따라 차별해서 관리

        if(product instanceof Fund){
            if(eventHappen(0.1)){   // 10%의 확률로 펀드의 종목 구성을 변경한다. (변경 시 펀드의 리스크 등급이 바뀜. -> 종목의 상승,하락 폭이 바뀜)
                Fund fund = (Fund) product;
                fund.restructuring();
            }
        }

        double rise_weight; // 주식 위험에 따른 rise_Probability에 곱할 가중치
        double upDown_weight; // 주식 위험에 따른 upDown_Probability에 곱할 가중치
        
        if(product.getRisk() == RISK.LOW) { // 0.7 ~ 1.2 사이의 가중치를 가지며, 이는 rise_Probability를 증감시키는 가중치이다. (Low라서 변동폭이 작음)
            rise_weight = 0.9 + 0.2 * (random.nextDouble()); // 0.9 ~ 1.1 사이의 가중치를 가지며, 이는 rise_Probability를 증감시키는 가중치이다.
            upDown_weight = 1.0 + 2 * (random.nextDouble()); // 1.0 ~ 3.0 사이의 가중치를 가지며, 이는 upDown_Rate에 더해지는 %이다. (가중치 X 그냥 더해지는 %)
        }else if(product.getRisk() == RISK.MID){
            rise_weight = 0.8 + 0.4 * (random.nextDouble()); // 0.8 ~ 1.2 사이의 가중치를 가지며, 이는 rise_Probability를 증감시키는 가중치이다.
            upDown_weight = 1.5 + 3 * (random.nextDouble()); // 1.5 ~ 4.5 사이의 가중치를 가지며, 이는 upDown_Rate에 더해지는 %이다. (가중치 X 그냥 더해지는 %)
        }else{
            rise_weight = 0.7 + 0.6 * (random.nextDouble()); // 0.7 ~ 1.3 사이의 가중치를 가지며, 이는 rise_Probability를 증감시키는 가중치이다.
            upDown_weight = 3.0 + 10 * (random.nextDouble()); // 3.0 ~ 13.0 사이의 가중치를 가지며, 이는 upDown_Rate에 더해지는 %이다. (가중치 X 그냥 더해지는 %)
        }

        if(eventHappen(floor_Probability)){ // 하한가 확률 당첨 시
            product.floor();

            return message(product,"-",30l,"\uD83E\uDC47");

        }else if(eventHappen(ceil_Probability)){ // 상한가 확률 당첨 시
            product.ceil();

            return message(product,"+",30l,"\uD83E\uDC45");

        }else if(eventHappen(rise_Probability * rise_weight)){ // 상승 확률 당첨 시
            double upDownPercent = 1.0 + (upDown_Rate+upDown_weight)/100;
            product.setPrice(Math.round(upDownPercent * product.getPrice()));

            return message(product,"+",upDown_Rate+upDown_weight,"▲");

        }else{ // 하락 확률 당첨 시
            double upDownPercent = 1.0 - (upDown_Rate+upDown_weight)/100;
            product.setPrice(Math.round(upDownPercent * product.getPrice()));

            return message(product,"-",upDown_Rate+upDown_weight,"▼");
        }
    }

    public static boolean eventHappen(double probability){
        return random.nextDouble() < probability;
    }

    public void init(double rise_Probability,double upDown_Rate,double ceil_Probability, double floor_Probability,double limit_ratio){
        this.rise_Probability = rise_Probability;
        this.upDown_Rate = upDown_Rate;
        this.ceil_Probability = ceil_Probability;
        this.floor_Probability = floor_Probability;
        this.limit_ratio = limit_ratio;
    }

    public String message(Product product,String sign, double upDown,String arrow) {
        return String.format("%s %,d (%s%.2f%% %s)", product.getName(),product.getPrice(),sign, upDown,arrow);
    }

    public boolean gameOver(double ratio){
        return ratio < limit_ratio;
    }

    public void printMessage(){
        System.out.println("-- 상승 --");

        for(String msg : upMessage){
            System.out.println(msg);
        }

        System.out.println("\n-- 하락 --");

        for(String msg : downMessage){
            System.out.println(msg);
        }
    }
}
