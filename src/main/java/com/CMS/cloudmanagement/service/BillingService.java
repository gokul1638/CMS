package com.cms.cloudmanagement.service;

import com.cms.cloudmanagement.model.*;
import com.cms.cloudmanagement.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class BillingService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private TariffService tariffService;

    @Autowired
    private NotificationService notificationService;

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    public List<Invoice> getInvoicesForUser(User user) {
        return invoiceRepository.findByUser(user);
    }

    public Invoice getInvoiceById(Long id) {
        return invoiceRepository.findById(id).orElse(null);
    }

    public Invoice generateInvoiceForVM(User user, VirtualMachine vm) {
        String invoiceNumber = "INV-" + (10000 + new Random().nextInt(90000));
        
        LocalDateTime start = LocalDateTime.now().minusDays(30); // Simulated billing period
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime dueDate = LocalDateTime.now().plusDays(15);

        double cpuPrice = tariffService.getPriceForResource("CPU");
        double ramPrice = tariffService.getPriceForResource("RAM");
        double storagePrice = tariffService.getPriceForResource("Storage");
        double gpuPrice = tariffService.getPriceForResource("GPU");
        double bandwidthPrice = tariffService.getPriceForResource("Bandwidth");

        Invoice invoice = new Invoice(
                invoiceNumber,
                user,
                start,
                end,
                0.0,
                0.0,
                0.0,
                0.0,
                dueDate
        );

        double subtotal = 0.0;

        if (vm.getCpuCores() != null && vm.getCpuCores() > 0) {
            InvoiceItem item = new InvoiceItem("CPU - " + vm.getCpuCores() + " Cores", vm.getCpuCores(), cpuPrice);
            invoice.addItem(item);
            subtotal += item.getTotalPrice();
        }

        if (vm.getRamGb() != null && vm.getRamGb() > 0) {
            InvoiceItem item = new InvoiceItem("Memory (RAM) - " + vm.getRamGb() + " GB", vm.getRamGb(), ramPrice);
            invoice.addItem(item);
            subtotal += item.getTotalPrice();
        }

        if (vm.getStorageGb() != null && vm.getStorageGb() > 0) {
            InvoiceItem item = new InvoiceItem("Storage - " + vm.getStorageGb() + " GB", vm.getStorageGb(), storagePrice);
            invoice.addItem(item);
            subtotal += item.getTotalPrice();
        }

        if (vm.getGpuCards() != null && vm.getGpuCards() > 0) {
            InvoiceItem item = new InvoiceItem("GPU - " + vm.getGpuCards() + " Cards", vm.getGpuCards(), gpuPrice);
            invoice.addItem(item);
            subtotal += item.getTotalPrice();
        }

        if (vm.getBandwidthMbps() != null && vm.getBandwidthMbps() > 0) {
            InvoiceItem item = new InvoiceItem("Bandwidth - " + vm.getBandwidthMbps() + " Mbps", vm.getBandwidthMbps(), bandwidthPrice);
            invoice.addItem(item);
            subtotal += item.getTotalPrice();
        }

        double gst = subtotal * 0.18; // 18% GST
        double discount = 0.0;
        if (user.getSubscriptionPlan() != null && 
            !user.getSubscriptionPlan().equalsIgnoreCase("None") && 
            !user.getSubscriptionPlan().equalsIgnoreCase("Custom")) {
            discount = subtotal * 0.10; // 10% discount for pre-defined plans
        }
        double grandTotal = subtotal + gst - discount;

        invoice.setSubtotal(subtotal);
        invoice.setGst(gst);
        invoice.setDiscount(discount);
        invoice.setGrandTotal(grandTotal);

        Invoice saved = invoiceRepository.save(invoice);

        notificationService.createNotification(user, 
                "Invoice " + invoiceNumber + " generated for amount ₹" + String.format("%.2f", grandTotal), 
                "BILL_GENERATED");

        return saved;
    }
}
