package capitec.fraudengine.classes;

public class Transaction {

    private long tranid;
    private String date_time;
    private String card_acceptor;

    public Transaction(Long tranid, String date_time, String card_acceptor) {
        this.tranid = tranid;
        this.date_time = date_time;
        this.card_acceptor = card_acceptor;

    }

    public Long gettran_id() {

        return tranid;

    }

    public String get_date() {

        return date_time;

    }

    public String get_card_acceptor() {

        return card_acceptor;

    }

}