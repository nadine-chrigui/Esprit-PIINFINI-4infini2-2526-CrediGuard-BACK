package tn.esprit.pi_back.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class TransportBookingRequestDto {

    @NotNull
    private Long transportServiceId;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime bookingDate;

    private Integer seatNumber;

    @NotNull
    private String bookingStatus;

    public TransportBookingRequestDto() {
    }

    public Long getTransportServiceId() {
        return transportServiceId;
    }

    public void setTransportServiceId(Long transportServiceId) {
        this.transportServiceId = transportServiceId;
    }

    public LocalDateTime getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDateTime bookingDate) {
        this.bookingDate = bookingDate;
    }

    public Integer getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(Integer seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }
}
