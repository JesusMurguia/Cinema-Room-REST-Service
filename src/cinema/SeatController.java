package cinema;

import org.apache.juli.logging.Log;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

@RestController
public class SeatController {
    private int rows = 9;
    private int columns = 9;
    private ConcurrentMap<String, Seat> seats = generateSeats(rows, columns);
    private ConcurrentMap<String, Ticket> tickets = new ConcurrentHashMap<>();
    private volatile int income = 0;


    @GetMapping("/seats")
    public Room getSeats() {
        return new Room(rows,columns, new ArrayList<>(seats.values()));
    }


    @PostMapping("/purchase")
    public ResponseEntity<?> postSeat(@RequestBody Seat seat) {
        if(isSeatValid(seat)){
            return new ResponseEntity(Map.of("error", "The number of a row or a column is out of bounds!"), HttpStatus.BAD_REQUEST);
        }
        if(seats.containsKey(getKey(seat))) {
            Ticket ticket = new Ticket(UUID.randomUUID().toString(),seat);
            tickets.put(ticket.getToken(), ticket);
            updateIncome(seat.getPrice());
            seats.remove(getKey(seat));
            return new ResponseEntity<>(ticket, HttpStatus.OK);
        }else{
            return new ResponseEntity(Map.of("error", "The ticket has been already purchased!"), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/return")
    public ResponseEntity<?> returnTicket(@RequestBody Ticket ticket) {
        if(!tickets.containsKey(ticket.getToken())){
            return new ResponseEntity(Map.of("error", "Wrong token!"), HttpStatus.BAD_REQUEST);
        }
        ticket.setTicket(tickets.get(ticket.getToken()).getTicket());
        seats.put(ticket.getToken(), ticket.getTicket());
        tickets.remove(ticket.getToken());
        updateIncome(-ticket.getTicket().getPrice());
        return new ResponseEntity<>(Map.of("returned_ticket", ticket.getTicket()), HttpStatus.OK);
    }

    @PostMapping("/stats")
    public ResponseEntity<?> getStats(@RequestParam (required = false) String password) {
        if("super_secret".equals(password)){
            return new ResponseEntity<>(Map.of(
                    "current_income", tickets,
                    "number_of_available_seats", seats.size(),
                    "number_of_purchased_tickets", tickets.size()
            ), HttpStatus.OK);
        }
        return new ResponseEntity(Map.of("error", "The password is wrong!"), HttpStatus.UNAUTHORIZED);
    }


    public synchronized void updateIncome(double value) {
        income += value;
    }


    private ConcurrentMap<String, Seat> generateSeats(int rows, int columns){
        seats = new ConcurrentHashMap<>();
        for (int i = 1; i <= rows; i++) {
            for (int j = 1; j <= columns; j++) {
                seats.put(String.valueOf(i) + j,new Seat(i,j));
            }
        }
        return seats;
    }

    private boolean isSeatValid(Seat seat){
        return seat.getRow() < 1 || seat.getRow() > rows || seat.getColumn() < 1 || seat.getColumn() > columns;
    }

    public String getKey(Seat seat){ return String.valueOf(seat.getRow())+seat.getColumn();}

}
