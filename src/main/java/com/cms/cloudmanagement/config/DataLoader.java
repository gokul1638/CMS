package com.cms.cloudmanagement.config;

import com.cms.cloudmanagement.model.*;
import com.cms.cloudmanagement.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CloudResourceRepository resourceRepository;

    @Autowired
    private TariffRepository tariffRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private VirtualMachineRepository vmRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Only load if table is empty to avoid duplicate entry errors
        if (userRepository.count() > 0) {
            return;
        }

        System.out.println("Seeding Database with sample values...");

        // 1. Seed Users for each Role
        String defaultPasswordEncrypted = passwordEncoder.encode("password");

        User customer = new User("customer", defaultPasswordEncrypted, "customer@cms.com", "ROLE_CUSTOMER");
        customer.setSubscriptionPlan("Standard");
        userRepository.save(customer);

        User support = new User("support", defaultPasswordEncrypted, "support@cms.com", "ROLE_SUPPORT");
        userRepository.save(support);

        User billing = new User("billing", defaultPasswordEncrypted, "billing@cms.com", "ROLE_BILLING_ADMIN");
        userRepository.save(billing);

        User admin = new User("admin", defaultPasswordEncrypted, "admin@cms.com", "ROLE_ADMIN");
        userRepository.save(admin);

        User superadmin = new User("superadmin", defaultPasswordEncrypted, "superadmin@cms.com", "ROLE_SUPER_ADMIN");
        userRepository.save(superadmin);

        System.out.println("✓ Seeded users successfully.");

        // 2. Seed Resource Catalog (CPU, RAM, Storage, GPU, Bandwidth)
        resourceRepository.save(new CloudResource("CPU", "Core", "Compute processing core capacity", 400.0, 160, 500, "ACTIVE"));
        resourceRepository.save(new CloudResource("RAM", "GB", "High-speed volatile memory storage", 120.0, 324, 1024, "ACTIVE"));
        resourceRepository.save(new CloudResource("Storage", "GB", "SSD Block Storage space", 10.0, 30720, 51200, "ACTIVE")); // 30 TB free out of 50 TB
        resourceRepository.save(new CloudResource("GPU", "Card", "Graphics processing accelerator acceleration", 7000.0, 16, 32, "ACTIVE"));
        resourceRepository.save(new CloudResource("Bandwidth", "Mbps", "Network throughput speed limit", 5.0, 5000, 10000, "ACTIVE"));

        System.out.println("✓ Seeded resource catalog successfully.");

        // 3. Seed Tariffs
        tariffRepository.save(new Tariff("CPU", 400.0, LocalDateTime.now()));
        tariffRepository.save(new Tariff("RAM", 120.0, LocalDateTime.now()));
        tariffRepository.save(new Tariff("Storage", 10.0, LocalDateTime.now()));
        tariffRepository.save(new Tariff("GPU", 7000.0, LocalDateTime.now()));
        tariffRepository.save(new Tariff("Bandwidth", 5.0, LocalDateTime.now()));

        System.out.println("✓ Seeded active tariffs successfully.");

        // 4. Seed Resource Inventory (Total, Allocated)
        inventoryRepository.save(new Inventory("CPU", 500, 340));
        inventoryRepository.save(new Inventory("RAM", 1024, 700));
        inventoryRepository.save(new Inventory("Storage", 51200, 20480)); // 20 TB allocated, 30 TB available
        inventoryRepository.save(new Inventory("GPU", 32, 16));
        inventoryRepository.save(new Inventory("Bandwidth", 10000, 5000));

        System.out.println("✓ Seeded inventory capacities successfully.");

        // 5. Seed Running Virtual Machines
        VirtualMachine vm1 = new VirtualMachine("Production Web Server", "Ubuntu", 4, 8, 100, 0, 1000, "us-east-1", customer);
        vm1.setStatus("Running");
        vmRepository.save(vm1);

        VirtualMachine vm2 = new VirtualMachine("Database Server", "RedHat Linux", 8, 16, 250, 1, 500, "us-east-1", customer);
        vm2.setStatus("Running");
        vmRepository.save(vm2);

        VirtualMachine vm3 = new VirtualMachine("Backup Storage Node", "CentOS", 2, 4, 500, 0, 100, "us-west-2", customer);
        vm3.setStatus("Stopped");
        vmRepository.save(vm3);

        System.out.println("✓ Seeded sample virtual machines successfully.");

        // 6. Seed Invoices and Invoice Line Items
        Invoice invoicePaid = new Invoice("INV-10294", customer, LocalDateTime.now().minusDays(60), LocalDateTime.now().minusDays(30), 4500.0, 810.0, 450.0, 4860.0, LocalDateTime.now().minusDays(15));
        invoicePaid.setStatus("Paid");
        invoicePaid.addItem(new InvoiceItem("CPU - 4 Cores", 4, 400.0));
        invoicePaid.addItem(new InvoiceItem("Memory - 8 GB", 8, 120.0));
        invoicePaid.addItem(new InvoiceItem("Storage - 100 GB", 100, 10.0));
        invoiceRepository.save(invoicePaid);

        // Record a payment for the paid invoice
        paymentRepository.save(new Payment(invoicePaid, customer, 4860.0, "Successful", "Credit Card"));

        Invoice invoicePending = new Invoice("INV-94021", customer, LocalDateTime.now().minusDays(30), LocalDateTime.now(), 6500.0, 1170.0, 650.0, 7020.0, LocalDateTime.now().plusDays(15));
        invoicePending.setStatus("Generated");
        invoicePending.addItem(new InvoiceItem("CPU - 8 Cores", 8, 400.0));
        invoicePending.addItem(new InvoiceItem("Memory - 16 GB", 16, 120.0));
        invoicePending.addItem(new InvoiceItem("Storage - 250 GB", 250, 10.0));
        invoicePending.addItem(new InvoiceItem("GPU - 1 Card", 1, 7000.0));
        invoiceRepository.save(invoicePending);

        System.out.println("✓ Seeded sample invoices and payments successfully.");

        // 7. Seed Provisioning Orders
        Order orderCompleted = new Order(customer, "Production Web Server", "Ubuntu", 4, 8, 100, 0, 1000, "us-east-1");
        orderCompleted.setStatus("Completed");
        orderRepository.save(orderCompleted);

        Order orderPending = new Order(customer, "DeepLearning Sandbox", "Windows Server", 16, 32, 500, 2, 2000, "ap-south-1");
        orderPending.setStatus("Pending Approval");
        orderRepository.save(orderPending);

        System.out.println("✓ Seeded sample orders successfully.");

        // 8. Seed Notifications
        notificationRepository.save(new Notification(customer, "Welcome to the Cloud Management System! Customize your subscription plan in the dashboard.", "INFO"));
        notificationRepository.save(new Notification(customer, "Order for VM 'Production Web Server' completed and resources provisioned.", "VM_CREATED"));
        notificationRepository.save(new Notification(customer, "Monthly invoice INV-94021 for ₹7,020.00 is generated and due on " + invoicePending.getDueDate().toLocalDate(), "BILL_GENERATED"));

        System.out.println("✓ Seeded sample notifications successfully.");

        // 9. Seed Audit Logs
        auditLogRepository.save(new AuditLog("system", "Initialize Database", null, "Database Seed Success", "127.0.0.1"));
        auditLogRepository.save(new AuditLog("customer", "Register", null, "Created standard CUSTOMER account", "192.168.1.10"));
        auditLogRepository.save(new AuditLog("admin", "Login", null, "Opened dashboard configuration console", "192.168.1.5"));

        System.out.println("✓ Seeded sample audit logs successfully.");
        System.out.println("✓ Data seeding completed successfully!");
    }
}
