package br.com.elitedev.service;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.elitedev.domain.event.Event;
import br.com.elitedev.domain.reservation.Reservation;
import br.com.elitedev.domain.user.User;
import br.com.elitedev.dto.reservation.CreateReservationRequest;
import br.com.elitedev.dto.reservation.ReservationResponse;
import br.com.elitedev.exception.ResourceNotFoundException;
import br.com.elitedev.repository.EventRepository;
import br.com.elitedev.repository.ReservationRepository;
import br.com.elitedev.repository.UserRepository;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            EventRepository eventRepository,
            UserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ReservationResponse create(
            CreateReservationRequest request,
            String customerEmail) {
        User customer = findCustomer(customerEmail);
        Event event = eventRepository.findByIdForUpdate(request.eventId())
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado."));

        event.reserve(request.quantity());
        Reservation reservation = new Reservation(event, customer, request.quantity());
        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> listMine(String customerEmail) {
        User customer = findCustomer(customerEmail);
        return reservationRepository
                .findByCustomerIdOrderByCreatedAtDesc(customer.getId())
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReservationResponse getMine(
            Long reservationId,
            String customerEmail) {
        User customer = findCustomer(customerEmail);
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva não encontrada."));
        validateOwnership(reservation, customer);
        return ReservationResponse.from(reservation);
    }

    @Transactional
    public ReservationResponse cancel(
            Long reservationId,
            String customerEmail) {
        User customer = findCustomer(customerEmail);
        Reservation reservation = reservationRepository.findByIdForUpdate(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva não encontrada."));
        validateOwnership(reservation, customer);

        Event event = eventRepository.findByIdForUpdate(reservation.getEvent().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado."));
        reservation.cancel();
        event.release(reservation.getQuantity());
        return ReservationResponse.from(reservation);
    }

    private User findCustomer(String email) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        return userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado."));
    }

    private void validateOwnership(Reservation reservation, User customer) {
        if (!reservation.getCustomer().getId().equals(customer.getId())) {
            throw new SecurityException("Você não possui permissão para acessar esta reserva.");
        }
    }
}
