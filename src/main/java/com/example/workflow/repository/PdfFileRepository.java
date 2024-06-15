package com.example.workflow.repository;

import com.example.workflow.model.entity.PdfFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PdfFileRepository extends JpaRepository<PdfFile, Long> {
}
