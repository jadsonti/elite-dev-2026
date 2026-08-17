package br.com.elitedev.service;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.elitedev.domain.payment.Payment;
import br.com.elitedev.domain.payment.PaymentStatus;
import br.com.elitedev.domain.reservation.Reservation;
import br.com.elitedev.domain.reservation.ReservationStatus;
import br.com.elitedev.domain.user.User;
import br.com.elitedev.dto.payment.PaymentResponse;
import br.com.elitedev.dto.payment.ProcessPaymentRequest;
import br.com.elitedev.exception.ResourceNotFoundException;
import br.com.elitedev.repository.PaymentRepository;
import br.com.elitedev.repository.ReservationRepository;
import br.com.elitedev.repository.UserRepository;

@Service
public class PaymentService {

    private static final String DECLINED_REASON =
            "Pagamento recusado pela simulação.";

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;

    public PaymentService(
            PaymentRepository paymentRepository,
            ReservationRepository reservationRepository,
            UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public PaymentResponse process(
            ProcessPaymentRequest request,
            String customerEmail) {
        User customer = findCustomer(customerEmail);
        Reservation reservation = findReservationForUpdate(request.reservationId());
        validateOwnership(reservation, customer);

        String failureReason = null;
        if (request.outcome() == PaymentStatus.APPROVED) {
            reservation.confirmPayment();
        } else {
            validatePendingPayment(reservation);
            failureReason = DECLINED_REASON;
        }

        Payment payment = new Payment(
                reservation,
                customer,
                request.outcome(),
                failureReason);
        return PaymentResponse.from(paymentRepository.save(payment));
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> listByReservation(
            Long reservationId,
            String customerEmail) {
        User customer = findCustomer(customerEmail);
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva não encontrada."));
        validateOwnership(reservation, customer);

        return paymentRepository
                .findByReservationIdOrderByProcessedAtDesc(reservationId)
                .stream()
                .map(PaymentResponse::from)
                .toList();
    }

    private Reservation findReservationForUpdate(Long reservationId) {
        return reservationRepository.findByIdForUpdate(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva não encontrada."));
    }

    private User findCustomer(String email) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        return userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado."));
    }

    private void validatePendingPayment(Reservation reservation) {
        if (reservation.getStatus() != ReservationStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("A reserva não está aguardando pagamento.");
        }
    }

    private void validateOwnership(Reservation reservation, User customer) {
        if (!reservation.getCustomer().getId().equals(customer.getId())) {
            throw new SecurityException("Você não possui permissão para pagar esta reserva.");
        }
    }
}
