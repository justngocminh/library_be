package com.edu.library.entity;


import com.edu.library.enums.BookStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("delete = false")
public class Book {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    UUID id;

    @Column(unique = true, nullable = false, length = 20)
    private String isbn;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "publication_year")
    private Integer publicationYear;

    @Column(length = 100)
    private String publisher;

    @Column(name = "total_copies", nullable = false)
    private Integer totalCopies;

    @Column(name = "available_copies", nullable = false)
    private Integer availableCopies = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookStatus status = BookStatus.AVAILABLE;

    @Column(name = "image_url")
    private String imageUrl;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "book_authors",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    private Set<Author> authors = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @CreationTimestamp
    @Column(name = "create_at", updatable = false)
    private LocalDateTime createAt;

    @UpdateTimestamp
    @Column(name = "update_at")
    private LocalDateTime updateAt;

    @Column(name = "deleted")
    private Boolean deleted = false;

    // Helper method - Giảm số lượng
    public void decreaseAvailableCopies() {
        if (this.availableCopies > 0) {
            this.availableCopies--;
        }
    }

    // Helper method - Tăng số lượng
    public void increaseAvailableCopies() {
        if (this.availableCopies < this.totalCopies) {
            this.availableCopies++;
            updateStatus();
        }
    }

    // Helper method - Cập nhật lại trạng thái theo số lượng
    private void updateStatus() {
        if (this.availableCopies == 0) {
            this.status = BookStatus.BORROWED;
        } else {
            this.status = BookStatus.AVAILABLE;
        }
    }

    // Helper method - Chuyển trạng thái thành xóa
    public void softDelete() {
        this.deleted = true;
    }

    // Helper method - Kiểm tra sách có available không
    public boolean isAvailableForBorrow() {
        return availableCopies > 0 &&
                status == BookStatus.AVAILABLE &&
                !deleted;
    }
}
