package com.cms.cloudmanagement.service;

import com.cms.cloudmanagement.model.Invoice;
import com.cms.cloudmanagement.model.Payment;
import com.cms.cloudmanagement.model.User;
import com.cms.cloudmanagement.repository.InvoiceRepository;
import com.cms.cloudmanagement.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private NotificationService notificationService;

    public List<Payment> getPaymentsForUser(User user) {
        return paymentRepository.findByUser(user);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public Payment processPayment(Long invoiceId, String method, boolean simulateSuccess) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        Payment payment = new Payment(invoice, invoice.getUser(), invoice.getGrandTotal(), "Pending", method);

        if (simulateSuccess) {
            payment.setStatus("Successful");
            invoice.setStatus("Paid");
            invoiceRepository.save(invoice);
            
            notificationService.createNotification(invoice.getUser(),
                    "Payment of ₹" + String.format("%.2f", invoice.getGrandTotal()) + " for invoice " + invoice.getInvoiceNumber() + " was successful.",
                    "PAYMENT_RECEIVED");
        } else {
            payment.setStatus("Failed");
            notificationService.createNotification(invoice.getUser(),
                    "Payment of ₹" + String.format("%.2f", invoice.getGrandTotal()) + " for invoice " + invoice.getInvoiceNumber() + " failed.",
                    "INFO");
        }

        return paymentRepository.save(payment);
    }

    public Payment processRefund(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment record not found"));

        if ("Successful".equalsIgnoreCase(payment.getStatus())) {
            payment.setStatus("Refunded");
            Invoice invoice = payment.getInvoice();
            invoice.setStatus("Cancelled");
            invoiceRepository.save(invoice);

            notificationService.createNotification(payment.getUser(),
                    "Refund processed for payment ID " + payment.getId() + " of amount ₹" + String.format("%.2f", payment.getAmount()) + ".",
                    "INFO");
        }
        return paymentRepository.save(payment);
    }
}
