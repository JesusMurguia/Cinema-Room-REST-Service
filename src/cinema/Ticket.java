package cinema;

public class Ticket {
    private String token;
    private Seat ticket;

    public Ticket(String token, Seat ticket) {
        this.token = token;
        this.ticket = ticket;
        ticket.setPrice();
    }

    public Ticket() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Seat getTicket() {
        return ticket;
    }

    public void setTicket(Seat ticket) {
        this.ticket = ticket;
    }
}
