package com.edu.library.entity;

import com.edu.library.enums.BorrowStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "borrow_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // Many-to-One với Member
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // Many-to-One với Book
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "borrow_date", nullable = false)
    private LocalDateTime borrowDate;

    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;

    @Column(name = "return_date")
    private LocalDateTime returnDate;

    @Column(name = "fine_amount", precision = 10, scale = 2)
    private BigDecimal fineAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BorrowStatus status = BorrowStatus.BORROWED;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "renew_count")
    private Integer renewCount = 0;

    @Column(name = "expected_return_date")
    private LocalDateTime expectedReturnDate;

    // Auditing fields
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper methods - Tính số ngày quá hạn
    public long getOverdueDays() {
        if (returnDate != null) {
            // Đã trả - tính từ dueDate đến returnDate
            if (returnDate.isAfter(dueDate)) {
                return java.time.Duration.between(dueDate, returnDate).toDays();
            }
            return 0;
        } else {
            // Chưa trả - tính từ dueDate đến hiện tại
            if (LocalDateTime.now().isAfter(dueDate)) {
                return java.time.Duration.between(dueDate, LocalDateTime.now()).toDays();
            }
            return 0;
        }
    }

    // Helper methods - Tính phí phạt
    public BigDecimal calculateFine() {
        long overdueDays = getOverdueDays();
        if (overdueDays > 0) {
            // 5,000 VND mỗi ngày quá hạn
            return BigDecimal.valueOf(overdueDays * 5000);
        }
        return BigDecimal.ZERO;
    }

    // Helper methods - Gia hạn mượn sách
    public boolean renew(int additionalDays) {
        if (this.renewCount >= 2) { // Chỉ được gia hạn tối đa 2 lần
            return false;
        }

        if (this.status != BorrowStatus.BORROWED) {
            return false; // Chỉ gia hạn khi đang mượn
        }

        this.dueDate = this.dueDate.plusDays(additionalDays);
        this.renewCount++;
        this.status = BorrowStatus.RENEWED;

        return true;
    }

    // Helper methods - Trả sách
    public void returnBook() {
        this.returnDate = LocalDateTime.now();
        this.fineAmount = calculateFine();

        if (this.fineAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.status = BorrowStatus.OVERDUE;
        } else {
            this.status = BorrowStatus.RETURNED;
        }

        // Cập nhật available copies của sách
        if (this.book != null) {
            this.book.increaseAvailableCopies();
        }
    }

    // Helper methods - Kiểm tra quá hạn
    public boolean isOverdue() {
        return LocalDateTime.now().isAfter(dueDate) && returnDate == null;
    }

    // Helper methods - Đánh dấu sách bị mất
    public void markAsLost() {
        this.status = BorrowStatus.LOST;
        this.fineAmount = BigDecimal.valueOf(100000); // Phí mất sách 100,000 VND
        // Giảm total copies của sách
        if (this.book != null) {
            this.book.setTotalCopies(this.book.getTotalCopies() - 1);
            if (this.book.getTotalCopies() < 0) {
                this.book.setTotalCopies(0);
            }
        }
    }
}