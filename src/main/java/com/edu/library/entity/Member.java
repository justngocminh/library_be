package com.edu.library.entity;

import com.edu.library.enums.BorrowStatus;
import com.edu.library.enums.MemberStatus;
import com.edu.library.enums.MemberType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "members")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;  // 👈 UUID

    @Column(name = "member_code", unique = true, nullable = false, length = 20)
    private String memberCode;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 15)
    private String phone;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberType type = MemberType.STUDENT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberStatus status = MemberStatus.ACTIVE;

    @Column(name = "registration_date", nullable = false)
    private LocalDate registrationDate;

    @Column(name = "max_borrow_books", nullable = false)
    private Integer maxBorrowBooks = 5;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper method - Lấy tên đầy đủ
    public String getFullName() {
        return firstName + " " + lastName;
    }

    // One-to-Many với BorrowRecord
    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<BorrowRecord> borrowRecords = new ArrayList<>();

    // Helper method - Kiểm tra member có thể mượn thêm sách không
    public boolean canBorrowMoreBooks() {
        long currentlyBorrowed = borrowRecords.stream()
                .filter(record -> record.getStatus() == BorrowStatus.BORROWED ||
                        record.getStatus() == BorrowStatus.RENEWED)
                .count();
        return currentlyBorrowed < maxBorrowBooks;
    }

    // Helper method - Lấy số sách đang mượn
    public long getCurrentlyBorrowedCount() {
        return borrowRecords.stream()
                .filter(record -> record.getStatus() == BorrowStatus.BORROWED ||
                        record.getStatus() == BorrowStatus.RENEWED)
                .count();
    }

}